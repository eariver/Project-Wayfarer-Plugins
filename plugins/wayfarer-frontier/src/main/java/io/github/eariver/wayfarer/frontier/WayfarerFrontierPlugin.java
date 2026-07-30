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
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseCoordinator;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WayfarerFrontierPlugin extends JavaPlugin {
    private final AtomicBoolean accepting = new AtomicBoolean();
    private volatile FrontierModulePersistence persistence;
    private volatile JdbcFrontierPurchaseRepository purchaseRepository;
    private volatile JdbcLaunchpadRepository launchpadRepository;
    private volatile FrontierGameplayRuntime gameplay;
    private volatile FrontierPurchaseCoordinator purchases;
    private final ConcurrentHashMap<String, String> purchaseRequests =
        new ConcurrentHashMap<>();
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
            command.setExecutor((sender, ignored, label, arguments) ->
                handleCommand(sender, arguments, moduleConfig, services)
            );
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
        purchases = null;
        purchaseRequests.clear();
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
            launchpadRepository,
            new ReflectiveLeafGrappleBridge(
                this,
                getServer().getPluginManager().getPlugin("LeafGrapple")
            ),
            Clock.systemUTC()
        );
        purchases = new FrontierPurchaseCoordinator(
            new FrontierWorldGate(java.util.Set.of(config.exactWorldName())),
            config.shopCatalog(),
            purchaseRepository,
            services.transactions(),
            services.tasks(),
            gameplay::deliverPurchase,
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

    private boolean handleCommand(
        CommandSender sender,
        String[] arguments,
        FrontierModuleConfig config,
        WayfarerServices services
    ) {
        if (arguments.length == 0 || "status".equalsIgnoreCase(arguments[0])) {
            sender.sendMessage(
                "Wayfarer Frontier: " + runtimeState
                    + " | config=" + config.configVersion()
                    + " | world=" + config.exactWorldName()
            );
            return true;
        }
        if (!(sender instanceof Player player)
            || !player.hasPermission("wayfarer.frontier.use")) {
            sender.sendMessage("Wayfarer Frontier operation is unavailable.");
            return true;
        }
        if ("open".equalsIgnoreCase(arguments[0])) {
            var inventory = Bukkit.createInventory(
                null,
                27,
                Component.text("Wayfarer Navigation")
            );
            inventory.setItem(13, new ItemStack(Material.COMPASS));
            player.openInventory(inventory);
            return true;
        }
        if (!"shop".equalsIgnoreCase(arguments[0]) || arguments.length != 2) {
            sender.sendMessage(
                "Usage: /wayfarer-frontier <status|open|shop <offer>>"
            );
            return true;
        }
        FrontierPurchaseCoordinator coordinator = purchases;
        if (coordinator == null) {
            sender.sendMessage("Wayfarer Frontier shop is unavailable.");
            return true;
        }
        String offer = arguments[1].toLowerCase(java.util.Locale.ROOT);
        String requestSlot = player.getUniqueId() + ":" + offer;
        String idempotency = purchaseRequests.computeIfAbsent(
            requestSlot,
            ignored -> "frontier-shop:" + UUID.randomUUID()
        );
        coordinator.purchase(new FrontierPurchaseCoordinator.Request(
            idempotency,
            player.getUniqueId(),
            player.getWorld().getName(),
            offer
        )).thenAccept(result -> services.tasks().mainThread(() -> {
            player.sendMessage("Wayfarer Frontier purchase: " + result.status());
            if (result.status() != FrontierPurchaseCoordinator.Status.UNKNOWN) {
                getServer().getScheduler().runTaskLater(
                    this,
                    () -> purchaseRequests.remove(requestSlot, idempotency),
                    40L
                );
            }
        }));
        return true;
    }

    private void failClosed(String message) {
        runtimeState = "FAILED";
        getLogger().severe(message + " Disabling fail-closed.");
        getServer().getPluginManager().disablePlugin(this);
    }
}
