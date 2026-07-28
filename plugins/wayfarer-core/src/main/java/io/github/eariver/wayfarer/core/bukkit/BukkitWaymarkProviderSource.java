package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import io.github.eariver.wayfarer.core.transaction.WaymarkProviderSource;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicesManager;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BukkitWaymarkProviderSource implements WaymarkProviderSource {
    private final ServicesManager servicesManager;
    private final MainThreadDispatcher mainThread;
    private final Function<UUID, OfflinePlayer> offlinePlayerResolver;
    private final String expectedProvider;
    private final Consumer<String> warningSink;
    private VaultWaymarkProvider provider;
    private boolean closed;

    public BukkitWaymarkProviderSource(
        ServicesManager servicesManager,
        MainThreadDispatcher mainThread,
        Function<UUID, OfflinePlayer> offlinePlayerResolver,
        String expectedProvider,
        Consumer<String> warningSink
    ) {
        this.servicesManager = Objects.requireNonNull(servicesManager, "servicesManager");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.offlinePlayerResolver = Objects.requireNonNull(
            offlinePlayerResolver,
            "offlinePlayerResolver"
        );
        this.expectedProvider = Objects.requireNonNull(
            expectedProvider,
            "expectedProvider"
        );
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    @Override
    public synchronized Optional<WayfarerWaymarkProvider> discover() {
        if (closed) {
            return Optional.empty();
        }
        if (provider != null) {
            return Optional.of(provider);
        }
        Economy economy = servicesManager.load(Economy.class);
        if (economy == null) {
            return Optional.empty();
        }
        provider = new VaultWaymarkProvider(
            economy,
            mainThread,
            offlinePlayerResolver,
            expectedProvider,
            warningSink
        );
        return Optional.of(provider);
    }

    @Override
    public synchronized void close() {
        closed = true;
        if (provider != null) {
            provider.close();
            provider = null;
        }
    }
}
