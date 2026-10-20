package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 本地 Qwen3-TTS（OpenAI 兼容 /v1/audio/speech）。
 * <p>
 * 使用 HttpURLConnection 而非 java.net.http.HttpClient：后者在连 uvicorn 时偶发
 * 422 body missing / 400 Invalid HTTP request（keep-alive / 重定向丢 body）。
 */
@Component
public class Qwen3TtsClient {
    private static final int MAX_INPUT_CHARS = 4096;

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final String voice;
    private final String responseFormat;
    private final int timeoutSeconds;

    public Qwen3TtsClient(
        @Value("${arelore.tts.qwen.enabled:true}") boolean enabled,
        @Value("${arelore.tts.qwen.base-url:http://127.0.0.1:8880}") String baseUrl,
        @Value("${arelore.tts.qwen.model:qwen3-tts}") String model,
        @Value("${arelore.tts.qwen.voice:alloy}") String voice,
        @Value("${arelore.tts.qwen.response-format:mp3}") String responseFormat,
        @Value("${arelore.tts.qwen.timeout-seconds:180}") int timeoutSeconds
    ) {
        this.enabled = enabled;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = StringUtils.hasText(model) ? model.trim() : "qwen3-tts";
        this.voice = StringUtils.hasText(voice) ? voice.trim() : "alloy";
        this.responseFormat = StringUtils.hasText(responseFormat) ? responseFormat.trim() : "mp3";
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 180;
    }

    public boolean isEnabled() {
        return enabled && StringUtils.hasText(baseUrl);
    }

    public String getVoice() {
        return voice;
    }

    /**
     * 合成语音，返回 mp3（或配置的格式）字节。
     */
    public byte[] synthesize(String input) throws Exception {
        if (!isEnabled()) {
            throw new IllegalStateException("本地 Qwen3-TTS 未启用或未配置 base-url");
        }
        String text = sanitizeInput(input);
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("TTS 文本为空");
        }

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("voice", voice);
        body.put("input", text);
        body.put("response_format", responseFormat);
        body.put("language", "Auto");
        byte[] payload = body.toJSONString().getBytes(StandardCharsets.UTF_8);

        URL url = URI.create(baseUrl + "/v1/audio/speech").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(timeoutSeconds * 1000);
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "audio/mpeg, application/octet-stream, */*");
        conn.setRequestProperty("Connection", "close");
        conn.setFixedLengthStreamingMode(payload.length);

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
            throw new IllegalStateException("TTS HTTP " + status + (err.isEmpty() ? "" : ": " + err));
        }
        if (data == null || data.length == 0) {
            throw new IllegalStateException("TTS 返回空音频");
        }
        return data;
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
