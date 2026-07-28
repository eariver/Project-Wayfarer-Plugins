package io.github.eariver.wayfarer.core.redis;

record RedisCacheValue(int schemaVersion, String payload) {
    RedisCacheValue {
        if (schemaVersion < 1) {
            throw new RedisRuntimeException("cache schema version is invalid");
        }
        RedisDataValidator.safePayload(payload, 16 * 1024, "cache payload");
    }
}
