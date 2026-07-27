package io.github.eariver.wayfarer.api;

import java.util.UUID;
import java.util.concurrent.CompletionStage;

/**
 * JDK-only service-provider boundary for an authoritative Waymark economy.
 */
public interface WayfarerWaymarkProvider {
    CompletionStage<ProbeResult> probe();
    CompletionStage<Long> balance(UUID playerUuid);
    CompletionStage<EffectResult> debit(
        UUID playerUuid,
        long amount,
        String operationId
    );
    CompletionStage<EffectResult> refund(
        UUID playerUuid,
        long amount,
        String operationId,
        String debitProviderReference
    );
    CompletionStage<EffectResolution> resolve(
        String operationId,
        String providerReference
    );

    record ProbeResult(boolean available, String providerId, String failureCode) {}

    record EffectResult(
        EffectStatus status,
        String providerReference,
        String failureCode
    ) {}

    enum EffectStatus {
        SUCCEEDED,
        INSUFFICIENT_FUNDS,
        KNOWN_FAILURE,
        UNKNOWN
    }

    enum EffectResolution {
        APPLIED,
        NOT_APPLIED,
        UNKNOWN
    }
}
