package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking module-private launchpad repository. Calls must run through
 * {@code WayfarerTasks.database}.
 */
public interface LaunchpadRepository {
    Optional<Launchpad> find(UUID launchpadId);

    int countActive(UUID placerUuid, Instant now);

    boolean create(Launchpad launchpad, Instant now);

    default boolean create(
        Launchpad launchpad,
        int maximumActive,
        Instant now
    ) {
        if (maximumActive > 0
            && countActive(launchpad.placerUuid(), now) >= maximumActive) {
            return false;
        }
        return create(launchpad, now);
    }

    default boolean createFromItem(
        Launchpad launchpad,
        UUID itemInstanceId,
        int maximumActive,
        Instant now
    ) {
        return create(launchpad, maximumActive, now);
    }

    default boolean rollbackCreatedPlacement(
        Launchpad launchpad,
        UUID itemInstanceId,
        Instant now
    ) {
        return remove(
            launchpad.launchpadId(),
            launchpad.lockVersion(),
            Launchpad.State.RECONCILED_REMOVED,
            now
        );
    }

    Optional<Launchpad> findAt(Launchpad.Location location);

    default List<Launchpad> findActive(int limit) {
        return List.of();
    }

    Optional<Launchpad> claimForUse(UUID launchpadId, Instant claimUntil, Instant now);

    boolean releaseUseClaim(UUID launchpadId, long expectedLockVersion, Instant now);

    boolean saveAfterUse(Launchpad launchpad, long expectedLockVersion, Instant now);

    void markUnknown(UUID launchpadId, String failureCode, Instant now);

    boolean remove(
        UUID launchpadId,
        long expectedLockVersion,
        Launchpad.State removalState,
        Instant now
    );

    List<Launchpad> findExpirationCandidates(Instant now, int limit);
}
