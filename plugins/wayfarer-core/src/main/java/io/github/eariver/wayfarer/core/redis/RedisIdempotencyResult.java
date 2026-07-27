package io.github.eariver.wayfarer.core.redis;

enum RedisIdempotencyResult {
    NEW_HINT,
    DUPLICATE_HINT
}
