package io.github.eariver.wayfarer.core.redis;

public final class RedisRuntimeException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public RedisRuntimeException(String message) {
        super(message);
    }
}
