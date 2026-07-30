package io.github.eariver.wayfarer.preclient;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class PreclientProbePlugin extends JavaPlugin {
    private static final UUID ACTOR_ID =
        UUID.fromString("12345678-1234-5678-9234-567812345678");
    private static final Path SCENARIO_FILE = Path.of("preclient-scenario.txt");
    private static final Path FIXTURE_MODE_FILE = Path.of("fixture-mode.txt");
    private static final Path EXPECTED_TRANSACTION_FILE =
        Path.of("expected-transaction-id.txt");
    private static final Path EXPECTED_STATE_FILE = Path.of("expected-state.txt");

    @Override
    public void onEnable() {
        getServer().getScheduler().runTask(this, () -> startWhenReady(0));
    }

    private void startWhenReady(int attempt) {
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);
        if (registration == null) {
            fail("SERVICE_LOOKUP");
            return;
        }

        WayfarerServices services = registration.getProvider();
        try {
            String scenario = read(SCENARIO_FILE, "baseline");
            if (!"provider-outage".equals(scenario)
                && !providerServicesReady(services)) {
                if (attempt >= 200) {
                    fail("PROVIDER_STARTUP_TIMEOUT");
                    return;
                }
                getServer().getScheduler().runTaskLater(
                    this,
                    () -> startWhenReady(attempt + 1),
                    1L
                );
                return;
            }
            verifySynchronousBoundaries(services, scenario);
            getLogger().info("WAYFARER_PRECLIENT_PROBE: SERVICE_LOOKUP PASS");
            runScenario(services, scenario).whenComplete((ignored, failure) ->
                getServer().getScheduler().runTask(this, () -> {
                    if (failure == null) {
                        getLogger().info(
                            "WAYFARER_PRECLIENT_PROBE: PASS scenario=" + scenario
                        );
                    } else {
                        fail(rootCause(failure).getClass().getSimpleName());
                    }
                })
            );
        } catch (RuntimeException failure) {
            fail(rootCause(failure).getClass().getSimpleName());
        }
    }

    private static boolean providerServicesReady(WayfarerServices services) {
        try {
            return services.transactions() != null && services.waymark() != null;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("WAYFARER_PRECLIENT_PROBE: DISABLED");
    }

    private void verifySynchronousBoundaries(
        WayfarerServices services,
        String scenario
    ) {
        require("wayfarer-preclient".equals(services.serverId()), "server identity");
        require(services.configVersion() == 1, "config version");
        require(
            services.lifecycleState() == WayfarerLifecycleState.ENABLED,
            "lifecycle state"
        );

        WayfarerHealth.HealthSnapshot health = services.health().snapshot();
        for (String component : Set.of(
            "Config",
            "Executor",
            "MariaDB",
            "Migration",
            "Audit",
            "Identity",
            "Redis",
            "Services",
            "Lifecycle"
        )) {
            WayfarerHealth.ComponentHealth value = health.components().get(component);
            require(value != null, component + " health");
            require(value.status() == WayfarerHealth.Status.UP, component + " status");
        }
        if ("provider-outage".equals(scenario)) {
            requireUnavailable(services::transactions, "Transactions");
            requireUnavailable(services::waymark, "Waymark");
        } else {
            require(services.transactions() != null, "Transactions service");
            require(services.waymark() != null, "Waymark service");
        }
        if ("baseline".equals(scenario)) {
            CoreInternalRuntimeProbe.verifyMainThreadGuards(this);
        }
    }

    private CompletionStage<Void> runScenario(
        WayfarerServices services,
        String scenario
    ) {
        return switch (scenario) {
            case "baseline" -> baseline(services);
            case "provider-outage" -> providerOutage(services);
            case "timeout-before-effect" -> timeoutBeforeEffect(services);
            case "timeout-after-effect" -> timeoutAfterEffect(services);
            case "debit-crash" -> execute(services, scenario).thenAccept(ignored -> {
                throw new IllegalStateException("debit crash was not injected");
            });
            case "refund-crash" -> refundCrash(services);
            case "recovery-verify" -> recoveryVerify(services);
            case "queue-rejection" -> queueRejection(services);
            case "accepted-drain" -> acceptedDrain(services);
            case "shutdown-timeout" -> shutdownTimeout(services);
            default -> CompletableFuture.failedFuture(
                new IllegalArgumentException("Unknown pre-client scenario")
            );
        };
    }

    private CompletionStage<Void> baseline(WayfarerServices services) {
        return CoreInternalRuntimeProbe.verifyPlayerIdentity(this, ACTOR_ID)
            .thenCompose(ignored ->
                CoreInternalRuntimeProbe.verifyRedisPrimitives(this, services)
            )
            .thenCompose(ignored -> auditIdentityAndTaskProbe(services))
            .thenCompose(ignored -> services.waymark().balance(ACTOR_ID))
            .thenAccept(balance -> require(
                balance.compareTo(new BigDecimal("100000")) == 0,
                "Waymark balance"
            ))
            .thenCompose(ignored -> execute(services, "baseline"))
            .thenCompose(result -> {
                require(
                    result.state() == WayfarerTransactions.State.COMMITTED,
                    "successful transaction"
                );
                return services.transactions().inspect(result.transactionId());
            })
            .thenAccept(details -> {
                require(details.debitOperationId() != null, "debit operation ID");
                require(
                    details.debitProviderReference() == null,
                    "Vault must not synthesize a provider reference"
                );
                require(details.refundOperationId() == null, "refund operation absent");
                require(
                    details.refundProviderReference() == null,
                    "refund reference absent"
                );
                getLogger().info(
                    "WAYFARER_PRECLIENT_PROBE: TRANSACTION_SUCCESS id="
                        + details.transactionId()
                );
            });
    }

    private CompletionStage<Void> providerOutage(WayfarerServices services) {
        return auditIdentityAndTaskProbe(services);
    }

    private CompletionStage<Void> timeoutBeforeEffect(WayfarerServices services) {
        return execute(services, "timeout-before").thenCompose(result -> {
            require(result.state() == WayfarerTransactions.State.UNKNOWN, "timeout unknown");
            write(FIXTURE_MODE_FILE, "success");
            return services.transactions().reconcile(result.transactionId());
        }).thenAccept(result -> require(
            result.state() == WayfarerTransactions.State.FAILED,
            "not-applied automatic reconcile"
        ));
    }

    private CompletionStage<Void> timeoutAfterEffect(WayfarerServices services) {
        return execute(services, "timeout-after").thenCompose(result -> {
            require(result.state() == WayfarerTransactions.State.UNKNOWN, "timeout unknown");
            write(FIXTURE_MODE_FILE, "success");
            return services.transactions().reconcile(result.transactionId());
        }).thenAccept(result -> require(
            result.state() == WayfarerTransactions.State.RECONCILED_COMMITTED,
            "applied automatic reconcile"
        ));
    }

    private CompletionStage<Void> refundCrash(WayfarerServices services) {
        return execute(services, "refund-crash").thenCompose(result -> {
            require(result.state() == WayfarerTransactions.State.UNKNOWN, "refund seed unknown");
            getLogger().info(
                "WAYFARER_PRECLIENT_PROBE: REFUND_CRASH_TRANSACTION id="
                    + result.transactionId()
            );
            write(FIXTURE_MODE_FILE, "crash-after-refund");
            return services.transactions().reconcile(
                result.transactionId(),
                WayfarerTransactions.ReconcileAction.REFUND
            );
        }).thenAccept(ignored -> {
            throw new IllegalStateException("refund crash was not injected");
        });
    }

    private CompletionStage<Void> recoveryVerify(WayfarerServices services) {
        UUID transactionId = UUID.fromString(read(EXPECTED_TRANSACTION_FILE, ""));
        WayfarerTransactions.State expected = WayfarerTransactions.State.valueOf(
            read(EXPECTED_STATE_FILE, "")
        );
        return services.transactions().inspect(transactionId).thenAccept(details -> {
            require(details.state() == expected, "recovered transaction state");
            require(details.debitOperationId() != null, "recovered debit operation");
            if (expected == WayfarerTransactions.State.RECONCILED_REFUNDED) {
                require(details.refundOperationId() != null, "recovered refund operation");
                require(
                    details.refundProviderReference() != null,
                    "recovered refund reference"
                );
            }
            getLogger().info(
                "WAYFARER_PRECLIENT_PROBE: RECOVERY_PASS id=" + transactionId
                    + " state=" + expected
            );
        });
    }

    private CompletionStage<Void> queueRejection(WayfarerServices services) {
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger completed = new AtomicInteger();
        List<CompletableFuture<Void>> observations = new ArrayList<>();
        getServer().getScheduler().runTaskLater(this, release::countDown, 20L);
        for (int index = 0; index < 48; index++) {
            CompletionStage<Integer> stage = services.tasks().database(() -> {
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("queue probe interrupted");
                }
                return 1;
            });
            observations.add(stage.handle((value, failure) -> {
                if (failure == null) {
                    completed.incrementAndGet();
                } else {
                    rejected.incrementAndGet();
                }
                return (Void) null;
            }).toCompletableFuture());
        }
        return CompletableFuture.allOf(observations.toArray(CompletableFuture[]::new))
            .thenAccept(ignored -> {
                require(rejected.get() > 0, "bounded queue rejection");
                require(completed.get() > 0, "accepted queue work");
                getLogger().info(
                    "WAYFARER_PRECLIENT_PROBE: QUEUE_PASS accepted=" + completed.get()
                        + " rejected=" + rejected.get()
                );
            });
    }

    private CompletionStage<Void> shutdownTimeout(WayfarerServices services) {
        services.tasks().database(() -> {
            getLogger().info("WAYFARER_PRECLIENT_PROBE: SHUTDOWN_BLOCKER_RUNNING");
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(12);
            while (System.nanoTime() < deadline) {
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException ignored) {
                    // Deliberately ignore interruption to exercise the bounded non-clean path.
                }
            }
            return null;
        });
        return CompletableFuture.completedFuture(null);
    }

    private CompletionStage<Void> acceptedDrain(WayfarerServices services) {
        services.tasks().database(() -> {
            getLogger().info("WAYFARER_PRECLIENT_PROBE: ACCEPTED_WORK_RUNNING");
            try {
                Thread.sleep(2_000L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("accepted work was interrupted");
            }
            getLogger().info("WAYFARER_PRECLIENT_PROBE: ACCEPTED_WORK_COMPLETED");
            return null;
        });
        return CompletableFuture.completedFuture(null);
    }

    private CompletionStage<Void> auditIdentityAndTaskProbe(WayfarerServices services) {
        WayfarerAudit.AuditEvent event = new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            "PRECLIENT_RUNTIME_PROBE",
            ACTOR_ID,
            "HEADLESS_RUNTIME",
            "rc.1",
            services.serverId(),
            "{}",
            Instant.now()
        );
        WayfarerItemIdentity.CreateRequest create =
            new WayfarerItemIdentity.CreateRequest(
                "preclient_probe",
                ACTOR_ID,
                1,
                1,
                1
            );

        return services.audit().record(event)
            .thenCompose(ignored -> services.itemIdentity().create(create))
            .thenCompose(identity -> verifyIdentityRoundTrip(services, identity))
            .thenCompose(ignored -> services.tasks().database(() -> {
                require(!Bukkit.isPrimaryThread(), "database worker thread");
                return "worker-pass";
            }))
            .thenCompose(workerResult -> {
                require("worker-pass".equals(workerResult), "database task result");
                return services.tasks().bridge(
                    "preclient-snapshot",
                    value -> {
                        require(!Bukkit.isPrimaryThread(), "bridge worker thread");
                        return value.toUpperCase(java.util.Locale.ROOT);
                    },
                    "PRECLIENT-SNAPSHOT"::equals,
                    value -> require(Bukkit.isPrimaryThread(), "bridge main thread")
                );
            })
            .thenAccept(result -> {
                require(result.applied(), "bridge revalidation");
                require(
                    "PRECLIENT-SNAPSHOT".equals(result.immutableResult()),
                    "bridge result"
                );
            });
    }

    private CompletionStage<WayfarerTransactions.TransactionResult> execute(
        WayfarerServices services,
        String suffix
    ) {
        return services.transactions().execute(
            new WayfarerTransactions.TransactionRequest(
                "preclient-" + suffix + "-" + UUID.randomUUID(),
                "HEADLESS_TEST",
                ACTOR_ID,
                "PRECLIENT",
                suffix,
                25L,
                "{\"fixture\":true}"
            )
        );
    }

    private CompletionStage<Void> verifyIdentityRoundTrip(
        WayfarerServices services,
        WayfarerItemIdentity.Identity identity
    ) {
        return services.itemIdentity().find(identity.itemInstanceId())
            .thenCompose(found -> {
                require(found.equals(Optional.of(identity)), "identity find");
                WayfarerItemIdentity.RawClaim claim =
                    new WayfarerItemIdentity.RawClaim(
                        identity.itemInstanceId().toString(),
                        identity.itemType(),
                        identity.ownerUuid().toString(),
                        identity.instanceEpoch(),
                        identity.schemaVersion(),
                        identity.displayRevision()
                    );
                return services.itemIdentity().validate(
                    new WayfarerItemIdentity.ValidationRequest(
                        claim,
                        Set.of(identity.itemType()),
                        Set.of(identity.schemaVersion()),
                        identity.ownerUuid(),
                        OptionalLong.of(identity.instanceEpoch())
                    )
                );
            })
            .thenAccept(result -> require(result.valid(), "identity validation"));
    }

    private void fail(String detail) {
        getLogger().severe("WAYFARER_PRECLIENT_PROBE: FAIL " + detail);
    }

    private static void requireUnavailable(ServiceLookup lookup, String component) {
        try {
            lookup.get();
            throw new IllegalStateException(component + " unexpectedly available");
        } catch (IllegalStateException expected) {
            require(
                (component + " is unavailable").equals(expected.getMessage()),
                component + " fail-closed message"
            );
        }
    }

    private static void require(boolean condition, String checkpoint) {
        if (!condition) {
            throw new IllegalStateException("Probe checkpoint failed: " + checkpoint);
        }
    }

    private static String read(Path path, String fallback) {
        try {
            return Files.isRegularFile(path) ? Files.readString(path).trim() : fallback;
        } catch (IOException failure) {
            throw new IllegalStateException("Probe control file read failed");
        }
    }

    private static void write(Path path, String value) {
        try {
            Files.writeString(path, value + System.lineSeparator());
        } catch (IOException failure) {
            throw new IllegalStateException("Probe control file write failed");
        }
    }

    private static Throwable rootCause(Throwable failure) {
        Throwable current = failure;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    @FunctionalInterface
    private interface ServiceLookup {
        Object get();
    }
}
