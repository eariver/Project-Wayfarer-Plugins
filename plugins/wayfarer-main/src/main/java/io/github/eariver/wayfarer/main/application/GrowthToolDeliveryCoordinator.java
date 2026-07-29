package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class GrowthToolDeliveryCoordinator {
    private final GrowthToolRepository repository;
    private final WayfarerTasks tasks;
    private final WayfarerAudit audit;
    private final DeliveryGateway gateway;
    private final String serverId;
    private final Clock clock;

    public GrowthToolDeliveryCoordinator(
        GrowthToolRepository repository,
        WayfarerTasks tasks,
        WayfarerAudit audit,
        DeliveryGateway gateway,
        String serverId,
        Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CompletionStage<Outcome> onJoin(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        return tasks.database(() -> repository.findOrCreate(playerUuid, clock.instant()))
            .thenCompose(tool -> {
                if (tool.deliveryStatus() == GrowthTool.DeliveryStatus.DELIVERED) {
                    return CompletableFuture.completedFuture(Outcome.ALREADY_DELIVERED);
                }
                DeliveryCapture capture = new DeliveryCapture(tool);
                return tasks.mainThread(() -> capture.outcome = gateway.deliverIfEligible(tool))
                    .thenCompose(ignored -> completeDelivery(capture));
            }).exceptionally(ignored -> Outcome.UNAVAILABLE);
    }

    private CompletionStage<Outcome> completeDelivery(DeliveryCapture capture) {
        Outcome outcome = capture.outcome;
        if (outcome != Outcome.DELIVERED && outcome != Outcome.ALREADY_PRESENT) {
            return record(capture.tool, "GROWTH_TOOL_DELIVERY_PENDING", outcome.name())
                .handle((ignored, failure) -> outcome);
        }
        return tasks.database(() -> repository.markDelivered(
            capture.tool.toolId(),
            capture.tool.lockVersion(),
            clock.instant()
        )).thenCompose(marked -> {
            Outcome persisted = marked ? outcome : Outcome.CONFLICT;
            String event = marked
                ? "GROWTH_TOOL_DELIVERED"
                : "GROWTH_TOOL_DELIVERY_CONFLICT";
            return record(capture.tool, event, persisted.name())
                .handle((ignored, failure) -> persisted);
        });
    }

    private CompletionStage<Void> record(GrowthTool tool, String type, String result) {
        return audit.record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            type,
            tool.ownerUuid(),
            "GROWTH_TOOL",
            tool.toolId().toString(),
            serverId,
            "{\"result\":\"" + result + "\"}",
            clock.instant()
        ));
    }

    public interface DeliveryGateway {
        /**
         * Runs on the main thread. It must recheck online state, world/backend eligibility,
         * inventory capacity, and an already-present canonical item before mutation.
         */
        Outcome deliverIfEligible(GrowthTool tool);
    }

    public enum Outcome {
        DELIVERED,
        ALREADY_PRESENT,
        ALREADY_DELIVERED,
        INVENTORY_FULL,
        PLAYER_OFFLINE,
        WRONG_BACKEND,
        CONFLICT,
        UNAVAILABLE
    }

    private static final class DeliveryCapture {
        private final GrowthTool tool;
        private Outcome outcome = Outcome.UNAVAILABLE;

        private DeliveryCapture(GrowthTool tool) {
            this.tool = tool;
        }
    }
}
