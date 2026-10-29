package com.arelore.server.core.biz.word.admin.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 本地 Edge TTS：调用 al-basic-components/local/edge-tts/speak.py，
 * 走微软在线神经音色，并再压成小体积 MP3（默认 32k / 16kHz 单声道）。
 */
@Component
public class EdgeTtsClient {
    private static final Logger log = LoggerFactory.getLogger(EdgeTtsClient.class);
    private static final int MAX_INPUT_CHARS = 4096;

    private final boolean enabled;
    private final String pythonExe;
    private final Path scriptPath;
    private final String voice;
    private final String bitrate;
    private final int sampleRate;
    private final int timeoutSeconds;
    private final int maxAttempts;
    private final int retryDelayMs;
    private final Semaphore inFlight;

    public EdgeTtsClient(
        @Value("${arelore.tts.edge.enabled:true}") boolean enabled,
        @Value("${arelore.tts.edge.python:python}") String pythonExe,
        @Value("${arelore.tts.edge.script:C:/Workspaces/person/al-basic-components/local/edge-tts/speak.py}") String script,
        @Value("${arelore.tts.edge.voice:zh-CN-XiaoxiaoNeural}") String voice,
        @Value("${arelore.tts.edge.bitrate:32k}") String bitrate,
        @Value("${arelore.tts.edge.sample-rate:16000}") int sampleRate,
        @Value("${arelore.tts.edge.timeout-seconds:120}") int timeoutSeconds,
        @Value("${arelore.tts.edge.max-attempts:3}") int maxAttempts,
        @Value("${arelore.tts.edge.retry-delay-ms:1500}") int retryDelayMs,
        @Value("${arelore.tts.edge.in-flight:4}") int inFlight
    ) {
        this.enabled = enabled;
        this.pythonExe = StringUtils.hasText(pythonExe) ? pythonExe.trim() : "python";
        this.scriptPath = StringUtils.hasText(script) ? Path.of(script.trim()).toAbsolutePath().normalize() : null;
        this.voice = StringUtils.hasText(voice) ? voice.trim() : "zh-CN-XiaoxiaoNeural";
        this.bitrate = StringUtils.hasText(bitrate) ? bitrate.trim() : "32k";
        this.sampleRate = sampleRate > 0 ? sampleRate : 16000;
        this.timeoutSeconds = timeoutSeconds > 0 ? timeoutSeconds : 120;
        this.maxAttempts = maxAttempts > 0 ? Math.min(8, maxAttempts) : 3;
        this.retryDelayMs = retryDelayMs > 0 ? retryDelayMs : 1500;
        int permits = inFlight > 0 ? Math.min(16, inFlight) : 4;
        this.inFlight = new Semaphore(permits, true);
    }

    public boolean isEnabled() {
        return enabled && scriptPath != null && Files.isRegularFile(scriptPath);
    }

    public String getVoice() {
        return voice;
    }

    public String getBitrate() {
        return bitrate;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    /** 合成并压缩后的 mp3 字节。 */
    public byte[] synthesize(String input) throws Exception {
        if (!isEnabled()) {
            throw new IllegalStateException(
                "Edge TTS 未启用或脚本不存在: " + (scriptPath == null ? "(null)" : scriptPath));
        }
        String text = sanitizeInput(input);
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException("TTS 文本为空");
        }

        long acquireTimeoutSec = Math.max(60L, (long) timeoutSeconds * maxAttempts + 60L);
        try {
            if (!inFlight.tryAcquire(acquireTimeoutSec, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                    "Edge TTS 客户端排队超时（等待 in-flight 许可超过 " + acquireTimeoutSec + "s）");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Edge TTS 客户端排队被中断", ie);
        }
        try {
            return synthesizeWithRetry(text);
        } finally {
            inFlight.release();
        }
    }

    private byte[] synthesizeWithRetry(String text) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return synthesizeOnce(text);
            } catch (IllegalStateException e) {
                // 脚本缺失、空音频等业务错误不重试
                if (e.getMessage() != null && e.getMessage().contains("exit=")) {
                    last = e;
                    if (attempt >= maxAttempts) {
                        break;
                    }
                    log.warn("Edge TTS process failed attempt={}/{} chars={} err={}",
                        attempt, maxAttempts, text.length(), e.getMessage());
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw e;
            } catch (Exception e) {
                last = e;
                if (!isTransient(e) || attempt >= maxAttempts) {
                    throw new IllegalStateException("Edge TTS 失败: " + e.getMessage(), e);
                }
                log.warn("Edge TTS transient failure attempt={}/{} chars={} err={}",
                    attempt, maxAttempts, text.length(), e.toString());
                sleepBeforeRetry(attempt);
            }
        }
        throw new IllegalStateException(
            "Edge TTS 失败（attempts=" + maxAttempts + ", chars=" + text.length() + ")", last);
    }

    private byte[] synthesizeOnce(String text) throws Exception {
        Path workDir = scriptPath.getParent();
        // 绝对路径输出，避免依赖 edge-tts/outputs；speak.py 对绝对路径直接落盘
        Path outFile = Files.createTempFile("arelore-edge-", ".mp3");
        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(pythonExe);
            cmd.add(scriptPath.toString());
            cmd.add("-t");
            cmd.add(text);
            cmd.add("-o");
            cmd.add(outFile.toAbsolutePath().toString());
            cmd.add("-v");
            cmd.add(voice);
            cmd.add("--bitrate");
            cmd.add(bitrate);
            cmd.add("--sample-rate");
            cmd.add(String.valueOf(sampleRate));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(workDir.toFile());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            ByteArrayOutputStream stdout = new ByteArrayOutputStream();
            Thread gobbler = new Thread(() -> {
                try (InputStream in = process.getInputStream()) {
                    byte[] buf = new byte[4096];
                    int n;
                    while ((n = in.read(buf)) >= 0) {
                        stdout.write(buf, 0, n);
                    }
                } catch (Exception ignored) {
                    // process 结束即可
                }
            }, "edge-tts-stdout-" + UUID.randomUUID());
            gobbler.setDaemon(true);
            gobbler.start();

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                gobbler.join(2000);
                throw new IllegalStateException(
                    "Edge TTS 超时（" + timeoutSeconds + "s） chars=" + text.length());
            }
            gobbler.join(5000);
            int exit = process.exitValue();
            String logOut = stdout.toString(StandardCharsets.UTF_8);
            if (logOut.length() > 500) {
                logOut = logOut.substring(0, 500);
            }
            if (exit != 0) {
                throw new IllegalStateException(
                    "Edge TTS exit=" + exit + (logOut.isEmpty() ? "" : ": " + logOut));
            }
            if (!Files.isRegularFile(outFile) || Files.size(outFile) <= 0) {
                throw new IllegalStateException(
                    "Edge TTS 未生成音频" + (logOut.isEmpty() ? "" : ": " + logOut));
            }
            byte[] data = Files.readAllBytes(outFile);
            log.debug("Edge TTS ok chars={} bytes={} voice={} bitrate={}",
                text.length(), data.length, voice, bitrate);
            return data;
        } finally {
            try {
                Files.deleteIfExists(outFile);
            } catch (Exception ignored) {
                // ignore
            }
        }
    }

    private void sleepBeforeRetry(int attempt) throws InterruptedException {
        Thread.sleep((long) retryDelayMs * attempt);
    }

    private static boolean isTransient(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            String msg = t.getMessage();
            if (msg != null) {
                String lower = msg.toLowerCase();
                if (lower.contains("timeout")
                    || lower.contains("timed out")
                    || lower.contains("connection")
                    || lower.contains("temporarily")
                    || lower.contains("429")
                    || lower.contains("503")) {
                    return true;
                }
            }
        }
        return false;
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
}
