package io.github.eariver.wayfarer.common.secret;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

public final class SecretValue implements AutoCloseable {
    private static final String REDACTED = "[REDACTED]";
    private char[] value;

    private SecretValue(char[] value) {
        this.value = value;
    }

    public static SecretValue of(String value) {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Secret value must not be blank");
        }
        return new SecretValue(value.toCharArray());
    }

    public synchronized <T> T use(Function<char[], T> operation) {
        Objects.requireNonNull(operation, "operation");
        ensureAvailable();
        char[] copy = Arrays.copyOf(value, value.length);
        try {
            return operation.apply(copy);
        } finally {
            Arrays.fill(copy, '\0');
        }
    }

    public synchronized String redact(String text) {
        if (text == null) {
            return "";
        }
        if (value == null || value.length == 0) {
            return text;
        }
        return text.replace(new String(value), REDACTED);
    }

    @Override
    public synchronized void close() {
        if (value != null) {
            Arrays.fill(value, '\0');
            value = null;
        }
    }

    @Override
    public String toString() {
        return REDACTED;
    }

    private void ensureAvailable() {
        if (value == null) {
            throw new IllegalStateException("Secret value is no longer available");
        }
    }
}
