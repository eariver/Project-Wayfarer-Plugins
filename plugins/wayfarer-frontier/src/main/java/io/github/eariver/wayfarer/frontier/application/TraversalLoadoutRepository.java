package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Blocking module-private loadout repository. Calls must run through
 * {@code WayfarerTasks.database}.
 */
public interface TraversalLoadoutRepository {
    TraversalLoadout findOrCreate(UUID playerUuid, Instant now);

    List<PendingDelivery> ensureInitialDeliveries(TraversalLoadout loadout, Instant now);

    boolean markDelivered(UUID deliveryId, Instant now);
}
