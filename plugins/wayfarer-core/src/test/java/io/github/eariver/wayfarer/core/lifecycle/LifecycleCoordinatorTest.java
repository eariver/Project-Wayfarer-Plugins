package io.github.eariver.wayfarer.core.lifecycle;

import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LifecycleCoordinatorTest {
    @Test
    void validStartupTransitionReachesEnabled() {
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of(new LifecycleStep("one", () -> () -> {})));
        assertEquals(WayfarerLifecycleState.ENABLED, lifecycle.state());
        assertTrue(lifecycle.acceptsCallbacks());
    }

    @Test
    void invalidTransitionAfterDisableIsRejected() {
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.disable();
        assertThrows(LifecycleException.class, () -> lifecycle.enable(List.of()));
    }

    @Test
    void doubleEnableIsRejected() {
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of());
        assertThrows(LifecycleException.class, () -> lifecycle.enable(List.of()));
    }

    @Test
    void cleanDisableReleasesResources() {
        List<String> events = new ArrayList<>();
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of(new LifecycleStep("one", () -> () -> events.add("closed"))));
        lifecycle.disable();
        assertEquals(List.of("closed"), events);
        assertEquals(WayfarerLifecycleState.DISABLED, lifecycle.state());
    }

    @Test
    void doubleDisableIsSafe() {
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of());
        lifecycle.disable();
        assertDoesNotThrow(lifecycle::disable);
    }

    @Test
    void partialInitializationCleansInitializedResources() {
        List<String> events = new ArrayList<>();
        LifecycleCoordinator lifecycle = lifecycle();
        assertThrows(
            LifecycleException.class,
            () -> lifecycle.enable(List.of(
                new LifecycleStep("one", () -> () -> events.add("one")),
                new LifecycleStep("two", () -> {
                    throw new IllegalStateException("unsafe internal message");
                })
            ))
        );
        assertEquals(List.of("one"), events);
    }

    @Test
    void cleanupRunsInReverseOrder() {
        List<String> events = new ArrayList<>();
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of(
            new LifecycleStep("one", () -> () -> events.add("one")),
            new LifecycleStep("two", () -> () -> events.add("two")),
            new LifecycleStep("three", () -> () -> events.add("three"))
        ));
        lifecycle.disable();
        assertEquals(List.of("three", "two", "one"), events);
    }

    @Test
    void initializationFailureUsesFailedStateAndSanitizedReason() {
        LifecycleCoordinator lifecycle = lifecycle();
        LifecycleException failure = assertThrows(
            LifecycleException.class,
            () -> lifecycle.enable(List.of(new LifecycleStep("safe-name", () -> {
                throw new IllegalStateException("secret-value");
            })))
        );
        assertEquals(WayfarerLifecycleState.FAILED, lifecycle.state());
        assertTrue(failure.getMessage().contains("safe-name"));
        assertFalse(failure.getMessage().contains("secret-value"));
    }

    @Test
    void callbacksAreRejectedAfterDisable() {
        LifecycleCoordinator lifecycle = lifecycle();
        lifecycle.enable(List.of());
        lifecycle.disable();
        assertFalse(lifecycle.acceptsCallbacks());
    }

    @Test
    void cleanupFailureDoesNotSkipRemainingResources() {
        List<String> events = new ArrayList<>();
        LifecycleCoordinator lifecycle = new LifecycleCoordinator(events::add);
        lifecycle.enable(List.of(
            new LifecycleStep("one", () -> () -> events.add("one")),
            new LifecycleStep("two", () -> () -> {
                throw new IllegalStateException("cleanup failure");
            })
        ));
        lifecycle.disable();
        assertTrue(events.contains("Cleanup failed for two"));
        assertTrue(events.contains("one"));
    }

    @Test
    void diagnosticFailureDoesNotSkipRemainingCleanup() {
        List<String> events = new ArrayList<>();
        LifecycleCoordinator lifecycle = new LifecycleCoordinator(message -> {
            throw new IllegalStateException("diagnostics unavailable");
        });
        lifecycle.enable(List.of(
            new LifecycleStep("one", () -> () -> events.add("one")),
            new LifecycleStep("two", () -> () -> {
                throw new IllegalStateException("cleanup failure");
            })
        ));
        lifecycle.disable();
        assertEquals(List.of("one"), events);
        assertEquals(WayfarerLifecycleState.DISABLED, lifecycle.state());
    }

    private static LifecycleCoordinator lifecycle() {
        return new LifecycleCoordinator(ignored -> {});
    }
}
