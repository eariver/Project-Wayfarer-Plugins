package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LaunchpadUseCoordinatorTest {
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");
    private static final UUID LAUNCHPAD =
        UUID.fromString("00000000-0000-0000-0000-000000000080");
    private static final UUID PLAYER =
        UUID.fromString("00000000-0000-0000-0000-000000000081");

    @Test
    void nonLaunchReleasesClaimSoAnotherUseDoesNotWaitForLease() {
        FakeRepository repository = new FakeRepository();
        LaunchpadUseCoordinator coordinator = coordinator(repository, true, false);

        LaunchpadUseCoordinator.Result first = coordinator.use(request(true))
            .toCompletableFuture().join();
        LaunchpadUseCoordinator.Result second = coordinator.use(request(true))
            .toCompletableFuture().join();

        assertEquals(Launchpad.Outcome.SNEAKING, first.outcome());
        assertEquals(Launchpad.Outcome.SNEAKING, second.outcome());
        assertFalse(first.reconciliationRequired());
        assertEquals(2, repository.claimCalls);
        assertEquals(2, repository.releaseCalls);
    }

    @Test
    void failedReleaseIsExplicitlyReconciledWithoutCallingLaunch() {
        FakeRepository repository = new FakeRepository();
        repository.releaseSucceeds = false;
        LaunchpadUseCoordinator.Result result = coordinator(repository, false, false)
            .use(request(false)).toCompletableFuture().join();

        assertEquals(Launchpad.Outcome.UNSAFE, result.outcome());
        assertTrue(result.reconciliationRequired());
        assertEquals(0, repository.saveCalls);
    }

    @Test
    void effectPersistenceFailureIsUnknownAndClaimIsNotReleased() {
        FakeRepository repository = new FakeRepository();
        repository.saveSucceeds = false;
        LaunchpadUseCoordinator.Result result = coordinator(repository, true, true)
            .use(request(false)).toCompletableFuture().join();

        assertEquals(Launchpad.Outcome.UNAVAILABLE, result.outcome());
        assertTrue(result.reconciliationRequired());
        assertEquals(1, repository.saveCalls);
        assertEquals(0, repository.releaseCalls);
        assertEquals(1, repository.unknownCalls);
    }

    @Test
    void successfulEffectRunsPostPersistenceCleanupExactlyOnce() {
        FakeRepository repository = new FakeRepository();
        int[] persisted = {0};
        LaunchpadUseCoordinator coordinator = new LaunchpadUseCoordinator(
            repository,
            new DirectTasks(),
            new LaunchpadUseCoordinator.LaunchGateway() {
                @Override
                public boolean safeToLaunch(
                    UUID playerUuid,
                    Launchpad launchpad
                ) {
                    return true;
                }

                @Override
                public void launch(UUID playerUuid, Launchpad launchpad) {}

                @Override
                public void afterPersisted(Launchpad launchpad) {
                    persisted[0]++;
                }
            },
            Clock.fixed(NOW, ZoneOffset.UTC),
            Duration.ofSeconds(5),
            Duration.ofDays(30)
        );

        LaunchpadUseCoordinator.Result result = coordinator.use(request(false))
            .toCompletableFuture().join();

        assertEquals(Launchpad.Outcome.LAUNCHED, result.outcome());
        assertFalse(result.reconciliationRequired());
        assertEquals(1, persisted[0]);
    }

    private static LaunchpadUseCoordinator coordinator(
        FakeRepository repository,
        boolean safe,
        boolean launch
    ) {
        return new LaunchpadUseCoordinator(
            repository,
            new DirectTasks(),
            new LaunchpadUseCoordinator.LaunchGateway() {
                @Override
                public boolean safeToLaunch(UUID playerUuid, Launchpad launchpad) {
                    return safe;
                }

                @Override
                public void launch(UUID playerUuid, Launchpad launchpad) {
                    if (!launch) {
                        throw new AssertionError("launch must not be called");
                    }
                }
            },
            Clock.fixed(NOW, ZoneOffset.UTC),
            Duration.ofSeconds(5),
            Duration.ofDays(30)
        );
    }

    private static LaunchpadUseCoordinator.Request request(boolean sneaking) {
        return new LaunchpadUseCoordinator.Request(
            LAUNCHPAD,
            PLAYER,
            "frontier_iris",
            sneaking,
            null
        );
    }

    private static Launchpad launchpad() {
        return new Launchpad(
            LAUNCHPAD,
            new Launchpad.Location("frontier_iris", 1, 64, 1),
            0,
            PLAYER,
            0,
            3,
            NOW.minusSeconds(60),
            null,
            NOW.plus(Duration.ofDays(30)),
            "default",
            Launchpad.State.ACTIVE,
            1,
            1
        );
    }

    private static final class FakeRepository implements LaunchpadRepository {
        private boolean claimed;
        private boolean releaseSucceeds = true;
        private boolean saveSucceeds = true;
        private int claimCalls;
        private int releaseCalls;
        private int saveCalls;
        private int unknownCalls;

        @Override
        public Optional<Launchpad> find(UUID launchpadId) {
            Launchpad current = launchpad();
            return current.launchpadId().equals(launchpadId)
                ? Optional.of(current)
                : Optional.empty();
        }

        @Override
        public int countActive(UUID placerUuid, Instant now) {
            return 1;
        }

        @Override
        public boolean create(Launchpad launchpad, Instant now) {
            return true;
        }

        @Override
        public Optional<Launchpad> findAt(Launchpad.Location location) {
            return Optional.of(launchpad());
        }

        @Override
        public Optional<Launchpad> claimForUse(
            UUID launchpadId,
            Instant claimUntil,
            Instant now
        ) {
            claimCalls++;
            if (claimed) {
                return Optional.empty();
            }
            claimed = true;
            return Optional.of(launchpad());
        }

        @Override
        public boolean releaseUseClaim(
            UUID launchpadId,
            long expectedLockVersion,
            Instant now
        ) {
            releaseCalls++;
            if (releaseSucceeds) {
                claimed = false;
            }
            return releaseSucceeds;
        }

        @Override
        public boolean saveAfterUse(
            Launchpad launchpad,
            long expectedLockVersion,
            Instant now
        ) {
            saveCalls++;
            return saveSucceeds;
        }

        @Override
        public void markUnknown(UUID launchpadId, String failureCode, Instant now) {
            unknownCalls++;
        }

        @Override
        public boolean remove(
            UUID launchpadId,
            long expectedLockVersion,
            Launchpad.State removalState,
            Instant now
        ) {
            return true;
        }

        @Override
        public List<Launchpad> findExpirationCandidates(Instant now, int limit) {
            return List.of();
        }
    }

    private static final class DirectTasks implements WayfarerTasks {
        @Override
        public <T> CompletionStage<T> database(Supplier<T> operation) {
            try {
                return CompletableFuture.completedFuture(operation.get());
            } catch (RuntimeException failure) {
                return CompletableFuture.failedFuture(failure);
            }
        }

        @Override
        public <I, O> CompletionStage<TaskBridgeResult<O>> bridge(
            I immutableRequest,
            Function<? super I, ? extends O> asyncOperation,
            Predicate<? super O> mainThreadRevalidation,
            Consumer<? super O> mainThreadMutation
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompletionStage<Void> mainThread(Runnable operation) {
            operation.run();
            return CompletableFuture.completedFuture(null);
        }
    }
}
