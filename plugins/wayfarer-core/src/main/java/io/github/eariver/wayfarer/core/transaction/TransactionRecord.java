package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerTransactions;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TransactionRecord(
    UUID transactionId,
    String idempotencyKey,
    String transactionType,
    UUID actorUuid,
    String subjectType,
    String subjectId,
    long amountWaymark,
    String payloadJson,
    WayfarerTransactions.State state,
    String debitOperationId,
    String debitProviderReference,
    String refundOperationId,
    String refundProviderReference,
    WayfarerTransactions.State refundTerminalState,
    String recoveryClaimId,
    Instant recoveryClaimUntil,
    String failureCode,
    long lockVersion
) {
    public boolean sameRequest(WayfarerTransactions.TransactionRequest request) {
        return transactionType.equals(request.transactionType())
            && Objects.equals(actorUuid, request.actorUuid())
            && subjectType.equals(request.subjectType())
            && subjectId.equals(request.subjectId())
            && amountWaymark == request.amountWaymark()
            && Objects.equals(payloadJson, request.payloadJson());
    }

    WayfarerTransactions.TransactionResult result() {
        return new WayfarerTransactions.TransactionResult(
            transactionId,
            state,
            failureCode
        );
    }

    WayfarerTransactions.TransactionDetails details() {
        return new WayfarerTransactions.TransactionDetails(
            transactionId,
            idempotencyKey,
            transactionType,
            actorUuid,
            subjectType,
            subjectId,
            amountWaymark,
            state,
            debitOperationId,
            debitProviderReference,
            refundOperationId,
            refundProviderReference,
            failureCode,
            lockVersion
        );
    }
}
