package io.github.eariver.wayfarer.core.persistence;

import java.time.Instant;
import java.util.UUID;

record PlayerIdentityRecord(
    UUID playerUuid,
    String lastKnownName,
    Instant firstSeenAt,
    Instant lastSeenAt,
    String lastServerId,
    long lockVersion
) {}
