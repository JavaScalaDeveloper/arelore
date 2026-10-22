package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 本地 Qwen3-TTS（OpenAI 兼容 /v1/audio/speech）。
 * <p>
 * 使用 HttpURLConnection 而非 java.net.http.HttpClient：后者在连 uvicorn 时偶发
 * 422 body missing / 400 Invalid HTTP request（keep-alive / 重定向丢 body）。
 * <p>
 * 客户端用 Semaphore 对齐服务端 TTS_MAX_CONCURRENT（默认 1）：批量并行时在本地排队，
 * 不把多个 HTTP 请求堆到 TTS 侧，避免读超时把「排队等待」也算进去。
 */
@Component
public class Qwen3TtsClient {
    private static final Logger log = LoggerFactory.getLogger(Qwen3TtsClient.class);
    private static final int MAX_INPUT_CHARS = 4096;

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final String voice;
    private final String responseFormat;
    private final int timeoutSeconds;
    private final int maxAttempts;
    private final int retryDelayMs;
    private final Semaphore inFlight;

    public Qwen3TtsClient(
        @Value("${arelore.tts.qwen.enabled:true}") boolean enabled,
        @Value("${arelore.tts.qwen.base-url:http://127.0.0.1:8880}") String baseUrl,
        @Value("${arelore.tts.qwen.model:qwen3-tts}") String model,
        @Value("${arelore.tts.qwen.voice:alloy}") String voice,
        @Value("${arelore.tts.qwen.response-format:mp3}") String responseFormat,
        @Value("${arelore.tts.qwen.timeout-seconds:1800}") int timeoutSeconds,
        @Value("${arelore.tts.qwen.max-attempts:3}") int maxAttempts,
        @Value("${arelore.tts.qwen.retry-delay-ms:3000}") int retryDelayMs,
        @Value("${arelore.tts.qwen.in-flight:1}") int inFlight
    ) {
        this.enabled = enabled;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = StringUtils.hasText(model) ? model.trim() : "qwen3-tts";
        this.voice = StringUtils.hasText(voice) ? voice.trim() : "alloy";
        this.responseFormat = StringUtils.hasText(responseFormat) ? responseFormat.trim() : "mp3";
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 1800;
        this.maxAttempts = maxAttempts > 0 ? Math.min(8, maxAttempts) : 3;
        this.retryDelayMs = retryDelayMs > 0 ? retryDelayMs : 3000;
        int permits = inFlight > 0 ? Math.min(8, inFlight) : 1;
        this.inFlight = new Semaphore(permits, true);
    }

    public boolean isEnabled() {
        return enabled && StringUtils.hasText(baseUrl);
    }

    public String getVoice() {
        return voice;
    }

    /** 拼出可直接复制到终端复现的 curl（与 README 同形态）。 */
    public String toCurl(String input) {
        String text = sanitizeInput(input);
        JSONObject body = buildBody(text);
        String json = body.toJSONString();
        // bash 单引号：把 ' 写成 '\''
        String quoted = json.replace("'", "'\\''");
        return "curl -X POST " + baseUrl + "/v1/audio/speech"
            + " -H \"Content-Type: application/json\""
            + " -d '" + quoted + "'"
            + " --output speech.mp3";
    }

    /**
     * 合成语音，返回 mp3（或配置的格式）字节。
     * 先占用 in-flight 许可再发 HTTP，使读超时只覆盖实际合成；瞬时失败按 max-attempts 重试。
     */
    public byte[] synthesize(String input) throws Exception {
        if (!isEnabled()) {
            throw new IllegalStateException("本地 Qwen3-TTS 未启用或未配置 base-url");
        }
        String text = sanitizeInput(input);
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("TTS 文本为空");
        }

        // 排队等待不计入 HTTP read timeout；上限约 (attempts * timeout) + 余量
        long acquireTimeoutSec = Math.max(60L, (long) timeoutSeconds * maxAttempts + 120L);
        try {
            if (!inFlight.tryAcquire(acquireTimeoutSec, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                    "TTS 客户端排队超时（等待 in-flight 许可超过 " + acquireTimeoutSec + "s）");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("TTS 客户端排队被中断", ie);
        }
        try {
            return synthesizeWithRetry(text);
        } finally {
            inFlight.release();
        }
    }

    private byte[] synthesizeWithRetry(String text) throws Exception {
        String curl = toCurl(text);
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return synthesizeOnce(text, curl);
            } catch (SocketTimeoutException e) {
                last = e;
                log.warn("TTS read timed out attempt={}/{} chars={} timeoutSec={} curl={}",
                    attempt, maxAttempts, text.length(), timeoutSeconds, curl);
                if (attempt >= maxAttempts) {
                    break;
                }
                sleepBeforeRetry(attempt);
            } catch (IllegalStateException e) {
                // HTTP 4xx/5xx、空音频：业务错误，不重试
                throw e;
            } catch (Exception e) {
                last = e;
                if (!isTransientIo(e) || attempt >= maxAttempts) {
                    log.warn("TTS failed chars={} curl={} err={}", text.length(), curl, e.toString());
                    throw new IllegalStateException(e.getMessage() + " | curl: " + curl, e);
                }
                log.warn("TTS transient failure attempt={}/{} chars={} err={}",
                    attempt, maxAttempts, text.length(), e.toString());
                sleepBeforeRetry(attempt);
            }
        }
        throw new IllegalStateException(
            "Read timed out (timeoutSec=" + timeoutSeconds + ", chars=" + text.length()
                + ", attempts=" + maxAttempts + ") | curl: " + curl,
            last);
    }

    private byte[] synthesizeOnce(String text, String curl) throws Exception {
        JSONObject body = buildBody(text);
        byte[] payload = body.toJSONString().getBytes(StandardCharsets.UTF_8);

        URL url = URI.create(baseUrl + "/v1/audio/speech").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10_000);
        int readTimeoutMs = (int) Math.min(Integer.MAX_VALUE, (long) timeoutSeconds * 1000L);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "audio/mpeg, application/octet-stream, */*");
        conn.setRequestProperty("Connection", "close");
        conn.setFixedLengthStreamingMode(payload.length);

        try {
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
                out.flush();
            }

            int status = conn.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream();
            byte[] data = readAll(stream);
            if (status < 200 || status >= 300) {
                String err = data == null || data.length == 0 ? "" : new String(data, StandardCharsets.UTF_8);
                if (err.length() > 300) {
                    err = err.substring(0, 300);
                }
                log.warn("TTS HTTP {} chars={} timeoutSec={} curl={}", status, text.length(), timeoutSeconds, curl);
                throw new IllegalStateException(
                    "TTS HTTP " + status + (err.isEmpty() ? "" : ": " + err) + " | curl: " + curl);
            }
            if (data == null || data.length == 0) {
                log.warn("TTS empty audio chars={} curl={}", text.length(), curl);
                throw new IllegalStateException("TTS 返回空音频 | curl: " + curl);
            }
            return data;
        } finally {
            conn.disconnect();
        }
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        long delay = (long) retryDelayMs * attempt;
        Thread.sleep(delay);
    }

    private static boolean isTransientIo(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SocketTimeoutException) {
                return true;
            }
            String name = t.getClass().getName();
            if (name.contains("ConnectException")
                || name.contains("ConnectionReset")
                || name.contains("SocketException")) {
                return true;
            }
            String msg = t.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("connection reset")
                    || lower.contains("broken pipe")
                    || lower.contains("connection refused")) {
                    return true;
                }
            }
        }
        return false;
    }

    private JSONObject buildBody(String text) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("voice", voice);
        body.put("input", text == null ? "" : text);
        body.put("response_format", responseFormat);
        return body;
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (!StringUtils.hasText(baseUrl)) {
            return "";
        }
        String url = baseUrl.trim().replaceAll("/+$", "");
        // Windows 上 localhost 可能走 IPv6 (::1)，与 uvicorn 监听不一致时易异常
        if (url.startsWith("http://localhost:") || url.equals("http://localhost")) {
            url = url.replaceFirst("http://localhost", "http://127.0.0.1");
        }
        return url;
    }

    private static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        String text = input.trim().replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", " ").replaceAll("\\s+", " ").trim();
        if (text.length() > MAX_INPUT_CHARS) {
            text = text.substring(0, MAX_INPUT_CHARS);
        }
        return text;
    }

    private static byte[] readAll(InputStream in) throws Exception {
        if (in == null) {
            return new byte[0];
        }
        try (InputStream stream = in; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = stream.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        }
    }
}
