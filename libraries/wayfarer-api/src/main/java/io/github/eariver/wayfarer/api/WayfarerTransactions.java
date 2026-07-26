package io.github.eariver.wayfarer.api;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerTransactions {
    CompletionStage<TransactionResult> execute(TransactionRequest request);
    CompletionStage<TransactionResult> reconcile(UUID transactionId);

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

    enum State {
        PREPARED, DEBIT_PENDING, DEBITED, DOMAIN_COMMIT_PENDING, COMMITTED,
        REFUND_PENDING, REFUNDED, UNKNOWN, RECONCILED_COMMITTED,
        RECONCILED_REFUNDED, FAILED
    }
}
