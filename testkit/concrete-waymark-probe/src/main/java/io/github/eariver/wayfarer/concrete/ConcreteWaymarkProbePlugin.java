package io.github.eariver.wayfarer.concrete;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public final class ConcreteWaymarkProbePlugin extends JavaPlugin {
    private static final UUID PLAYER_UUID =
        UUID.fromString("731ceb9e-0a86-4bab-a9a1-9289bfce5156");
    private static final double SEED_BALANCE = 37.5D;
    private static final long DEBIT_AMOUNT = 25L;
    private static final long INTEROPERABILITY_AMOUNT = 10L;
    private static final int MAX_READINESS_POLLS = 100;

    private BukkitTask readinessTask;
    private int readinessPolls;

    @Override
    public void onEnable() {
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);
        if (registration == null) {
            fail("WAYFARER_SERVICE_UNAVAILABLE");
            return;
        }
        WayfarerServices services = registration.getProvider();
        readinessTask = getServer().getScheduler().runTaskTimer(
            this,
            () -> awaitReady(services),
            1L,
            1L
        );
    }

    @Override
    public void onDisable() {
        if (readinessTask != null) {
            readinessTask.cancel();
        }
        getLogger().info("WAYFARER_CONCRETE_PROVIDER: DISABLED");
    }

    private void awaitReady(WayfarerServices services) {
        readinessPolls++;
        WayfarerHealth.ComponentHealth waymark = services.health()
            .snapshot()
            .components()
            .get("Waymark");
        WayfarerHealth.ComponentHealth transaction = services.health()
            .snapshot()
            .components()
            .get("Transaction");
        if (waymark != null
            && transaction != null
            && waymark.status() == WayfarerHealth.Status.UP
            && transaction.status() == WayfarerHealth.Status.UP) {
            readinessTask.cancel();
            runScenario(services).whenComplete((ignored, failure) ->
                getServer().getScheduler().runTask(this, () -> {
                    if (failure == null) {
                        getLogger().info(
                            "WAYFARER_CONCRETE_PROVIDER: PASS "
                                + "provider=Vault/RedisEconomy "
                                + "balance=shared debit=idempotent refund=accepted"
                        );
                    } else {
                        fail(rootCause(failure).getClass().getSimpleName());
                    }
                })
            );
            return;
        }
        if (readinessPolls >= MAX_READINESS_POLLS) {
            readinessTask.cancel();
            fail("CAPABILITY_NOT_READY");
        }
    }

    private CompletionStage<Void> runScenario(WayfarerServices services) {
        Economy economy = requireEconomy();
        verifySafeHealth(services);
        return vault(() -> {
            require(economy.isEnabled(), "Vault provider enabled");
            require(
                "RedisEconomy".equalsIgnoreCase(economy.getName()),
                "selected Economy provider"
            );
            OfflinePlayer player = getServer().getOfflinePlayer(PLAYER_UUID);
            if (!economy.hasAccount(player)) {
                require(
                    economy.createPlayerAccount(player),
                    "Vault account creation"
                );
            }
            require(economy.hasAccount(player), "Vault account available");
            double current = economy.getBalance(player);
            if (current > 0D) {
                requireSuccess(
                    economy.withdrawPlayer(player, current),
                    "clean initial balance"
                );
            }
            requireSuccess(
                economy.depositPlayer(player, SEED_BALANCE),
                "Vault seed deposit"
            );
            return economy.getBalance(player);
        }).thenCompose(vaultBalance -> {
            requireDecimal("37.5", vaultBalance, "Vault initial balance");
            return services.waymark().balance(PLAYER_UUID);
        }).thenCompose(wayfarerBalance -> {
            requireDecimal("37.5", wayfarerBalance, "Wayfarer initial balance");
            WayfarerTransactions.TransactionRequest request =
                new WayfarerTransactions.TransactionRequest(
                    "concrete-vault-idempotency",
                    "CONCRETE_PROVIDER_TEST",
                    PLAYER_UUID,
                    "RUNTIME_PROBE",
                    "shared-balance",
                    DEBIT_AMOUNT,
                    "{\"test\":\"concrete-provider\"}"
                );
            return services.transactions().execute(request)
                .thenCompose(first -> {
                    require(
                        first.state() == WayfarerTransactions.State.COMMITTED,
                        "Wayfarer debit success"
                    );
                    return services.transactions().execute(request)
                        .thenApply(second -> {
                            require(
                                second.state() == WayfarerTransactions.State.COMMITTED,
                                "idempotent replay state"
                            );
                            require(
                                second.transactionId().equals(first.transactionId()),
                                "idempotent transaction identity"
                            );
                            return second;
                        });
                });
        }).thenCompose(ignored -> services.waymark().balance(PLAYER_UUID))
            .thenCompose(balanceAfterDebit -> {
                require(
                    balanceAfterDebit.compareTo(new BigDecimal("12.5")) == 0,
                    "single Wayfarer debit"
                );
                return vault(() -> {
                    Economy selected = requireEconomy();
                    OfflinePlayer player = getServer().getOfflinePlayer(PLAYER_UUID);
                    require(
                        BigDecimal.valueOf(selected.getBalance(player))
                            .compareTo(balanceAfterDebit) == 0,
                        "Vault observes Wayfarer debit"
                    );
                    requireSuccess(
                        selected.withdrawPlayer(player, INTEROPERABILITY_AMOUNT),
                        "representative Vault consumer withdraw"
                    );
                    return selected.getBalance(player);
                });
            }).thenCompose(vaultBalance -> {
                require(
                    BigDecimal.valueOf(vaultBalance)
                        .compareTo(new BigDecimal("2.5")) == 0,
                    "Vault withdraw balance"
                );
                return services.waymark().balance(PLAYER_UUID);
            }).thenCompose(wayfarerBalance -> {
                require(
                    wayfarerBalance.compareTo(new BigDecimal("2.5")) == 0,
                    "Wayfarer observes Vault withdraw"
                );
                return vault(() -> {
                    Economy selected = requireEconomy();
                    OfflinePlayer player = getServer().getOfflinePlayer(PLAYER_UUID);
                    requireSuccess(
                        selected.depositPlayer(player, INTEROPERABILITY_AMOUNT),
                        "representative Vault consumer deposit"
                    );
                    return selected.getBalance(player);
                });
            }).thenCompose(ignored -> services.waymark().balance(PLAYER_UUID))
            .thenCompose(restoredInteroperability -> {
                require(
                    restoredInteroperability.compareTo(new BigDecimal("12.5")) == 0,
                    "shared balance restored after Vault deposit"
                );
                WayfarerTransactions.TransactionRequest insufficient =
                    new WayfarerTransactions.TransactionRequest(
                        "concrete-vault-insufficient",
                        "CONCRETE_PROVIDER_TEST",
                        PLAYER_UUID,
                        "RUNTIME_PROBE",
                        "insufficient",
                        375L,
                        "{\"test\":\"insufficient\"}"
                    );
                return services.transactions().execute(insufficient);
            }).thenCompose(insufficient -> {
                require(
                    insufficient.state() == WayfarerTransactions.State.FAILED,
                    "insufficient funds state"
                );
                require(
                    "INSUFFICIENT_FUNDS".equals(insufficient.failureCode()),
                    "insufficient funds code"
                );
                return services.waymark().refund(
                    PLAYER_UUID,
                    DEBIT_AMOUNT,
                    "concrete-vault-refund"
                );
            }).thenCompose(refund -> {
                require(refund.success(), "Wayfarer refund success");
                require(refund.providerReference() == null, "no synthetic provider reference");
                return vault(() -> {
                    Economy selected = requireEconomy();
                    return selected.getBalance(
                        getServer().getOfflinePlayer(PLAYER_UUID)
                    );
                });
            }).thenAccept(finalBalance ->
                requireDecimal(
                    "37.5",
                    finalBalance,
                    "Vault observes Wayfarer refund"
                )
            );
    }

    private void verifySafeHealth(WayfarerServices services) {
        WayfarerHealth.ComponentHealth waymark = services.health()
            .snapshot()
            .components()
            .get("Waymark");
        require(waymark != null, "Waymark health");
        require(
            "Provider Vault/RedisEconomy available".equals(waymark.detail()),
            "safe provider health identity"
        );
        String health = services.health().snapshot().toString()
            .toLowerCase(Locale.ROOT);
        require(!health.contains("password"), "health password redaction");
        require(!health.contains("redis://"), "health URI redaction");
        require(!health.contains("dev.unnm3d"), "health raw provider redaction");
        require(!health.contains("@"), "health object reference redaction");
    }

    private Economy requireEconomy() {
        RegisteredServiceProvider<Economy> registration =
            getServer().getServicesManager().getRegistration(Economy.class);
        if (registration == null) {
            throw new IllegalStateException("Vault Economy service unavailable");
        }
        return registration.getProvider();
    }

    private <T> CompletableFuture<T> vault(Supplier<T> operation) {
        CompletableFuture<T> result = new CompletableFuture<>();
        getServer().getScheduler().runTask(this, () -> {
            try {
                result.complete(operation.get());
            } catch (RuntimeException failure) {
                result.completeExceptionally(failure);
            }
        });
        return result;
    }

    private static void requireSuccess(EconomyResponse response, String checkpoint) {
        require(response != null && response.transactionSuccess(), checkpoint);
    }

    private static void requireDecimal(
        String expected,
        BigDecimal actual,
        String checkpoint
    ) {
        require(new BigDecimal(expected).compareTo(actual) == 0, checkpoint);
    }

    private static void requireDecimal(
        String expected,
        double actual,
        String checkpoint
    ) {
        requireDecimal(expected, BigDecimal.valueOf(actual), checkpoint);
    }

    private void fail(String code) {
        getLogger().severe("WAYFARER_CONCRETE_PROVIDER: FAIL " + code);
    }

    private static void require(boolean condition, String checkpoint) {
        if (!condition) {
            throw new IllegalStateException("Probe checkpoint failed: " + checkpoint);
        }
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
