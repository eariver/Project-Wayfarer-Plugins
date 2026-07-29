package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class TraversalDeliveryCoordinator {
    private final FrontierWorldGate worldGate;
    private final TraversalLoadoutRepository repository;
    private final WayfarerTasks tasks;
    private final DeliveryGateway gateway;
    private final Clock clock;

    public TraversalDeliveryCoordinator(
        FrontierWorldGate worldGate,
        TraversalLoadoutRepository repository,
        WayfarerTasks tasks,
        DeliveryGateway gateway,
        Clock clock
    ) {
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CompletionStage<Result> onSafeEntry(UUID playerUuid, String exactWorldName) {
        if (!worldGate.allows(exactWorldName)) {
            return CompletableFuture.completedFuture(new Result(0, 0, true, false));
        }
        return tasks.database(() -> {
            var loadout = repository.findOrCreate(playerUuid, clock.instant());
            return repository.ensureInitialDeliveries(loadout, clock.instant());
        }).thenCompose(deliveries -> deliverSequentially(playerUuid, deliveries))
            .exceptionally(ignored -> new Result(0, 0, false, true));
    }

    private CompletionStage<Result> deliverSequentially(
        UUID playerUuid,
        List<PendingDelivery> deliveries
    ) {
        CompletionStage<Result> result =
            CompletableFuture.completedFuture(new Result(0, 0, false, false));
        for (PendingDelivery delivery : deliveries) {
            result = result.thenCompose(current -> deliverOne(playerUuid, delivery)
                .thenApply(outcome -> current.add(outcome)));
        }
        return result;
    }

    private CompletionStage<DeliveryOutcome> deliverOne(
        UUID playerUuid,
        PendingDelivery delivery
    ) {
        DeliveryCapture capture = new DeliveryCapture();
        return tasks.mainThread(() ->
            capture.outcome = gateway.deliverIfStillEligible(playerUuid, delivery)
        ).thenCompose(ignored -> {
            if (capture.outcome != DeliveryOutcome.DELIVERED
                && capture.outcome != DeliveryOutcome.ALREADY_PRESENT) {
                return CompletableFuture.completedFuture(capture.outcome);
            }
            return tasks.database(() ->
                repository.markDelivered(delivery.deliveryId(), clock.instant())
            ).thenApply(saved -> saved ? capture.outcome : DeliveryOutcome.CONFLICT);
        });
    }

    public interface DeliveryGateway {
        /**
         * Runs on the main thread. It must recheck online state, exact world, capacity, canonical
         * permanent-item identity, and never drop an item.
         */
        DeliveryOutcome deliverIfStillEligible(UUID playerUuid, PendingDelivery delivery);
    }

    public enum DeliveryOutcome {
        DELIVERED,
        ALREADY_PRESENT,
        INVENTORY_FULL,
        PLAYER_OFFLINE,
        LEFT_THEME,
        CAPABILITY_UNAVAILABLE,
        CONFLICT
    }

    public record Result(
        int delivered,
        int pending,
        boolean rejectedWorld,
        boolean unavailable
    ) {
        private Result add(DeliveryOutcome outcome) {
            return switch (outcome) {
                case DELIVERED, ALREADY_PRESENT ->
                    new Result(delivered + 1, pending, rejectedWorld, unavailable);
                default -> new Result(delivered, pending + 1, rejectedWorld, unavailable);
            };
        }
    }

    private static final class DeliveryCapture {
        private DeliveryOutcome outcome = DeliveryOutcome.CONFLICT;
    }
}
