package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking module-private repository contract. Callers must invoke it through
 * {@code WayfarerTasks.database}.
 */
public interface GrowthToolRepository {
    GrowthTool findOrCreate(UUID ownerUuid, Instant now);

    Optional<GrowthTool> findByOwner(UUID ownerUuid);

    boolean markDelivered(UUID toolId, long expectedLockVersion, Instant now);

    Optional<GrowthTool> checkpoint(
        GrowthTool tool,
        long expectedLockVersion,
        Instant now
    );
}
