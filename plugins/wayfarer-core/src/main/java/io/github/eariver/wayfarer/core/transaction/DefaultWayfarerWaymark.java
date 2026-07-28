package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerWaymark;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class DefaultWayfarerWaymark implements WayfarerWaymark {
    private final WayfarerWaymarkProvider provider;

    public DefaultWayfarerWaymark(WayfarerWaymarkProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
    }

    @Override
    public CompletionStage<Long> balance(UUID playerUuid) {
        return provider.balance(Objects.requireNonNull(playerUuid, "playerUuid"));
    }

    @Override
    public CompletionStage<OperationResult> debit(
        UUID playerUuid,
        long amount,
        String reference
    ) {
        validateAmount(amount);
        return provider.debit(playerUuid, amount, reference).thenApply(DefaultWayfarerWaymark::map);
    }

    @Override
    public CompletionStage<OperationResult> credit(
        UUID playerUuid,
        long amount,
        String reference
    ) {
        validateAmount(amount);
        return provider.refund(playerUuid, amount, reference, reference)
            .thenApply(DefaultWayfarerWaymark::map);
    }

    private static OperationResult map(WayfarerWaymarkProvider.EffectResult result) {
        return new OperationResult(
            result.status() == WayfarerWaymarkProvider.EffectStatus.SUCCEEDED,
            result.providerReference(),
            result.failureCode()
        );
    }

    private static void validateAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Waymark amount must be positive");
        }
    }
}
