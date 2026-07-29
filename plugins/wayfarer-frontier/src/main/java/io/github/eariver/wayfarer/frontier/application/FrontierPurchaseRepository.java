package io.github.eariver.wayfarer.frontier.application;

import io.github.eariver.wayfarer.frontier.domain.FrontierShopCatalog;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking module-private order repository. Calls must run through
 * {@code WayfarerTasks.database}.
 */
public interface FrontierPurchaseRepository {
    Purchase prepare(
        String idempotencyKey,
        UUID playerUuid,
        FrontierShopCatalog.Offer offer,
        Instant now
    );

    Optional<Purchase> claimPayment(
        UUID purchaseId,
        long expectedLockVersion,
        Instant now
    );

    boolean markPaymentCommitted(
        UUID purchaseId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    );

    boolean markFailed(
        UUID purchaseId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    );

    void markUnknown(
        UUID purchaseId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    );

    Optional<Purchase> find(UUID purchaseId);

    record Purchase(
        UUID purchaseId,
        String idempotencyKey,
        UUID playerUuid,
        FrontierShopCatalog.Offer offer,
        State state,
        UUID transactionId,
        long lockVersion
    ) {}

    enum State {
        PREPARED,
        PAYMENT_PENDING,
        PAYMENT_COMMITTED,
        DELIVERED,
        UNKNOWN,
        FAILED
    }
}
