package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class LaunchpadUseCoordinator {
    private final LaunchpadRepository repository;
    private final WayfarerTasks tasks;
    private final LaunchGateway gateway;
    private final Clock clock;
    private final Duration claimDuration;
    private final Duration expiryAfterUse;
    private final boolean extendExpiration;

    public LaunchpadUseCoordinator(
        LaunchpadRepository repository,
        WayfarerTasks tasks,
        LaunchGateway gateway,
        Clock clock,
        Duration claimDuration,
        Duration expiryAfterUse
    ) {
        this(
            repository,
            tasks,
            gateway,
            clock,
            claimDuration,
            expiryAfterUse,
            true
        );
    }

    public LaunchpadUseCoordinator(
        LaunchpadRepository repository,
        WayfarerTasks tasks,
        LaunchGateway gateway,
        Clock clock,
        Duration claimDuration,
        Duration expiryAfterUse,
        boolean extendExpiration
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.claimDuration = positive(claimDuration);
        this.expiryAfterUse = positive(expiryAfterUse);
        this.extendExpiration = extendExpiration;
    }

    public CompletionStage<Result> use(Request request) {
        Objects.requireNonNull(request, "request");
        if (!"frontier_iris".equals(request.exactWorldName())) {
            return CompletableFuture.completedFuture(Result.of(Launchpad.Outcome.UNAVAILABLE));
        }
        return tasks.database(() -> repository.claimForUse(
            request.launchpadId(),
            clock.instant().plus(claimDuration),
            clock.instant()
        )).thenCompose(claimed -> {
            if (claimed.isEmpty()) {
                return CompletableFuture.completedFuture(
                    Result.of(Launchpad.Outcome.UNAVAILABLE)
                );
            }
            UseCapture capture = new UseCapture(claimed.orElseThrow());
            CompletionStage<Void> stage;
            try {
                stage = tasks.mainThread(() -> {
                boolean safe = gateway.safeToLaunch(request.playerUuid(), capture.original);
                Launchpad.UseResult use = capture.original.use(
                    clock.instant(),
                    request.cooldownUntil(),
                    request.sneaking(),
                    safe,
                    expiryAfterUse,
                    extendExpiration
                );
                capture.updated = use.launchpad();
                capture.outcome = use.outcome();
                if (capture.outcome == Launchpad.Outcome.LAUNCHED) {
                    capture.launchAttempted = true;
                    gateway.launch(request.playerUuid(), capture.updated);
                }
                });
            } catch (RuntimeException failure) {
                return release(capture);
            }
            return stage.handle((ignored, failure) -> failure)
                .thenCompose(failure -> {
                    if (failure == null) {
                        return persist(capture);
                    }
                    return capture.launchAttempted
                        ? markUnknown(capture.original.launchpadId())
                        : release(capture);
                });
        }).exceptionally(ignored -> Result.unknown());
    }

    private CompletionStage<Result> persist(UseCapture capture) {
        if (capture.outcome != Launchpad.Outcome.LAUNCHED) {
            return release(capture);
        }
        return tasks.database(() -> repository.saveAfterUse(
            capture.updated,
            capture.original.lockVersion(),
            clock.instant()
        )).thenCompose(saved -> {
            if (saved) {
                return tasks.mainThread(() ->
                    gateway.afterPersisted(capture.updated)
                ).thenApply(ignored ->
                    Result.of(Launchpad.Outcome.LAUNCHED)
                ).exceptionally(ignored ->
                    Result.reconcile(Launchpad.Outcome.LAUNCHED)
                );
            }
            return markUnknown(capture.original.launchpadId());
        }).exceptionallyCompose(ignored -> markUnknown(capture.original.launchpadId()));
    }

    private CompletionStage<Result> release(UseCapture capture) {
        return tasks.database(() -> repository.releaseUseClaim(
            capture.original.launchpadId(),
            capture.original.lockVersion(),
            clock.instant()
        )).thenApply(released -> released
            ? Result.of(capture.outcome)
            : Result.reconcile(capture.outcome)
        ).exceptionally(ignored -> Result.reconcile(capture.outcome));
    }

    private CompletionStage<Result> markUnknown(UUID launchpadId) {
        return tasks.database(() -> {
            repository.markUnknown(launchpadId, "LAUNCH_EFFECT_PERSISTENCE_UNKNOWN", clock.instant());
            return Result.unknown();
        }).exceptionally(ignored -> Result.unknown());
    }

    private static Duration positive(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        return duration;
    }

    public interface LaunchGateway {
        boolean safeToLaunch(UUID playerUuid, Launchpad launchpad);

        void launch(UUID playerUuid, Launchpad launchpad);

        default void afterPersisted(Launchpad launchpad) {}
    }

    public record Request(
        UUID launchpadId,
        UUID playerUuid,
        String exactWorldName,
        boolean sneaking,
        java.time.Instant cooldownUntil
    ) {}

    public record Result(Launchpad.Outcome outcome, boolean reconciliationRequired) {
        private static Result of(Launchpad.Outcome outcome) {
            return new Result(outcome, false);
        }

        private static Result unknown() {
            return new Result(Launchpad.Outcome.UNAVAILABLE, true);
        }

        private static Result reconcile(Launchpad.Outcome outcome) {
            return new Result(outcome, true);
        }
    }

    private static final class UseCapture {
        private final Launchpad original;
        private Launchpad updated;
        private Launchpad.Outcome outcome = Launchpad.Outcome.UNAVAILABLE;
        private boolean launchAttempted;

        private UseCapture(Launchpad original) {
            this.original = original;
            this.updated = original;
        }
    }
}
