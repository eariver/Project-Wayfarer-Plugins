package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.common.secret.EnvironmentSecretResolver;
import io.github.eariver.wayfarer.core.bukkit.BukkitConfigView;
import io.github.eariver.wayfarer.core.bukkit.BukkitHealthCommand;
import io.github.eariver.wayfarer.core.bukkit.BukkitPlayerIdentityListenerRegistrar;
import io.github.eariver.wayfarer.core.bukkit.BukkitServicePublisher;
import io.github.eariver.wayfarer.core.command.HealthCommandHandler;
import io.github.eariver.wayfarer.core.command.OperationalAuditSink;
import io.github.eariver.wayfarer.core.command.OperationalEventSink;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.config.CoreConfigException;
import io.github.eariver.wayfarer.core.config.CoreConfigLoader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.concurrent.CompletableFuture;

public final class WayfarerCorePlugin extends JavaPlugin {
    private CoreRuntime runtime;

    @Override
    public void onEnable() {
        try {
            saveDefaultConfig();
            CoreConfig config = new CoreConfigLoader().load(
                new BukkitConfigView(getConfig()),
                new EnvironmentSecretResolver()
            );
            Clock clock = Clock.systemUTC();
            runtime = new CoreRuntime(
                config,
                new BukkitServicePublisher(getServer().getServicesManager(), this),
                operation -> getServer().getScheduler().runTask(this, operation),
                clock,
                getLogger()::warning,
                getServer()::isPrimaryThread,
                new BukkitPlayerIdentityListenerRegistrar(
                    this,
                    config.serverId(),
                    clock,
                    getLogger()::warning
                )
            );
            runtime.enable();

            PluginCommand command = getCommand("wayfarer");
            if (command == null) {
                throw new IllegalStateException("Command registration is unavailable");
            }
            OperationalEventSink operationalEvents = config.audit().enabled()
                ? new OperationalAuditSink(
                    runtime.services().audit(),
                    config.serverId(),
                    clock
                )
                : ignored -> CompletableFuture.completedFuture(null);
            HealthCommandHandler handler = new HealthCommandHandler(
                runtime::services,
                getPluginMeta().getVersion(),
                config.health().playerDetails(),
                operationalEvents,
                getLogger()::warning
            );
            command.setExecutor(new BukkitHealthCommand(handler));
            getLogger().info(
                "Wayfarer_Core enabled for server " + config.serverId()
                    + " with config version " + config.configVersion()
            );
        } catch (RuntimeException failure) {
            String diagnostic = sanitizedDiagnostic(failure);
            safelyDisableRuntime();
            getLogger().severe(
                "Wayfarer_Core failed closed during enable: " + diagnostic
            );
            throw new IllegalStateException("Wayfarer_Core enable failed; diagnostics are sanitized");
        }
    }

    @Override
    public void onDisable() {
        safelyDisableRuntime();
    }

    private void safelyDisableRuntime() {
        if (runtime == null) {
            return;
        }
        try {
            if (runtime.state() != WayfarerLifecycleState.DISABLED) {
                runtime.disable();
            }
        } catch (RuntimeException failure) {
            getLogger().severe(
                "Wayfarer_Core shutdown encountered a sanitized failure ("
                    + failure.getClass().getSimpleName() + ")"
            );
        } finally {
            runtime = null;
        }
    }

    private String sanitizedDiagnostic(RuntimeException failure) {
        if (runtime != null && !runtime.failureDetail().isBlank()) {
            return runtime.failureDetail();
        }
        if (failure instanceof CoreConfigException && failure.getMessage() != null) {
            return failure.getMessage();
        }
        return failure.getClass().getSimpleName();
    }
}
