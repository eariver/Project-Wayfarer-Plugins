package io.github.eariver.wayfarer.core.redis;

import java.time.Instant;
import java.util.UUID;

record RedisLease(String key, UUID ownerToken, Instant expiresAt) {}
