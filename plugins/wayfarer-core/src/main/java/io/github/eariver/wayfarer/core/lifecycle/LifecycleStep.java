package io.github.eariver.wayfarer.core.lifecycle;

public record LifecycleStep(String name, Initializer initializer) {
    public LifecycleStep {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Lifecycle step name must not be blank");
        }
        if (initializer == null) {
            throw new IllegalArgumentException("Lifecycle initializer must not be null");
        }
    }

    @FunctionalInterface
    public interface Initializer {
        AutoCloseable initialize() throws Exception;
    }
}
