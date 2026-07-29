package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

final class VaultWaymarkProvider implements WayfarerWaymarkProvider, AutoCloseable {
    private static final double MAX_EXACT_LONG_AS_DOUBLE = 9_007_199_254_740_992D;
    private static final String PROVIDER_DISABLED = "PROVIDER_DISABLED";
    private static final String PROVIDER_FAILURE = "VAULT_FAILURE";
    private static final String PROVIDER_UNKNOWN = "VAULT_RESULT_UNKNOWN";
    private static final String RESOLUTION_UNAVAILABLE = "EFFECT_LOOKUP_UNAVAILABLE";

    private final Economy economy;
    private final MainThreadDispatcher mainThread;
    private final Function<UUID, OfflinePlayer> offlinePlayerResolver;
    private final String expectedProvider;
    private final String safeProviderId;
    private final Consumer<String> warningSink;
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    VaultWaymarkProvider(
        Economy economy,
        MainThreadDispatcher mainThread,
        Function<UUID, OfflinePlayer> offlinePlayerResolver,
        String expectedProvider,
        Consumer<String> warningSink
    ) {
        this.economy = Objects.requireNonNull(economy, "economy");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.offlinePlayerResolver = Objects.requireNonNull(
            offlinePlayerResolver,
            "offlinePlayerResolver"
        );
        this.expectedProvider = Objects.requireNonNull(
            expectedProvider,
            "expectedProvider"
        );
        this.safeProviderId = "Vault/" + expectedProvider;
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    @Override
    public CompletionStage<ProbeResult> probe() {
        if (!accepting.get()) {
            return CompletableFuture.completedFuture(
                new ProbeResult(false, null, PROVIDER_DISABLED)
            );
        }
        return onMainThread(() -> {
            if (!economy.isEnabled()) {
                return new ProbeResult(false, null, PROVIDER_DISABLED);
            }
            String actualProvider = economy.getName();
            if (actualProvider == null
                || !actualProvider.toLowerCase(Locale.ROOT).equals(
                    expectedProvider.toLowerCase(Locale.ROOT)
                )) {
                return new ProbeResult(false, null, "UNEXPECTED_PROVIDER");
            }
            return new ProbeResult(true, safeProviderId, null);
        });
    }

    @Override
    public CompletionStage<BigDecimal> balance(UUID playerUuid) {
        UUID checkedUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        return onMainThread(() -> {
            requireEnabled();
            OfflinePlayer player = resolve(checkedUuid);
            return waymarkBalance(economy.getBalance(player));
        });
    }

    @Override
    public CompletionStage<EffectResult> debit(
        UUID playerUuid,
        long amount,
        String operationId
    ) {
        UUID checkedUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        requireOperation(operationId);
        double vaultAmount = exactVaultAmount(amount);
        if (!accepting.get()) {
            return knownFailure(PROVIDER_DISABLED);
        }
        return onMainThread(() -> {
            if (!economy.isEnabled()) {
                return effect(EffectStatus.KNOWN_FAILURE, PROVIDER_DISABLED);
            }
            OfflinePlayer player = resolve(checkedUuid);
            return mapDebit(economy.withdrawPlayer(player, vaultAmount));
        });
    }

    @Override
    public CompletionStage<EffectResult> refund(
        UUID playerUuid,
        long amount,
        String operationId,
        String debitProviderReference
    ) {
        UUID checkedUuid = Objects.requireNonNull(playerUuid, "playerUuid");
        requireOperation(operationId);
        double vaultAmount = exactVaultAmount(amount);
        if (!accepting.get()) {
            return knownFailure(PROVIDER_DISABLED);
        }
        return onMainThread(() -> {
            if (!economy.isEnabled()) {
                return effect(EffectStatus.KNOWN_FAILURE, PROVIDER_DISABLED);
            }
            OfflinePlayer player = resolve(checkedUuid);
            return mapGeneral(economy.depositPlayer(player, vaultAmount));
        });
    }

    @Override
    public CompletionStage<ResolutionResult> resolve(
        EffectKind effectKind,
        String operationId,
        String providerReference
    ) {
        Objects.requireNonNull(effectKind, "effectKind");
        requireOperation(operationId);
        if (!accepting.get()) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("Vault economy provider is unavailable")
            );
        }
        return CompletableFuture.completedFuture(
            new ResolutionResult(
                ResolutionStatus.UNKNOWN,
                null,
                RESOLUTION_UNAVAILABLE
            )
        );
    }

    @Override
    public void close() {
        accepting.set(false);
    }

    private EffectResult mapDebit(EconomyResponse response) {
        if (response == null) {
            return effect(EffectStatus.UNKNOWN, PROVIDER_UNKNOWN);
        }
        if (response.transactionSuccess()) {
            return effect(EffectStatus.SUCCEEDED, null);
        }
        if (response.type == EconomyResponse.ResponseType.FAILURE
            && "Insufficient funds".equals(response.errorMessage)) {
            return effect(EffectStatus.INSUFFICIENT_FUNDS, "INSUFFICIENT_FUNDS");
        }
        return effect(EffectStatus.KNOWN_FAILURE, PROVIDER_FAILURE);
    }

    private EffectResult mapGeneral(EconomyResponse response) {
        if (response == null) {
            return effect(EffectStatus.UNKNOWN, PROVIDER_UNKNOWN);
        }
        return response.transactionSuccess()
            ? effect(EffectStatus.SUCCEEDED, null)
            : effect(EffectStatus.KNOWN_FAILURE, PROVIDER_FAILURE);
    }

    private <T> CompletableFuture<T> onMainThread(Supplier<T> operation) {
        CompletableFuture<T> result = new CompletableFuture<>();
        if (!accepting.get()) {
            result.completeExceptionally(
                new IllegalStateException("Vault economy provider is unavailable")
            );
            return result;
        }
        try {
            mainThread.dispatch(() -> {
                if (result.isDone()) {
                    return;
                }
                if (!accepting.get()) {
                    result.completeExceptionally(
                        new IllegalStateException("Vault economy provider is unavailable")
                    );
                    return;
                }
                try {
                    result.complete(operation.get());
                } catch (Throwable failure) {
                    warn();
                    result.completeExceptionally(
                        new IllegalStateException("Vault economy operation failed")
                    );
                }
            });
        } catch (RuntimeException failure) {
            warn();
            result.completeExceptionally(
                new IllegalStateException("Vault economy dispatch failed")
            );
        }
        return result;
    }

    private OfflinePlayer resolve(UUID playerUuid) {
        return Objects.requireNonNull(
            offlinePlayerResolver.apply(playerUuid),
            "offline player"
        );
    }

    private void requireEnabled() {
        if (!economy.isEnabled()) {
            throw new IllegalStateException("Vault economy provider is unavailable");
        }
    }

    private static BigDecimal waymarkBalance(double balance) {
        if (!Double.isFinite(balance)) {
            throw new IllegalStateException("Vault balance is unavailable");
        }
        return BigDecimal.valueOf(balance);
    }

    private static double exactVaultAmount(long amount) {
        if (amount <= 0 || amount > (long) MAX_EXACT_LONG_AS_DOUBLE) {
            throw new IllegalArgumentException("Waymark amount is outside the Vault range");
        }
        return (double) amount;
    }

    private static void requireOperation(String operationId) {
        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException("operationId must not be blank");
        }
    }

    private static CompletableFuture<EffectResult> knownFailure(String failureCode) {
        return CompletableFuture.completedFuture(
            effect(EffectStatus.KNOWN_FAILURE, failureCode)
        );
    }

    private static EffectResult effect(EffectStatus status, String failureCode) {
        return new EffectResult(status, null, failureCode);
    }

    private void warn() {
        try {
            warningSink.accept("Vault economy provider operation failed");
        } catch (RuntimeException ignored) {
            // Provider classification must survive unavailable diagnostics.
        }
    }
}
