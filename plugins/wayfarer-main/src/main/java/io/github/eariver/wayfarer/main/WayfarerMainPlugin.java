package io.github.eariver.wayfarer.main;

import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import io.github.eariver.wayfarer.main.persistence.MainModulePersistence;
import io.github.eariver.wayfarer.main.persistence.JdbcRepairOperationRepository;
import io.github.eariver.wayfarer.main.persistence.JdbcGrowthToolRepository;
import io.github.eariver.wayfarer.main.gameplay.MainGameplayRuntime;
import io.github.eariver.wayfarer.main.application.RepairCoordinator;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class WayfarerMainPlugin extends JavaPlugin {
    private final AtomicBoolean accepting = new AtomicBoolean();
    private volatile MainModulePersistence persistence;
    private volatile JdbcRepairOperationRepository repairRepository;
    private volatile MainGameplayRuntime gameplay;
    private volatile RepairCoordinator repairCoordinator;
    private volatile Duration disableTimeout = Duration.ofSeconds(15);
    private volatile String runtimeState = "INITIALIZING";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        MainModuleConfig moduleConfig;
        try {
            moduleConfig = MainModuleConfig.load(getConfig());
        } catch (RuntimeException failure) {
            failClosed("Wayfarer_Main configuration is invalid.");
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
        PluginCommand command = getCommand("wayfarer-main");
        if (command != null) {
            command.setExecutor((sender, ignored, label, arguments) ->
                handleCommand(sender, arguments, moduleConfig, services)
            );
        }
        accepting.set(true);
        services.tasks().database(() -> MainModulePersistence.open(
            moduleConfig.database(),
            System::getenv
        )).whenComplete((opened, failure) -> {
            if (failure != null) {
                scheduleFailClosed("Wayfarer_Main persistence is unavailable.");
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
        MainGameplayRuntime activeGameplay = gameplay;
        gameplay = null;
        if (activeGameplay != null) {
            try {
                activeGameplay.stopAndFlush().toCompletableFuture().get(
                    disableTimeout.toMillis(),
                    TimeUnit.MILLISECONDS
                );
            } catch (Exception failure) {
                getLogger().warning(
                    "Wayfarer_Main checkpoint drain did not complete cleanly."
                );
            }
        }
        MainModulePersistence opened = persistence;
        persistence = null;
        repairRepository = null;
        repairCoordinator = null;
        if (opened != null) {
            opened.close();
        }
    }

    private void finishEnable(
        MainModulePersistence opened,
        MainModuleConfig config,
        WayfarerServices services
    ) {
        if (!accepting.get() || !isEnabled()) {
            opened.close();
            return;
        }
        persistence = opened;
        disableTimeout = config.disableTimeout();
        JdbcGrowthToolRepository growthRepository =
            new JdbcGrowthToolRepository(opened.dataSource());
        repairRepository = new JdbcRepairOperationRepository(opened.dataSource());
        gameplay = new MainGameplayRuntime(
            this,
            config,
            services,
            growthRepository,
            Clock.systemUTC()
        );
        repairCoordinator = new RepairCoordinator(
            repairRepository,
            services.transactions(),
            services.waymark(),
            services.tasks(),
            gameplay::applyFullRepair,
            Clock.systemUTC()
        );
        runtimeState = "ENABLED";
        getLogger().info(
            "Wayfarer_Main runtime enabled; module migration and persistence are UP."
        );
    }

    private void scheduleFailClosed(String message) {
        getServer().getScheduler().runTask(this, () -> failClosed(message));
    }

    private boolean handleCommand(
        CommandSender sender,
        String[] arguments,
        MainModuleConfig config,
        WayfarerServices services
    ) {
        if (arguments.length == 0 || !"repair".equalsIgnoreCase(arguments[0])) {
            sender.sendMessage(
                "Wayfarer Main: " + runtimeState
                    + " | config=" + config.configVersion()
            );
            return true;
        }
        if (!(sender instanceof Player player)
            || !player.hasPermission("wayfarer.main.use")) {
            sender.sendMessage("Wayfarer Main repair is unavailable.");
            return true;
        }
        MainGameplayRuntime active = gameplay;
        RepairCoordinator repairs = repairCoordinator;
        GrowthTool tool = active == null
            ? null
            : active.current(player.getUniqueId()).orElse(null);
        if (tool == null || repairs == null) {
            sender.sendMessage("Wayfarer Main repair is unavailable.");
            return true;
        }
        MainGameplayRuntime.RepairSnapshot snapshot =
            active.repairSnapshot(player, tool).orElse(null);
        if (snapshot == null) {
            sender.sendMessage("Hold your bound Growth Tool to repair it.");
            return true;
        }
        int maximum = snapshot.maximumDurability();
        int damage = snapshot.damage();
        var quote = config.repairPricing().quote(
            tool.status(),
            config.evolutionPlan().evaluate(
                tool.cumulativeProgressUnits(),
                tool.branch(),
                config.enchantmentCaps()
            ).evolutionCount(),
            damage,
            maximum
        );
        if (!quote.available()) {
            sender.sendMessage("Wayfarer Main repair is not required.");
            return true;
        }
        String key = "main-repair:" + tool.toolId() + ":"
            + tool.displayRevision() + ":" + damage;
        repairs.repair(new RepairCoordinator.Request(
            key,
            player.getUniqueId(),
            tool.toolId(),
            tool.instanceEpoch(),
            quote.amountWaymark()
        )).thenAccept(result -> services.tasks().mainThread(() ->
            sender.sendMessage("Wayfarer Main repair: " + result.status())
        ));
        return true;
    }

    private void failClosed(String message) {
        runtimeState = "FAILED";
        getLogger().severe(message + " Disabling fail-closed.");
        getServer().getPluginManager().disablePlugin(this);
    }
}
