package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class FrontierPurchaseCoordinator {
    private final FrontierWorldGate worldGate;
    private final FrontierShopCatalog catalog;
    private final FrontierPurchaseRepository repository;
    private final WayfarerTransactions transactions;
    private final WayfarerTasks tasks;
    private final Clock clock;

    public FrontierPurchaseCoordinator(
        FrontierWorldGate worldGate,
        FrontierShopCatalog catalog,
        FrontierPurchaseRepository repository,
        WayfarerTransactions transactions,
        WayfarerTasks tasks,
        Clock clock
    ) {
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CompletionStage<Result> purchase(Request request) {
        Objects.requireNonNull(request, "request");
        if (!worldGate.allows(request.exactWorldName())) {
            return CompletableFuture.completedFuture(Result.failed("WORLD_UNAVAILABLE"));
        }
        FrontierShopCatalog.Offer offer = catalog.findV002(request.offerId()).orElse(null);
        if (offer == null) {
            return CompletableFuture.completedFuture(Result.failed("OFFER_UNAVAILABLE"));
        }
        if (!request.idempotencyKey().matches("[A-Za-z0-9:_-]{8,191}")) {
            return CompletableFuture.completedFuture(Result.failed("INVALID_REQUEST"));
        }

        return tasks.database(() -> repository.prepare(
            request.idempotencyKey(),
            request.playerUuid(),
            offer,
            clock.instant()
        )).thenCompose(order -> {
            if (order.state() == FrontierPurchaseRepository.State.PAYMENT_COMMITTED
                || order.state() == FrontierPurchaseRepository.State.DELIVERED) {
                return CompletableFuture.completedFuture(
                    new Result(Status.ACCEPTED, order.purchaseId(), order.transactionId(), null)
                );
            }
            if (order.state() == FrontierPurchaseRepository.State.FAILED) {
                return CompletableFuture.completedFuture(
                    new Result(Status.FAILED, order.purchaseId(), order.transactionId(),
                        "PURCHASE_FAILED")
                );
            }
            if (order.state() != FrontierPurchaseRepository.State.PREPARED) {
                return CompletableFuture.completedFuture(
                    new Result(Status.UNKNOWN, order.purchaseId(), order.transactionId(),
                        "PURCHASE_REQUIRES_RECONCILE")
                );
            }
            return claimAndExecutePayment(order);
        }).exceptionally(ignored -> Result.failed("PURCHASE_UNAVAILABLE"));
    }

    private CompletionStage<Result> claimAndExecutePayment(
        FrontierPurchaseRepository.Purchase order
    ) {
        return tasks.database(() -> repository.claimPayment(
            order.purchaseId(),
            order.lockVersion(),
            clock.instant()
        )).thenCompose(claimed -> claimed
            .map(this::executePayment)
            .orElseGet(() -> CompletableFuture.completedFuture(
                new Result(Status.UNKNOWN, order.purchaseId(), order.transactionId(),
                    "PAYMENT_ALREADY_CLAIMED")
            ))
        );
    }

    private CompletionStage<Result> executePayment(FrontierPurchaseRepository.Purchase order) {
        WayfarerTransactions.TransactionRequest payment =
            new WayfarerTransactions.TransactionRequest(
                "frontier-shop:" + order.idempotencyKey(),
                "FRONTIER_SHOP",
                order.playerUuid(),
                "FRONTIER_PURCHASE",
                order.purchaseId().toString(),
                order.offer().priceWaymark(),
                "{\"offer_id\":\"" + order.offer().offerId() + "\"}"
            );
        CompletionStage<WayfarerTransactions.TransactionResult> stage;
        try {
            stage = transactions.execute(payment);
        } catch (RuntimeException failure) {
            return markUnknown(order, "TRANSACTION_UNKNOWN");
        }
        return stage.handle((result, failure) -> failure == null ? result : null)
            .thenCompose(result -> finishPayment(order, result));
    }

    private CompletionStage<Result> finishPayment(
        FrontierPurchaseRepository.Purchase order,
        WayfarerTransactions.TransactionResult transaction
    ) {
        if (transaction == null) {
            return markUnknown(order, "TRANSACTION_UNKNOWN");
        }
        if (transaction.state() == WayfarerTransactions.State.UNKNOWN) {
            return markUnknown(order, "TRANSACTION_UNKNOWN");
        }
        if (transaction.state() != WayfarerTransactions.State.COMMITTED
            && transaction.state() != WayfarerTransactions.State.RECONCILED_COMMITTED) {
            String failureCode = safeFailure(transaction.failureCode());
            return tasks.database(() -> repository.markFailed(
                order.purchaseId(),
                order.lockVersion(),
                failureCode,
                clock.instant()
            )).thenApply(saved -> saved
                ? new Result(Status.FAILED, order.purchaseId(), transaction.transactionId(),
                    failureCode)
                : new Result(Status.UNKNOWN, order.purchaseId(), transaction.transactionId(),
                    "PURCHASE_COMMIT_UNKNOWN")
            );
        }
        return tasks.database(() -> repository.markPaymentCommitted(
            order.purchaseId(),
            transaction.transactionId(),
            order.lockVersion(),
            clock.instant()
        )).thenApply(saved -> saved
            ? new Result(Status.ACCEPTED, order.purchaseId(), transaction.transactionId(), null)
            : new Result(Status.UNKNOWN, order.purchaseId(), transaction.transactionId(),
                "PURCHASE_COMMIT_UNKNOWN")
        );
    }

    private CompletionStage<Result> markUnknown(
        FrontierPurchaseRepository.Purchase order,
        String failureCode
    ) {
        return tasks.database(() -> {
            repository.markUnknown(
                order.purchaseId(),
                order.lockVersion(),
                failureCode,
                clock.instant()
            );
            return new Result(
                Status.UNKNOWN,
                order.purchaseId(),
                order.transactionId(),
                failureCode
            );
        }).exceptionally(ignored -> new Result(
            Status.UNKNOWN,
            order.purchaseId(),
            order.transactionId(),
            failureCode
        ));
    }

    private static String safeFailure(String code) {
        return code != null && code.matches("[A-Z0-9_]{1,96}") ? code : "PURCHASE_FAILED";
    }

    public record Request(
        String idempotencyKey,
        UUID playerUuid,
        String exactWorldName,
        String offerId
    ) {}

    public record Result(Status status, UUID purchaseId, UUID transactionId, String failureCode) {
        private static Result failed(String failureCode) {
            return new Result(Status.FAILED, null, null, failureCode);
        }
    }

    public enum Status {
        ACCEPTED,
        FAILED,
        UNKNOWN,
        UNAVAILABLE
    }
}
