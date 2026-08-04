package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking module-private loadout repository. Calls must run through
 * {@code WayfarerTasks.database}.
 */
public interface TraversalLoadoutRepository {
    Optional<TraversalLoadout> find(UUID playerUuid);

    TraversalLoadout findOrCreate(UUID playerUuid, Instant now);

    List<PendingDelivery> ensureInitialDeliveries(TraversalLoadout loadout, Instant now);

    List<PendingDelivery> pending(UUID playerUuid);

    boolean reissuePermanent(
        UUID playerUuid,
        TraversalIdentity.ItemType itemType,
        Instant now
    );

    DeathPersistResult persistDeathSnapshot(
        DeathIdentitySnapshot snapshot,
        Instant now
    );

    void reopenAbsentPermanents(
        UUID playerUuid,
        EnumSet<TraversalIdentity.ItemType> absentTypes,
        Instant now
    );

    DeliveryCompletion markPermanentDelivered(UUID deliveryId, Instant now);

    DeliveryCompletion markConsumableDelivered(UUID deliveryId, Instant now);
}
