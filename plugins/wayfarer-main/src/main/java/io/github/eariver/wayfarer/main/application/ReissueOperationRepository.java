package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.ReissueOperation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocking durable port.  Every implementation call must run through
 * {@code WayfarerTasks.database}.
 */
public interface ReissueOperationRepository {
    Optional<ReissueOperation> find(UUID reissueId);

    Optional<ReissueOperation> findByIdempotency(String idempotencyKey);

    Optional<ReissueOperation> findActiveByTool(UUID toolId);

    PrepareOutcome prepare(ReissueOperation operation, Instant now);

    Optional<ReissueOperation> claimPayment(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    );

    Optional<ReissueOperation> paymentCommitted(
        UUID reissueId,
        UUID transactionId,
        long expectedLockVersion,
        Instant now
    );

    Optional<ReissueOperation> unknown(
        UUID reissueId,
        long expectedLockVersion,
        UUID transactionId,
        String failureCode,
        Instant now
    );

    Optional<ReissueOperation> failed(
        UUID reissueId,
        UUID transactionId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    );

    Optional<ReissueOperation> abandoned(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    );

    Optional<ReissueOperation> pendingDelivery(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    );

    boolean delivered(UUID reissueId, long expectedLockVersion, Instant now);

    Optional<ReissueOperation> reopenPayment(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    );

    Optional<ReissueOperation> reopenToPaymentCommitted(
        UUID reissueId,
        long expectedLockVersion,
        Instant now
    );

    Optional<ReissueOperation> confirmPaymentCommittedFromUnknown(
        UUID reissueId,
        UUID expectedTransactionId,
        long expectedLockVersion,
        Instant committedAt,
        Instant now
    );

    Optional<ReissueOperation> failFromUnknown(
        UUID reissueId,
        long expectedLockVersion,
        String failureCode,
        Instant now
    );

    List<ReissueOperation> findRecoveryCandidates();
}
