package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymark;
import io.github.eariver.wayfarer.main.domain.RepairOperation;
import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class RepairCoordinator {
    private final RepairOperationRepository repository;
    private final WayfarerTransactions transactions;
    private final WayfarerWaymark waymark;
    private final WayfarerTasks tasks;
    private final RepairGateway gateway;
    private final Clock clock;

    public RepairCoordinator(
        RepairOperationRepository repository,
        WayfarerTransactions transactions,
        WayfarerWaymark waymark,
        WayfarerTasks tasks,
        RepairGateway gateway,
        Clock clock
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.waymark = Objects.requireNonNull(waymark, "waymark");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.gateway = Objects.requireNonNull(gateway, "gateway");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CompletionStage<Result> repair(Request request) {
        Objects.requireNonNull(request, "request");
        if (!request.idempotencyKey().matches("[A-Za-z0-9:_-]{8,191}")
            || request.instanceEpoch() < 1 || request.amountWaymark() <= 0) {
            return CompletableFuture.completedFuture(Result.failed("INVALID_REPAIR_REQUEST"));
        }
        return tasks.database(() -> repository.prepare(
            request.idempotencyKey(),
            request.playerUuid(),
            request.toolId(),
            request.instanceEpoch(),
            request.amountWaymark(),
            clock.instant()
        )).thenCompose(this::continueOperation)
            .exceptionally(ignored -> Result.unavailable("REPAIR_UNAVAILABLE"));
    }

    private CompletionStage<Result> continueOperation(RepairOperation operation) {
        return switch (operation.state()) {
            case DOMAIN_COMMITTED ->
                CompletableFuture.completedFuture(Result.completed(operation));
            case REFUNDED ->
                CompletableFuture.completedFuture(Result.refunded(operation));
            case FAILED ->
                CompletableFuture.completedFuture(Result.failed(
                    operation,
                    operation.failureCode() == null
                        ? "REPAIR_NOT_APPLIED"
                        : operation.failureCode()
                ));
            case UNKNOWN ->
                CompletableFuture.completedFuture(Result.unknown(operation, "RECONCILE_REQUIRED"));
            case PAYMENT_COMMITTED -> applyRepair(operation);
            case REFUND_PENDING ->
                CompletableFuture.completedFuture(Result.unknown(operation, "REFUND_RECONCILE_REQUIRED"));
            case PAYMENT_PENDING ->
                CompletableFuture.completedFuture(Result.unknown(operation, "PAYMENT_RECONCILE_REQUIRED"));
            case PREPARED -> claimAndPay(operation);
        };
    }

    private CompletionStage<Result> claimAndPay(RepairOperation operation) {
        return tasks.database(() -> repository.claimPayment(
            operation.repairId(),
            operation.lockVersion(),
            clock.instant()
        )).thenCompose(claimed -> claimed
            .map(this::pay)
            .orElseGet(() -> CompletableFuture.completedFuture(
                Result.unknown(operation, "PAYMENT_ALREADY_CLAIMED")
            ))
        );
    }

    private CompletionStage<Result> pay(RepairOperation claimed) {
        WayfarerTransactions.TransactionRequest payment =
            new WayfarerTransactions.TransactionRequest(
                "main-repair:" + claimed.idempotencyKey(),
                "MAIN_TOOL_REPAIR",
                claimed.playerUuid(),
                "GROWTH_TOOL",
                claimed.toolId().toString(),
                claimed.amountWaymark(),
                "{\"repair_id\":\"" + claimed.repairId() + "\"}"
            );
        CompletionStage<WayfarerTransactions.TransactionResult> stage;
        try {
            stage = transactions.execute(payment);
        } catch (RuntimeException failure) {
            return markUnknown(claimed, "TRANSACTION_UNAVAILABLE");
        }
        return stage.handle((result, failure) -> failure == null ? result : null)
            .thenCompose(result -> paymentResult(claimed, result));
    }

    private CompletionStage<Result> paymentResult(
        RepairOperation claimed,
        WayfarerTransactions.TransactionResult transaction
    ) {
        if (transaction == null || transaction.state() == WayfarerTransactions.State.UNKNOWN) {
            return markUnknown(claimed, "PAYMENT_UNKNOWN");
        }
        if (transaction.state() != WayfarerTransactions.State.COMMITTED
            && transaction.state() != WayfarerTransactions.State.RECONCILED_COMMITTED) {
            String failureCode = sanitizedPaymentFailure(transaction.failureCode());
            return tasks.database(() -> repository.failed(
                claimed.repairId(),
                claimed.lockVersion(),
                failureCode,
                clock.instant()
            )).thenApply(failed -> failed
                .map(value -> Result.failed(value, failureCode))
                .orElseGet(() -> Result.unknown(claimed, "PAYMENT_FAILURE_COMMIT_UNKNOWN"))
            ).exceptionally(ignored ->
                Result.unknown(claimed, "PAYMENT_FAILURE_COMMIT_UNKNOWN")
            );
        }
        return tasks.database(() -> repository.paymentCommitted(
            claimed.repairId(),
            transaction.transactionId(),
            claimed.lockVersion(),
            clock.instant()
        )).thenCompose(committed -> committed
            .map(this::applyRepair)
            .orElseGet(() -> markUnknown(claimed, "PAYMENT_COMMIT_UNKNOWN"))
        );
    }

    private CompletionStage<Result> applyRepair(RepairOperation paid) {
        ApplyCapture capture = new ApplyCapture();
        CompletionStage<Void> stage;
        try {
            stage = tasks.mainThread(() ->
                capture.applied = gateway.applyFullRepair(
                    paid.playerUuid(),
                    paid.toolId(),
                    paid.instanceEpoch(),
                    paid.repairId()
                )
            );
        } catch (RuntimeException failure) {
            return markUnknown(paid, "REPAIR_EFFECT_UNKNOWN");
        }
        return stage.thenCompose(ignored -> {
            if (!capture.applied) {
                return claimAndRefund(paid);
            }
            return tasks.database(() -> repository.domainCommitted(
                paid.repairId(),
                paid.lockVersion(),
                clock.instant()
            )).thenCompose(saved -> saved
                ? CompletableFuture.completedFuture(Result.completed(paid))
                : markUnknown(paid, "REPAIR_COMMIT_UNKNOWN")
            );
        }).exceptionallyCompose(ignored -> markUnknown(paid, "REPAIR_EFFECT_UNKNOWN"));
    }

    private CompletionStage<Result> claimAndRefund(RepairOperation paid) {
        return tasks.database(() -> repository.claimRefund(
            paid.repairId(),
            paid.lockVersion(),
            clock.instant()
        )).thenCompose(claimed -> claimed
            .map(this::refund)
            .orElseGet(() -> markUnknown(paid, "REFUND_CLAIM_UNKNOWN"))
        );
    }

    private CompletionStage<Result> refund(RepairOperation claimed) {
        CompletionStage<WayfarerWaymark.OperationResult> stage;
        try {
            stage = waymark.refund(
                claimed.playerUuid(),
                claimed.amountWaymark(),
                claimed.refundOperationId()
            );
        } catch (RuntimeException failure) {
            return markUnknown(claimed, "REFUND_UNKNOWN");
        }
        return stage.handle((result, failure) -> failure == null ? result : null)
            .thenCompose(result -> {
                if (result == null || !result.success()) {
                    return markUnknown(claimed, "REFUND_UNKNOWN");
                }
                return tasks.database(() -> repository.refunded(
                    claimed.repairId(),
                    claimed.lockVersion(),
                    clock.instant()
                )).thenApply(saved -> saved
                    ? Result.refunded(claimed)
                    : Result.unknown(claimed, "REFUND_COMMIT_UNKNOWN")
                );
            });
    }

    private CompletionStage<Result> markUnknown(RepairOperation operation, String failureCode) {
        return tasks.database(() -> {
            repository.unknown(
                operation.repairId(),
                operation.lockVersion(),
                failureCode,
                clock.instant()
            );
            return Result.unknown(operation, failureCode);
        }).exceptionally(ignored -> Result.unknown(operation, failureCode));
    }

    private static String sanitizedPaymentFailure(String failureCode) {
        if (failureCode != null && failureCode.matches("[A-Z0-9_]{3,96}")) {
            return failureCode;
        }
        return "PAYMENT_FAILED";
    }

    public interface RepairGateway {
        /**
         * Runs on the main thread and must revalidate the online player, canonical PDC claim,
         * tool epoch, and repair preview before changing the physical item.
         */
        boolean applyFullRepair(UUID playerUuid, UUID toolId, long instanceEpoch, UUID repairId);
    }

    public record Request(
        String idempotencyKey,
        UUID playerUuid,
        UUID toolId,
        long instanceEpoch,
        long amountWaymark
    ) {}

    public record Result(
        Status status,
        UUID repairId,
        UUID transactionId,
        String failureCode
    ) {
        private static Result completed(RepairOperation operation) {
            return new Result(Status.COMPLETED, operation.repairId(), operation.transactionId(), null);
        }

        private static Result refunded(RepairOperation operation) {
            return new Result(Status.REFUNDED, operation.repairId(), operation.transactionId(), null);
        }

        private static Result failed(String code) {
            return new Result(Status.FAILED, null, null, code);
        }

        private static Result failed(RepairOperation operation, String code) {
            return new Result(
                Status.FAILED,
                operation.repairId(),
                operation.transactionId(),
                code
            );
        }

        private static Result unknown(RepairOperation operation, String code) {
            return new Result(Status.UNKNOWN, operation.repairId(), operation.transactionId(), code);
        }

        private static Result unavailable(String code) {
            return new Result(Status.UNAVAILABLE, null, null, code);
        }
    }

    public enum Status {
        COMPLETED,
        REFUNDED,
        FAILED,
        UNKNOWN,
        UNAVAILABLE
    }

    private static final class ApplyCapture {
        private boolean applied;
    }
}
