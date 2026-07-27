package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerTransactions;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface TransactionRepository {
    CompletionStage<TransactionRecord> prepare(
        UUID transactionId,
        WayfarerTransactions.TransactionRequest request,
        String providerOperationId,
        Instant now
    );

    CompletionStage<Optional<TransactionRecord>> find(UUID transactionId);

    CompletionStage<Optional<TransactionRecord>> transition(
        TransactionRecord current,
        WayfarerTransactions.State next,
        String providerReference,
        String failureCode,
        Instant now
    );
}
