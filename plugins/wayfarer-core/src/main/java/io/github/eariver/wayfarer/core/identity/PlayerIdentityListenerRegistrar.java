package io.github.eariver.wayfarer.core.identity;

@FunctionalInterface
public interface PlayerIdentityListenerRegistrar {
    AutoCloseable register(PlayerIdentitySink sink);

    static PlayerIdentityListenerRegistrar unavailable() {
        return ignored -> () -> {};
    }
}
