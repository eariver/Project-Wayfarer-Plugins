package io.github.eariver.wayfarer.preclient;

import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerItemIdentity;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public final class PreclientProbePlugin extends JavaPlugin {
    private static final UUID ACTOR_ID =
        UUID.fromString("12345678-1234-5678-9234-567812345678");

    @Override
    public void onEnable() {
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);
        if (registration == null) {
            getLogger().severe("WAYFARER_PRECLIENT_PROBE: FAIL SERVICE_LOOKUP");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        WayfarerServices services = registration.getProvider();
        try {
            verifySynchronousBoundaries(services);
            getLogger().info("WAYFARER_PRECLIENT_PROBE: SERVICE_LOOKUP PASS");
            runAsynchronousProbe(services).whenComplete((ignored, failure) ->
                getServer().getScheduler().runTask(this, () -> {
                    if (failure == null) {
                        getLogger().info("WAYFARER_PRECLIENT_PROBE: PASS");
                    } else {
                        getLogger().severe(
                            "WAYFARER_PRECLIENT_PROBE: FAIL "
                                + rootCause(failure).getClass().getSimpleName()
                        );
                    }
                })
            );
        } catch (RuntimeException failure) {
            getLogger().severe(
                "WAYFARER_PRECLIENT_PROBE: FAIL "
                    + rootCause(failure).getClass().getSimpleName()
            );
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("WAYFARER_PRECLIENT_PROBE: DISABLED");
    }

    private void verifySynchronousBoundaries(WayfarerServices services) {
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
        requireUnavailable(services::transactions, "Transactions");
        requireUnavailable(services::waymark, "Waymark");
    }

    private CompletionStage<Void> runAsynchronousProbe(WayfarerServices services) {
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
