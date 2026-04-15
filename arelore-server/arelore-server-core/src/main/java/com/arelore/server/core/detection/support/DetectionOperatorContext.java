package com.arelore.server.core.detection.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class DetectionOperatorContext {
    private DetectionOperatorContext() {
    }

    public static String getCurrentOperator() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "system";
        }

        HttpServletRequest request = attributes.getRequest();
        String operator = getHeaderValue(request, "X-Current-User");
        if (!operator.isEmpty()) {
            return operator;
        }

        operator = getHeaderValue(request, "X-User");
        if (!operator.isEmpty()) {
            return operator;
        }

        String authorization = getHeaderValue(request, "Authorization");
        if (authorization.startsWith("Bearer ")) {
            String tokenPart = authorization.substring(7).trim();
            if (!tokenPart.isEmpty()) {
                return tokenPart;
            }
        }

        return "system";
    }

    private static String getHeaderValue(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null ? "" : value.trim();
    }
}
