package io.github.eariver.wayfarer.api;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerWaymark {
    CompletionStage<Long> balance(UUID playerUuid);
    CompletionStage<OperationResult> debit(UUID playerUuid, long amount, String reference);
    CompletionStage<OperationResult> credit(UUID playerUuid, long amount, String reference);

    record OperationResult(boolean success, String providerReference, String failureCode) {}
}
