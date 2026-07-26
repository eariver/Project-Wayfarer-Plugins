package io.github.eariver.wayfarer.core.lifecycle;

import io.github.eariver.wayfarer.api.WayfarerLifecycleState;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class LifecycleCoordinator {
    private final Deque<NamedResource> resources = new ArrayDeque<>();
    private final Consumer<String> warningSink;
    private WayfarerLifecycleState state = WayfarerLifecycleState.NEW;
    private String failureDetail = "";

    public LifecycleCoordinator(Consumer<String> warningSink) {
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    public synchronized void enable(List<LifecycleStep> steps) {
        enable(steps, null);
    }

    public synchronized void enable(
        List<LifecycleStep> steps,
        LifecycleStep activation
    ) {
        Objects.requireNonNull(steps, "steps");
        transition(WayfarerLifecycleState.INITIALIZING);
        String activeStep = "initialization";
        try {
            for (LifecycleStep step : steps) {
                activeStep = step.name();
                AutoCloseable resource = step.initializer().initialize();
                if (resource != null) {
                    resources.push(new NamedResource(step.name(), resource));
                }
            }
            transition(WayfarerLifecycleState.ENABLED);
            if (activation != null) {
                activeStep = activation.name();
                AutoCloseable resource = activation.initializer().initialize();
                if (resource != null) {
                    resources.push(new NamedResource(activation.name(), resource));
                }
            }
        } catch (Exception failure) {
            failureDetail = "Initialization failed at step " + activeStep;
            cleanup();
            transition(WayfarerLifecycleState.FAILED);
            throw new LifecycleException(failureDetail);
        }
    }

    public synchronized void disable() {
        if (state == WayfarerLifecycleState.DISABLED) {
            return;
        }
        if (state == WayfarerLifecycleState.NEW) {
            transition(WayfarerLifecycleState.DISABLED);
            return;
        }
        if (state != WayfarerLifecycleState.ENABLED
            && state != WayfarerLifecycleState.FAILED) {
            throw new LifecycleException("Cannot disable lifecycle from " + state);
        }
        transition(WayfarerLifecycleState.STOPPING);
        cleanup();
        transition(WayfarerLifecycleState.DISABLED);
    }

    public synchronized WayfarerLifecycleState state() {
        return state;
    }

    public synchronized boolean acceptsCallbacks() {
        return state == WayfarerLifecycleState.ENABLED;
    }

    public synchronized String failureDetail() {
        return failureDetail;
    }

    private void cleanup() {
        while (!resources.isEmpty()) {
            NamedResource resource = resources.pop();
            try {
                resource.value().close();
            } catch (Exception cleanupFailure) {
                try {
                    warningSink.accept("Cleanup failed for " + resource.name());
                } catch (RuntimeException ignored) {
                    // Cleanup continues even if diagnostics are unavailable.
                }
            }
        }
    }

    private void transition(WayfarerLifecycleState target) {
        boolean allowed = switch (state) {
            case NEW -> target == WayfarerLifecycleState.INITIALIZING
                || target == WayfarerLifecycleState.DISABLED;
            case INITIALIZING -> target == WayfarerLifecycleState.ENABLED
                || target == WayfarerLifecycleState.FAILED;
            case ENABLED -> target == WayfarerLifecycleState.STOPPING
                || target == WayfarerLifecycleState.FAILED;
            case FAILED -> target == WayfarerLifecycleState.STOPPING;
            case STOPPING -> target == WayfarerLifecycleState.DISABLED;
            case DISABLED -> false;
        };
        if (!allowed) {
            throw new LifecycleException("Invalid lifecycle transition: " + state + " -> " + target);
        }
        state = target;
    }

    private record NamedResource(String name, AutoCloseable value) {}
}
