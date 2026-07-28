package io.github.eariver.wayfarer.core.redis;

import java.time.Instant;
import java.util.UUID;

record RedisEnvelope(
    int version,
    UUID messageId,
    String originServerId,
    String messageType,
    String payload,
    Instant occurredAt
) {}
