package io.github.eariver.wayfarer.core.identity;

import java.time.Instant;
import java.util.UUID;

public record PlayerIdentityObservation(
    UUID playerUuid,
    String lastKnownName,
    String serverId,
    Instant observedAt
) {}
