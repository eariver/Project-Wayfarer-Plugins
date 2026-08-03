package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class GrowthToolDeliveryCoordinator {
    private final GrowthToolRepository repository;
    private final WayfarerTasks tasks;
    private final WayfarerAudit audit;
    private final DeliveryGateway gateway;
    private final String serverId;
    private final Clock clock;
    private final DiagnosticSink diagnostics;
    private final ConcurrentHashMap<UUID, FailureSnapshot> lastFailures =
        new ConcurrentHashMap<>();

    public GrowthToolDeliveryCoordinator(
        GrowthToolRepository repository,
        WayfarerTasks tasks,
        WayfarerAudit audit,
        DeliveryGateway gateway,
        String serverId,
        Clock clock
    ) {
        this(
            repository,
            tasks,
            audit,
            gateway,
            serverId,
            clock,
            DiagnosticSink.noop()
        );
    }

    public GrowthToolDeliveryCoordinator(
        GrowthToolRepository repository,
        WayfarerTasks tasks,
        WayfarerAudit audit,
        DeliveryGateway gateway,
        String serverId,
        Clock clock,
        DiagnosticSink diagnostics
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.audit = Objects.requireNonNull(audit, "audit");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public CompletionStage<Outcome> onJoin(UUID playerUuid) {
        return onJoin(playerUuid, ignored -> true);
    }

    public CompletionStage<Outcome> onJoin(
        UUID playerUuid,
        Predicate<GrowthTool> deliveryAdmission
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(deliveryAdmission, "deliveryAdmission");
        lastFailures.remove(playerUuid);
        Attempt attempt = new Attempt();
        return tasks.database(() -> repository.findOrCreate(playerUuid, clock.instant()))
            .thenCompose(tool -> {
                if (tool.deliveryStatus() == GrowthTool.DeliveryStatus.DELIVERED) {
                    lastFailures.remove(playerUuid);
                    return CompletableFuture.completedFuture(Outcome.ALREADY_DELIVERED);
                }
                DeliveryCapture capture = new DeliveryCapture(tool);
                attempt.stage = DiagnosticStage.MAIN_THREAD_DELIVERY_GATE;
                return tasks.mainThread(() -> {
                    if (!deliveryAdmission.test(tool)) {
                        capture.outcome = Outcome.SUPERSEDED;
                        return;
                    }
                    capture.outcome = gateway.deliverIfEligible(tool);
                })
                    .thenCompose(ignored -> completeDelivery(capture, attempt));
            }).exceptionally(failure -> {
                Throwable cause = unwrap(failure);
                DiagnosticStage stage = cause instanceof DeliveryStageException staged
                    ? staged.stage()
                    : attempt.stage;
                FailureSnapshot snapshot = new FailureSnapshot(
                    attempt.correlationId,
                    stage,
                    cause.getClass().getName()
                );
                lastFailures.put(playerUuid, snapshot);
                diagnostics.failure(
                    playerUuid,
                    snapshot.correlationId(),
                    snapshot.stage(),
                    cause
                );
                return Outcome.UNAVAILABLE;
            });
    }

    public Optional<FailureSnapshot> lastFailure(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        return Optional.ofNullable(lastFailures.get(playerUuid));
    }

    private CompletionStage<Outcome> completeDelivery(
        DeliveryCapture capture,
        Attempt attempt
    ) {
        Outcome outcome = capture.outcome;
        if (outcome == Outcome.SUPERSEDED) {
            return CompletableFuture.completedFuture(Outcome.SUPERSEDED);
        }
        if (outcome != Outcome.DELIVERED && outcome != Outcome.ALREADY_PRESENT) {
            attempt.stage = DiagnosticStage.AUDIT_RESULT;
            return record(capture.tool, "GROWTH_TOOL_DELIVERY_PENDING", outcome.name())
                .handle((ignored, failure) -> outcome);
        }
        attempt.stage = DiagnosticStage.MARK_DELIVERED;
        return tasks.database(() -> repository.markDelivered(
            capture.tool.toolId(),
            capture.tool.lockVersion(),
            clock.instant()
        )).thenCompose(marked -> {
            Outcome persisted = marked ? outcome : Outcome.CONFLICT;
            String event = marked
                ? "GROWTH_TOOL_DELIVERED"
                : "GROWTH_TOOL_DELIVERY_CONFLICT";
            attempt.stage = DiagnosticStage.AUDIT_RESULT;
            return record(capture.tool, event, persisted.name())
                .handle((ignored, failure) -> {
                    lastFailures.remove(capture.tool.ownerUuid());
                    return persisted;
                });
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
        SUPERSEDED,
        UNAVAILABLE
    }

    public enum DiagnosticStage {
        FIND_OR_CREATE_AUTHORITY,
        MAIN_THREAD_DELIVERY_GATE,
        CREATE_AND_ANNOTATE_ITEM,
        INSERT_PHYSICAL_ITEM,
        MARK_DELIVERED,
        AUDIT_RESULT,
        SESSION_REFRESH,
        UNKNOWN
    }

    public record FailureSnapshot(
        String correlationId,
        DiagnosticStage stage,
        String exceptionClass
    ) {}

    public static final class DeliveryStageException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final DiagnosticStage stage;

        public DeliveryStageException(
            DiagnosticStage stage,
            Throwable cause
        ) {
            super(cause);
            this.stage = Objects.requireNonNull(stage, "stage");
        }

        public DiagnosticStage stage() {
            return stage;
        }
    }

    @FunctionalInterface
    public interface DiagnosticSink {
        void failure(
            UUID playerUuid,
            String correlationId,
            DiagnosticStage stage,
            Throwable failure
        );

        static DiagnosticSink noop() {
            return (playerUuid, correlationId, stage, failure) -> { };
        }
    }

    private static final class DeliveryCapture {
        private final GrowthTool tool;
        private Outcome outcome = Outcome.UNAVAILABLE;

        private DeliveryCapture(GrowthTool tool) {
            this.tool = tool;
        }
    }

    private static final class Attempt {
        private final String correlationId = UUID.randomUUID().toString();
        private volatile DiagnosticStage stage =
            DiagnosticStage.FIND_OR_CREATE_AUTHORITY;
    }

    private static Throwable unwrap(Throwable failure) {
        Throwable current = failure;
        while ((current instanceof java.util.concurrent.CompletionException
            || current instanceof java.util.concurrent.ExecutionException)
            && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
