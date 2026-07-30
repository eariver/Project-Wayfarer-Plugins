package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;

import java.nio.charset.StandardCharsets;
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
    private final DeliveryGateway deliveryGateway;
    private final Clock clock;

    public FrontierPurchaseCoordinator(
        FrontierWorldGate worldGate,
        FrontierShopCatalog catalog,
        FrontierPurchaseRepository repository,
        WayfarerTransactions transactions,
        WayfarerTasks tasks,
        Clock clock
    ) {
        this(
            worldGate,
            catalog,
            repository,
            transactions,
            tasks,
            (purchase, deliveryId) -> false,
            clock
        );
    }

    public FrontierPurchaseCoordinator(
        FrontierWorldGate worldGate,
        FrontierShopCatalog catalog,
        FrontierPurchaseRepository repository,
        WayfarerTransactions transactions,
        WayfarerTasks tasks,
        DeliveryGateway deliveryGateway,
        Clock clock
    ) {
        this.worldGate = Objects.requireNonNull(worldGate, "worldGate");
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.deliveryGateway = Objects.requireNonNull(deliveryGateway, "deliveryGateway");
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
        )).thenCompose(this::continuePurchase)
            .exceptionally(ignored -> Result.failed("PURCHASE_UNAVAILABLE"));
    }

    private CompletionStage<Result> continuePurchase(
        FrontierPurchaseRepository.Purchase purchase
    ) {
        return switch (purchase.state()) {
            case DELIVERED -> CompletableFuture.completedFuture(
                result(Status.DELIVERED, purchase, null)
            );
            case PENDING_DELIVERY, PAYMENT_COMMITTED -> ensureDelivery(purchase);
            case FAILED -> CompletableFuture.completedFuture(
                result(Status.FAILED, purchase, "PURCHASE_FAILED")
            );
            case UNKNOWN, PAYMENT_PENDING -> CompletableFuture.completedFuture(
                result(Status.UNKNOWN, purchase, "PURCHASE_REQUIRES_RECONCILE")
            );
            case PREPARED -> claimAndExecutePayment(purchase);
        };
    }

    private CompletionStage<Result> claimAndExecutePayment(
        FrontierPurchaseRepository.Purchase purchase
    ) {
        return tasks.database(() -> repository.claimPayment(
            purchase.purchaseId(),
            purchase.lockVersion(),
            clock.instant()
        )).thenCompose(claimed -> claimed
            .map(this::executePayment)
            .orElseGet(() -> CompletableFuture.completedFuture(
                result(Status.UNKNOWN, purchase, "PAYMENT_ALREADY_CLAIMED")
            ))
        );
    }

    private CompletionStage<Result> executePayment(
        FrontierPurchaseRepository.Purchase purchase
    ) {
        WayfarerTransactions.TransactionRequest payment =
            new WayfarerTransactions.TransactionRequest(
                "frontier-shop:" + purchase.idempotencyKey(),
                "FRONTIER_SHOP",
                purchase.playerUuid(),
                "FRONTIER_PURCHASE",
                purchase.purchaseId().toString(),
                purchase.offer().priceWaymark(),
                "{\"offer_id\":\"" + purchase.offer().offerId() + "\"}"
            );
        CompletionStage<WayfarerTransactions.TransactionResult> stage;
        try {
            stage = transactions.execute(payment);
        } catch (RuntimeException failure) {
            return markUnknown(purchase, "TRANSACTION_UNKNOWN");
        }
        return stage.handle((result, failure) -> failure == null ? result : null)
            .thenCompose(result -> finishPayment(purchase, result));
    }

    private CompletionStage<Result> finishPayment(
        FrontierPurchaseRepository.Purchase purchase,
        WayfarerTransactions.TransactionResult transaction
    ) {
        if (transaction == null
            || transaction.state() == WayfarerTransactions.State.UNKNOWN) {
            return markUnknown(purchase, "TRANSACTION_UNKNOWN");
        }
        if (transaction.state() != WayfarerTransactions.State.COMMITTED
            && transaction.state() != WayfarerTransactions.State.RECONCILED_COMMITTED) {
            String failureCode = safeFailure(transaction.failureCode());
            return tasks.database(() -> repository.markFailed(
                purchase.purchaseId(),
                purchase.lockVersion(),
                failureCode,
                clock.instant()
            )).thenApply(saved -> saved
                ? result(Status.FAILED, purchase, failureCode)
                : result(Status.UNKNOWN, purchase, "PURCHASE_COMMIT_UNKNOWN")
            );
        }
        return tasks.database(() -> {
            boolean saved = repository.markPaymentCommitted(
                purchase.purchaseId(),
                transaction.transactionId(),
                purchase.lockVersion(),
                clock.instant()
            );
            return saved ? repository.find(purchase.purchaseId()).orElse(null) : null;
        }).thenCompose(committed -> committed == null
            ? CompletableFuture.completedFuture(
                result(Status.UNKNOWN, purchase, "PURCHASE_COMMIT_UNKNOWN")
            )
            : ensureDelivery(committed)
        );
    }

    private CompletionStage<Result> ensureDelivery(
        FrontierPurchaseRepository.Purchase purchase
    ) {
        if (purchase.state() == FrontierPurchaseRepository.State.DELIVERED) {
            return CompletableFuture.completedFuture(result(Status.DELIVERED, purchase, null));
        }
        if (purchase.state() == FrontierPurchaseRepository.State.PAYMENT_COMMITTED) {
            UUID deliveryId = stableDeliveryId(purchase.purchaseId());
            PendingDelivery delivery = new PendingDelivery(
                deliveryId,
                purchase.playerUuid(),
                io.github.eariver.wayfarer.frontier.domain.TraversalIdentity.WORLDS_BEYOND,
                purchase.offer().itemType(),
                purchase.offer().quantity(),
                "frontier-shop-delivery:" + purchase.purchaseId(),
                PendingDelivery.State.PENDING,
                0,
                clock.instant()
            );
            return tasks.database(() -> repository.attachPendingDelivery(
                purchase.purchaseId(),
                purchase.lockVersion(),
                delivery,
                clock.instant()
            )).thenCompose(attached -> attached
                .map(this::attemptDelivery)
                .orElseGet(() -> CompletableFuture.completedFuture(
                    result(Status.UNKNOWN, purchase, "DELIVERY_PREPARE_UNKNOWN")
                ))
            );
        }
        if (purchase.state() == FrontierPurchaseRepository.State.PENDING_DELIVERY) {
            return attemptDelivery(purchase);
        }
        return CompletableFuture.completedFuture(
            result(Status.UNKNOWN, purchase, "DELIVERY_STATE_UNKNOWN")
        );
    }

    private CompletionStage<Result> attemptDelivery(
        FrontierPurchaseRepository.Purchase purchase
    ) {
        DeliveryCapture capture = new DeliveryCapture();
        CompletionStage<Void> stage;
        try {
            stage = tasks.mainThread(() ->
                capture.delivered = deliveryGateway.deliver(
                    purchase,
                    Objects.requireNonNull(purchase.deliveryId(), "deliveryId")
                )
            );
        } catch (RuntimeException failure) {
            return markUnknown(purchase, "DELIVERY_EFFECT_UNKNOWN");
        }
        return stage.handle((ignored, failure) -> failure).thenCompose(failure -> {
            if (failure != null) {
                return markUnknown(purchase, "DELIVERY_EFFECT_UNKNOWN");
            }
            if (!capture.delivered) {
                return CompletableFuture.completedFuture(
                    result(Status.PENDING, purchase, null)
                );
            }
            return tasks.database(() -> repository.markDelivered(
                purchase.purchaseId(),
                purchase.deliveryId(),
                purchase.lockVersion(),
                clock.instant()
            )).thenApply(saved -> saved
                ? result(Status.DELIVERED, purchase, null)
                : result(Status.UNKNOWN, purchase, "DELIVERY_COMMIT_UNKNOWN")
            );
        });
    }

    private CompletionStage<Result> markUnknown(
        FrontierPurchaseRepository.Purchase purchase,
        String failureCode
    ) {
        return tasks.database(() -> {
            repository.markUnknown(
                purchase.purchaseId(),
                purchase.lockVersion(),
                failureCode,
                clock.instant()
            );
            return result(Status.UNKNOWN, purchase, failureCode);
        }).exceptionally(ignored -> result(Status.UNKNOWN, purchase, failureCode));
    }

    private static UUID stableDeliveryId(UUID purchaseId) {
        return UUID.nameUUIDFromBytes(
            ("frontier-delivery:" + purchaseId).getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String safeFailure(String code) {
        return code != null && code.matches("[A-Z0-9_]{1,96}")
            ? code
            : "PURCHASE_FAILED";
    }

    private static Result result(
        Status status,
        FrontierPurchaseRepository.Purchase purchase,
        String failureCode
    ) {
        return new Result(
            status,
            purchase.purchaseId(),
            purchase.transactionId(),
            purchase.deliveryId(),
            failureCode
        );
    }

    public interface DeliveryGateway {
        /**
         * Runs on the main thread. A false result proves that no item was granted;
         * implementations must never drop an item as fallback.
         */
        boolean deliver(FrontierPurchaseRepository.Purchase purchase, UUID deliveryId);
    }

    public record Request(
        String idempotencyKey,
        UUID playerUuid,
        String exactWorldName,
        String offerId
    ) {}

    public record Result(
        Status status,
        UUID purchaseId,
        UUID transactionId,
        UUID deliveryId,
        String failureCode
    ) {
        private static Result failed(String failureCode) {
            return new Result(Status.FAILED, null, null, null, failureCode);
        }
    }

    public enum Status {
        DELIVERED,
        PENDING,
        FAILED,
        UNKNOWN
    }

    private static final class DeliveryCapture {
        private boolean delivered;
    }
}
