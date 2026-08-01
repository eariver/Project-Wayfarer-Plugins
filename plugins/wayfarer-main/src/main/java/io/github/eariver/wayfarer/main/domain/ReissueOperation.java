package io.github.eariver.wayfarer.main.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Durable state for a paid Growth Tool reissue. */
public record ReissueOperation(
    UUID reissueId,
    String idempotencyKey,
    UUID playerUuid,
    UUID toolId,
    UUID expectedItemInstanceId,
    UUID newItemInstanceId,
    long instanceEpoch,
    int evolutionCount,
    String configRevision,
    long amountWaymark,
    State state,
    UUID transactionId,
    Instant paymentCommittedAt,
    String failureCode,
    long lockVersion
) {
    public ReissueOperation {
        Objects.requireNonNull(reissueId, "reissueId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(expectedItemInstanceId, "expectedItemInstanceId");
        Objects.requireNonNull(newItemInstanceId, "newItemInstanceId");
        Objects.requireNonNull(configRevision, "configRevision");
        Objects.requireNonNull(state, "state");
        if (!idempotencyKey.matches("[A-Za-z0-9:_-]{8,191}")
            || instanceEpoch < 1
            || evolutionCount < 0
            || amountWaymark <= 0
            || lockVersion < 0
            || configRevision.isBlank()
            || configRevision.length() > 64) {
            throw new IllegalArgumentException("Reissue operation is invalid");
        }
        if (failureCode != null && !failureCode.matches("[A-Z0-9_]{3,96}")) {
            throw new IllegalArgumentException("Failure code is invalid");
        }
        validatePaymentInvariant(state, transactionId, paymentCommittedAt);
    }

    private static void validatePaymentInvariant(
        State state,
        UUID transactionId,
        Instant paymentCommittedAt
    ) {
        switch (state) {
            case PREPARED, PAYMENT_PENDING, ABANDONED -> {
                if (transactionId != null || paymentCommittedAt != null) {
                    throw new IllegalArgumentException(
                        "Unpaid reissue state cannot have payment evidence"
                    );
                }
            }
            case PAYMENT_COMMITTED, PENDING_DELIVERY, DELIVERED -> {
                if (transactionId == null || paymentCommittedAt == null) {
                    throw new IllegalArgumentException(
                        "Paid reissue state requires payment evidence"
                    );
                }
            }
            case FAILED -> {
                if (paymentCommittedAt != null) {
                    throw new IllegalArgumentException(
                        "Failed reissue cannot have a payment marker"
                    );
                }
            }
            case UNKNOWN -> {
                if (paymentCommittedAt != null && transactionId == null) {
                    throw new IllegalArgumentException(
                        "Payment marker requires a transaction id"
                    );
                }
            }
        }
    }

    public boolean paymentCommitted() {
        return paymentCommittedAt != null && transactionId != null;
    }

    public boolean activeGuardRequired() {
        return switch (state) {
            case PREPARED, PAYMENT_PENDING, PAYMENT_COMMITTED, UNKNOWN -> true;
            case PENDING_DELIVERY, DELIVERED, FAILED, ABANDONED -> false;
        };
    }

    public enum State {
        PREPARED,
        PAYMENT_PENDING,
        PAYMENT_COMMITTED,
        PENDING_DELIVERY,
        DELIVERED,
        FAILED,
        ABANDONED,
        UNKNOWN
    }
}
