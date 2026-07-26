package io.github.eariver.wayfarer.core.health;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthRegistryTest {
    private static final Instant NOW = Instant.parse("2026-07-26T00:00:00Z");

    @Test
    void overallAggregationIsUpWhenEveryComponentIsUp() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        fixture.registry().snapshot().components().keySet().forEach(
            component -> fixture.registry().update(component, WayfarerHealth.Status.UP, "ready")
        );
        assertEquals(WayfarerHealth.Status.UP, fixture.registry().snapshot().overall());
    }

    @Test
    void configFailureMakesOverallDown() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        fixture.registry().update(
            HealthRegistry.CONFIG,
            WayfarerHealth.Status.DOWN,
            "invalid configuration"
        );
        assertEquals(WayfarerHealth.Status.DOWN, fixture.registry().snapshot().overall());
    }

    @Test
    void executorFailureIsReported() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        fixture.registry().update(
            HealthRegistry.EXECUTOR,
            WayfarerHealth.Status.DOWN,
            "task failure"
        );
        assertEquals(
            WayfarerHealth.Status.DOWN,
            fixture.registry().snapshot().components().get(HealthRegistry.EXECUTOR).status()
        );
    }

    @Test
    void servicesStatusIsReported() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        fixture.registry().update(
            HealthRegistry.SERVICES,
            WayfarerHealth.Status.UP,
            "registered"
        );
        assertEquals(
            WayfarerHealth.Status.UP,
            fixture.registry().snapshot().components().get(HealthRegistry.SERVICES).status()
        );
    }

    @Test
    void disabledLifecycleMakesOverallDisabled() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        fixture.state().set(WayfarerLifecycleState.DISABLED);
        assertEquals(WayfarerHealth.Status.DISABLED, fixture.registry().snapshot().overall());
    }

    @Test
    void unimplementedDependenciesRemainUnknown() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        WayfarerHealth.ComponentHealth mariaDb =
            fixture.registry().snapshot().components().get("MariaDB");
        assertEquals(WayfarerHealth.Status.UNKNOWN, mariaDb.status());
        assertTrue(mariaDb.detail().contains("Not implemented"));
    }

    @Test
    void healthDetailRedactsKnownSecret() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        try (SecretValue secret = SecretValue.of("health-secret")) {
            fixture.registry().update(
                "SecretProbe",
                WayfarerHealth.Status.DOWN,
                "failed with health-secret",
                secret
            );
            String detail = fixture.registry()
                .snapshot()
                .components()
                .get("SecretProbe")
                .detail();
            assertFalse(detail.contains("health-secret"));
            assertTrue(detail.contains("[REDACTED]"));
        }
    }

    @Test
    void snapshotCarriesUtcTimestampAndLifecycle() {
        Fixture fixture = fixture(WayfarerLifecycleState.ENABLED);
        WayfarerHealth.HealthSnapshot snapshot = fixture.registry().snapshot();
        assertEquals(NOW, snapshot.timestamp());
        assertEquals(WayfarerLifecycleState.ENABLED, snapshot.lifecycleState());
    }

    private static Fixture fixture(WayfarerLifecycleState initial) {
        AtomicReference<WayfarerLifecycleState> state = new AtomicReference<>(initial);
        HealthRegistry registry = new HealthRegistry(
            Clock.fixed(NOW, ZoneOffset.UTC),
            state::get
        );
        return new Fixture(registry, state);
    }

    private record Fixture(
        HealthRegistry registry,
        AtomicReference<WayfarerLifecycleState> state
    ) {}
}
