package io.github.eariver.wayfarer.api;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerTransactions {
    CompletionStage<TransactionResult> execute(TransactionRequest request);
    CompletionStage<TransactionResult> reconcile(UUID transactionId);
    default CompletionStage<TransactionResult> reconcile(
        UUID transactionId,
        ReconcileAction action
    ) {
        if (action == ReconcileAction.AUTO) {
            return reconcile(transactionId);
        }
        return java.util.concurrent.CompletableFuture.failedFuture(
            new UnsupportedOperationException("Explicit reconciliation is unsupported")
        );
    }
    default CompletionStage<TransactionDetails> inspect(UUID transactionId) {
        return java.util.concurrent.CompletableFuture.failedFuture(
            new UnsupportedOperationException("Transaction inspection is unsupported")
        );
    }

    record TransactionRequest(
        String idempotencyKey,
        String transactionType,
        UUID actorUuid,
        String subjectType,
        String subjectId,
        long amountWaymark,
        String payloadJson
    ) {}

    record TransactionResult(UUID transactionId, State state, String failureCode) {}

    record TransactionDetails(
        UUID transactionId,
        String idempotencyKey,
        String transactionType,
        UUID actorUuid,
        String subjectType,
        String subjectId,
        long amountWaymark,
        State state,
        String providerReference,
        String failureCode,
        long lockVersion
    ) {}

    enum ReconcileAction {
        AUTO,
        COMMIT,
        REFUND,
        FAIL
    }

    enum State {
        PREPARED, DEBIT_PENDING, DEBITED, DOMAIN_COMMIT_PENDING, COMMITTED,
        REFUND_PENDING, REFUNDED, UNKNOWN, RECONCILED_COMMITTED,
        RECONCILED_REFUNDED, FAILED
    }
}
