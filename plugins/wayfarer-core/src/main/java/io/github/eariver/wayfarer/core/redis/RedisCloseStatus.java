package io.github.eariver.wayfarer.core.redis;

enum RedisCloseStatus {
    CLEAN,
    TIMED_OUT,
    INTERRUPTED,
    FAILED
}
