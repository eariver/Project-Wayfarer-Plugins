package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerTransactions;

import java.util.UUID;

public record TransactionRecord(
    UUID transactionId,
    String idempotencyKey,
    String transactionType,
    UUID actorUuid,
    String subjectType,
    String subjectId,
    long amountWaymark,
    WayfarerTransactions.State state,
    String providerReference,
    String providerOperationId,
    String failureCode,
    long lockVersion
) {
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
            providerReference,
            failureCode,
            lockVersion
        );
    }
}
