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
    boolean create(Launchpad launchpad, Instant now);

    Optional<Launchpad> findAt(Launchpad.Location location);

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
