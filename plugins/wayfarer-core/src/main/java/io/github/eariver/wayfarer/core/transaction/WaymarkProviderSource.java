package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;

import java.util.Optional;

@FunctionalInterface
public interface WaymarkProviderSource extends AutoCloseable {
    Optional<WayfarerWaymarkProvider> discover();

    @Override
    default void close() {}

    static WaymarkProviderSource unavailable() {
        return Optional::empty;
    }

    static WaymarkProviderSource fixed(WayfarerWaymarkProvider provider) {
        return provider == null
            ? unavailable()
            : () -> Optional.of(provider);
    }
}
