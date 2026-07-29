package io.github.eariver.wayfarer.api;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface WayfarerWaymark {
    /**
     * Reads the provider-authoritative balance without rounding it to an integer.
     */
    CompletionStage<BigDecimal> balance(UUID playerUuid);
    CompletionStage<OperationResult> debit(UUID playerUuid, long amount, String reference);
    CompletionStage<OperationResult> credit(UUID playerUuid, long amount, String reference);
    default CompletionStage<OperationResult> refund(
        UUID playerUuid,
        long amount,
        String reference
    ) {
        return credit(playerUuid, amount, reference);
    }

    record OperationResult(boolean success, String providerReference, String failureCode) {}
}
