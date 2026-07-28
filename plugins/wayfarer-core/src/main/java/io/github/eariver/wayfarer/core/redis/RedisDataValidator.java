package io.github.eariver.wayfarer.core.redis;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

final class RedisDataValidator {
    private static final Pattern KEY_PART = Pattern.compile("[A-Za-z0-9._-]{1,96}");
    private static final Pattern FORBIDDEN = Pattern.compile(
        "(?i)(?:jdbc:[a-z0-9]+:|rediss?://|authorization\\s*[:=]|bearer\\s+"
            + "|password\\s*[:=]|token\\s*[:=]|secret\\s*[:=])"
    );

    private RedisDataValidator() {}

    static String keyPart(String value, String field) {
        if (value == null || !KEY_PART.matcher(value).matches()) {
            throw new RedisRuntimeException(field + " is invalid");
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.contains("password")
            || normalized.contains("authorization")
            || normalized.contains("credential")
            || normalized.contains("secret")
            || normalized.contains("token")) {
            throw new RedisRuntimeException(field + " is sensitive");
        }
        return value;
    }

    static String safePayload(String value, int maximumBytes, String field) {
        if (value == null
            || value.getBytes(StandardCharsets.UTF_8).length > maximumBytes
            || value.indexOf('\r') >= 0
            || value.indexOf('\n') >= 0
            || FORBIDDEN.matcher(value).find()) {
            throw new RedisRuntimeException(field + " is invalid");
        }
        return value;
    }
}
