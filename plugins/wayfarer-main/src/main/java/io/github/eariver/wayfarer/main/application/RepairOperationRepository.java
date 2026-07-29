package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.RepairOperation;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking module-private repair repository. Every state claim is an optimistic,
 * transactionally committed compare-and-set and must run through {@code WayfarerTasks.database}.
 */
public interface RepairOperationRepository {
    RepairOperation prepare(
        String idempotencyKey,
        UUID playerUuid,
        UUID toolId,
        long instanceEpoch,
        long amountWaymark,
        Instant now
    );

    Optional<RepairOperation> claimPayment(UUID repairId, long expectedLockVersion, Instant now);

    Optional<RepairOperation> paymentCommitted(
        UUID repairId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    );

    boolean domainCommitted(UUID repairId, long expectedLockVersion, Instant now);

    Optional<RepairOperation> claimRefund(UUID repairId, long expectedLockVersion, Instant now);

    boolean refunded(UUID repairId, long expectedLockVersion, Instant now);

    void unknown(UUID repairId, long expectedLockVersion, String failureCode, Instant now);
}
