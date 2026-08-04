package io.github.eariver.wayfarer.main.domain;

import java.util.Objects;
import java.util.UUID;

public record RepairOperation(
    UUID repairId,
    String idempotencyKey,
    UUID playerUuid,
    UUID toolId,
    long instanceEpoch,
    long amountWaymark,
    State state,
    UUID transactionId,
    String refundOperationId,
    String failureCode,
    long lockVersion
) {
    public RepairOperation(
        UUID repairId,
        String idempotencyKey,
        UUID playerUuid,
        UUID toolId,
        long instanceEpoch,
        long amountWaymark,
        State state,
        UUID transactionId,
        String refundOperationId,
        long lockVersion
    ) {
        this(
            repairId,
            idempotencyKey,
            playerUuid,
            toolId,
            instanceEpoch,
            amountWaymark,
            state,
            transactionId,
            refundOperationId,
            null,
            lockVersion
        );
    }

    public RepairOperation {
        Objects.requireNonNull(repairId, "repairId");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(state, "state");
        if (!idempotencyKey.matches("[A-Za-z0-9:_-]{8,191}") || instanceEpoch < 1
            || amountWaymark <= 0 || lockVersion < 0) {
            throw new IllegalArgumentException("Repair operation is invalid");
        }
        if (refundOperationId != null
            && !refundOperationId.matches("[A-Za-z0-9:_-]{8,191}")) {
            throw new IllegalArgumentException("Refund operation id is invalid");
        }
        if (failureCode != null && !failureCode.matches("[A-Z0-9_]{3,96}")) {
            throw new IllegalArgumentException("Failure code is invalid");
        }
    }

    public enum State {
        PREPARED,
        PAYMENT_PENDING,
        PAYMENT_COMMITTED,
        DOMAIN_COMMITTED,
        REFUND_PENDING,
        REFUNDED,
        UNKNOWN,
        FAILED
    }
}
