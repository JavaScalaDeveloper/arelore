package com.arelore.server.core.registration.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public final class PasswordHashUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHashUtils() {
    }

    public static String randomSaltBase64Url(int lenBytes) {
        byte[] b = new byte[lenBytes];
        RANDOM.nextBytes(b);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte d : dig) {
                sb.append(String.format("%02x", d));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("hash failed", e);
        }
    }
}

