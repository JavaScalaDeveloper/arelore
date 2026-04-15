package com.arelore.server.core.registration.support;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpUtils {
    private ClientIpUtils() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String xff = header(request, "X-Forwarded-For");
        if (!xff.isBlank()) {
            // 取第一个
            int idx = xff.indexOf(',');
            return (idx > 0 ? xff.substring(0, idx) : xff).trim();
        }
        String realIp = header(request, "X-Real-IP");
        if (!realIp.isBlank()) {
            return realIp.trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }

    private static String header(HttpServletRequest request, String name) {
        String v = request.getHeader(name);
        return v == null ? "" : v.trim();
    }
}

