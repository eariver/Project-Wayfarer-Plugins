package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.core.identity.PlayerIdentityListenerRegistrar;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityObservation;
import io.github.eariver.wayfarer.core.identity.PlayerIdentitySink;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class BukkitPlayerIdentityListenerRegistrar
    implements PlayerIdentityListenerRegistrar {
    private final Plugin plugin;
    private final String serverId;
    private final Clock clock;
    private final Consumer<String> warningSink;

    public BukkitPlayerIdentityListenerRegistrar(
        Plugin plugin,
        String serverId,
        Clock clock,
        Consumer<String> warningSink
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.serverId = Objects.requireNonNull(serverId, "serverId");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    @Override
    public AutoCloseable register(PlayerIdentitySink sink) {
        Objects.requireNonNull(sink, "sink");
        Listener listener = new Listener() {
            @EventHandler
            public void onPlayerJoin(PlayerJoinEvent event) {
                UUID playerUuid = event.getPlayer().getUniqueId();
                String playerName = event.getPlayer().getName();
                Instant observedAt = clock.instant();
                PlayerIdentityObservation observation = snapshot(
                    playerUuid,
                    playerName,
                    serverId,
                    observedAt
                );
                sink.observe(observation).whenComplete((ignored, failure) -> {
                    if (failure != null) {
                        warn("Wayfarer player identity observation failed");
                    }
                });
            }
        };
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        return () -> HandlerList.unregisterAll(listener);
    }

    static PlayerIdentityObservation snapshot(
        UUID playerUuid,
        String playerName,
        String serverId,
        Instant observedAt
    ) {
        return new PlayerIdentityObservation(
            playerUuid,
            playerName,
            serverId,
            observedAt
        );
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Repository health remains authoritative.
        }
    }
}
