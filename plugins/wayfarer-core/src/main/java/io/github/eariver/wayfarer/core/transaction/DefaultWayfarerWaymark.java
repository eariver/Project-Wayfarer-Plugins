package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerWaymark;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public final class DefaultWayfarerWaymark implements WayfarerWaymark {
    private final WayfarerWaymarkProvider provider;
    private final ManagedExecutor executor;
    private final Duration timeout;

    public DefaultWayfarerWaymark(
        WayfarerWaymarkProvider provider,
        ManagedExecutor executor,
        Duration timeout
    ) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
    }

    @Override
    public CompletionStage<BigDecimal> balance(UUID playerUuid) {
        UUID checkedPlayerUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        return submit(() -> provider.balance(checkedPlayerUuid));
    }

    @Override
    public CompletionStage<OperationResult> debit(
        UUID playerUuid,
        long amount,
        String reference
    ) {
        validateAmount(amount);
        UUID checkedPlayerUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        String checkedReference = Objects.requireNonNull(reference, "reference");
        return submit(() -> provider.debit(
            checkedPlayerUuid,
            amount,
            checkedReference
        ).thenApply(DefaultWayfarerWaymark::map));
    }

    @Override
    public CompletionStage<OperationResult> credit(
        UUID playerUuid,
        long amount,
        String reference
    ) {
        validateAmount(amount);
        UUID checkedPlayerUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        String checkedReference = Objects.requireNonNull(reference, "reference");
        return submit(() -> provider.refund(
            checkedPlayerUuid,
            amount,
            checkedReference,
            checkedReference
        ).thenApply(DefaultWayfarerWaymark::map));
    }

    private <T> CompletionStage<T> submit(
        Supplier<? extends CompletionStage<T>> operation
    ) {
        return executor.submit(() -> {
            try {
                return Objects.requireNonNull(
                    operation.get(),
                    "provider result"
                ).toCompletableFuture()
                    .orTimeout(
                        timeout.toMillis(),
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    .join();
            } catch (RuntimeException failure) {
                throw new IllegalStateException("Waymark provider operation failed");
            }
        });
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
