package io.github.eariver.wayfarer.frontier;

import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.persistence.FrontierModulePersistence;
import io.github.eariver.wayfarer.frontier.persistence.JdbcFrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.persistence.JdbcLaunchpadRepository;
import io.github.eariver.wayfarer.frontier.persistence.JdbcTraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.gameplay.FrontierGameplayRuntime;
import io.github.eariver.wayfarer.integration.leafgrapple.ReflectiveLeafGrappleBridge;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Clock;

public final class WayfarerFrontierPlugin extends JavaPlugin {
    private final AtomicBoolean accepting = new AtomicBoolean();
    private volatile FrontierModulePersistence persistence;
    private volatile JdbcFrontierPurchaseRepository purchaseRepository;
    private volatile JdbcLaunchpadRepository launchpadRepository;
    private volatile FrontierGameplayRuntime gameplay;
    private volatile String runtimeState = "INITIALIZING";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FrontierModuleConfig moduleConfig;
        try {
            moduleConfig = FrontierModuleConfig.load(getConfig());
        } catch (RuntimeException failure) {
            failClosed("Wayfarer_Frontier configuration is invalid.");
            return;
        }
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);
        if (registration == null) {
            failClosed("Wayfarer_Core services are unavailable.");
            return;
        }
        WayfarerServices services;
        try {
            services = registration.getProvider();
            if (services.lifecycleState() != WayfarerLifecycleState.ENABLED) {
                failClosed("Wayfarer_Core is not enabled.");
                return;
            }
            services.tasks();
            services.audit();
            services.transactions();
            services.waymark();
            services.itemIdentity();
        } catch (RuntimeException failure) {
            failClosed("Required Wayfarer_Core capabilities are unavailable.");
            return;
        }
        PluginCommand command = getCommand("wayfarer-frontier");
        if (command != null) {
            command.setExecutor((sender, ignored, label, arguments) -> {
                sender.sendMessage(
                    "Wayfarer Frontier: " + runtimeState
                        + " | config=" + moduleConfig.configVersion()
                        + " | world=" + moduleConfig.exactWorldName()
                );
                return true;
            });
        }
        accepting.set(true);
        services.tasks().database(() -> FrontierModulePersistence.open(
            moduleConfig.database(),
            System::getenv
        )).whenComplete((opened, failure) -> {
            if (failure != null) {
                scheduleFailClosed("Wayfarer_Frontier persistence is unavailable.");
                return;
            }
            services.tasks().mainThread(() ->
                finishEnable(opened, moduleConfig, services)
            )
                .exceptionally(ignored -> {
                    opened.close();
                    return null;
                });
        });
    }

    @Override
    public void onDisable() {
        accepting.set(false);
        runtimeState = "DISABLED";
        FrontierModulePersistence opened = persistence;
        persistence = null;
        purchaseRepository = null;
        launchpadRepository = null;
        gameplay = null;
        if (opened != null) {
            opened.close();
        }
    }

    private void finishEnable(
        FrontierModulePersistence opened,
        FrontierModuleConfig config,
        WayfarerServices services
    ) {
        if (!accepting.get() || !isEnabled()) {
            opened.close();
            return;
        }
        persistence = opened;
        purchaseRepository = new JdbcFrontierPurchaseRepository(opened.dataSource());
        launchpadRepository = new JdbcLaunchpadRepository(opened.dataSource());
        JdbcTraversalLoadoutRepository loadouts =
            new JdbcTraversalLoadoutRepository(
                opened.dataSource(),
                config.loadout()
            );
        gameplay = new FrontierGameplayRuntime(
            this,
            config,
            services,
            loadouts,
            new ReflectiveLeafGrappleBridge(
                this,
                getServer().getPluginManager().getPlugin("LeafGrapple")
            ),
            Clock.systemUTC()
        );
        runtimeState = "ENABLED";
        getLogger().info(
            "Wayfarer_Frontier runtime enabled; module migration and persistence are UP."
        );
    }

    private void scheduleFailClosed(String message) {
        getServer().getScheduler().runTask(this, () -> failClosed(message));
    }

    private void failClosed(String message) {
        runtimeState = "FAILED";
        getLogger().severe(message + " Disabling fail-closed.");
        getServer().getPluginManager().disablePlugin(this);
    }
}
