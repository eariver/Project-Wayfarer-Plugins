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
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Clock;
import java.util.UUID;
import java.util.Optional;
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
        accepting.set(true);
        runtimeState = "WAITING_FOR_CORE";
        startWhenCoreReady(moduleConfig, 0);
    }

    private void startWhenCoreReady(
        FrontierModuleConfig moduleConfig,
        int attempt
    ) {
        if (!accepting.get() || !isEnabled()) {
            return;
        }
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);
        WayfarerServices services = null;
        try {
            if (registration != null) {
                services = registration.getProvider();
            }
            if (services != null
                && services.lifecycleState() == WayfarerLifecycleState.ENABLED) {
                services.tasks();
                services.audit();
                services.transactions();
                services.waymark();
                services.itemIdentity();
            } else {
                services = null;
            }
        } catch (RuntimeException failure) {
            services = null;
        }
        if (services == null) {
            if (attempt >= 200) {
                failClosed("Required Wayfarer_Core capabilities are unavailable.");
                return;
            }
            getServer().getScheduler().runTaskLater(
                this,
                () -> startWhenCoreReady(moduleConfig, attempt + 1),
                1L
            );
            return;
        }
        openPersistence(moduleConfig, services);
    }

    private void openPersistence(
        FrontierModuleConfig moduleConfig,
        WayfarerServices services
    ) {
        runtimeState = "INITIALIZING";
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
                    scheduleFailClosed(
                        "Wayfarer_Frontier runtime initialization failed."
                    );
                    return null;
                });
        });
    }

    @Override
    public void onDisable() {
        accepting.set(false);
        runtimeState = "DISABLED";
        FrontierGameplayRuntime activeGameplay = gameplay;
        gameplay = null;
        if (activeGameplay != null) {
            activeGameplay.stop();
        }
        FrontierModulePersistence opened = persistence;
        persistence = null;
        purchaseRepository = null;
        launchpadRepository = null;
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
        PluginCommand command = getCommand("wayfarer-frontier");
        if (command != null) {
            command.setExecutor((sender, ignored, label, arguments) ->
                handleCommand(sender, arguments, config, services)
            );
        }
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
        if (adminCommand(arguments[0])) {
            return handleAdminCommand(sender, arguments, services);
        }
        if (!(sender instanceof Player player)
            || !player.hasPermission("wayfarer.frontier.use")) {
            sender.sendMessage("Wayfarer Frontier operation is unavailable.");
            return true;
        }
        if ("open".equalsIgnoreCase(arguments[0])) {
            FrontierGameplayRuntime active = gameplay;
            if (active == null) {
                player.sendMessage("Wayfarer Frontier navigation is unavailable.");
                return true;
            }
            active.openNavigation(player);
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

    private boolean handleAdminCommand(
        CommandSender sender,
        String[] arguments,
        WayfarerServices services
    ) {
        if (!sender.hasPermission("wayfarer.frontier.admin")) {
            sender.sendMessage("Wayfarer Frontier administration is unavailable.");
            return true;
        }
        FrontierGameplayRuntime active = gameplay;
        if (active == null) {
            sender.sendMessage("Wayfarer Frontier administration is unavailable.");
            return true;
        }
        try {
            return switch (arguments[0].toLowerCase(java.util.Locale.ROOT)) {
                case "loadout" ->
                    handleLoadout(sender, arguments, active, services);
                case "delivery" ->
                    handleDelivery(sender, arguments, active, services);
                case "launchpad" ->
                    handleLaunchpad(sender, arguments, active, services);
                case "transaction", "audit" ->
                    handleTransaction(sender, arguments, services);
                default -> false;
            };
        } catch (RuntimeException failure) {
            sender.sendMessage("Wayfarer Frontier administration is unavailable.");
            return true;
        }
    }

    private boolean handleLoadout(
        CommandSender sender,
        String[] arguments,
        FrontierGameplayRuntime active,
        WayfarerServices services
    ) {
        UUID playerUuid = arguments.length == 3
            ? parseUuid(arguments[2])
            : null;
        if (playerUuid != null
            && "inspect".equalsIgnoreCase(arguments[1])) {
            active.inspectLoadout(playerUuid).whenComplete((found, failure) -> {
                String message = failure == null
                    ? found.map(loadout ->
                        "Frontier loadout: permanent="
                            + loadout.permanentItems().size()
                            + " initial-launchpads="
                            + loadout.initialLaunchpadsGranted()
                    ).orElse("Frontier loadout: NOT_FOUND")
                    : "Frontier loadout inspection is unavailable.";
                sendOnMain(services, sender, message);
            });
            return true;
        }
        UUID reissuePlayer = arguments.length == 5
            && "reissue".equalsIgnoreCase(arguments[1])
            && "confirm".equalsIgnoreCase(arguments[4])
            ? parseUuid(arguments[2])
            : null;
        TraversalIdentity.ItemType itemType = arguments.length == 5
            ? parseItemType(arguments[3])
            : null;
        if (reissuePlayer != null && itemType != null) {
            UUID actorUuid = sender instanceof Player player
                ? player.getUniqueId()
                : null;
            active.reissueLoadout(reissuePlayer, itemType, actorUuid)
                .whenComplete((result, failure) -> sendOnMain(
                    services,
                    sender,
                    failure == null
                        ? "Frontier loadout reissue: " + result
                        : "Frontier loadout reissue is unavailable."
                ));
            return true;
        }
        sender.sendMessage(
            "Usage: /wayfarer-frontier loadout "
                + "<inspect <player-uuid>|reissue <player-uuid> "
                + "<elytra|grappling_hook|navigation> confirm>"
        );
        return true;
    }

    private boolean handleDelivery(
        CommandSender sender,
        String[] arguments,
        FrontierGameplayRuntime active,
        WayfarerServices services
    ) {
        UUID playerUuid = arguments.length == 3
            ? parseUuid(arguments[2])
            : null;
        if (playerUuid == null) {
            sender.sendMessage(
                "Usage: /wayfarer-frontier delivery <inspect|retry> <player-uuid>"
            );
            return true;
        }
        if ("inspect".equalsIgnoreCase(arguments[1])) {
            active.inspectDeliveries(playerUuid).whenComplete((pending, failure) ->
                sendOnMain(
                    services,
                    sender,
                    failure == null
                        ? "Frontier deliveries: pending=" + pending.size()
                        : "Frontier delivery inspection is unavailable."
                )
            );
            return true;
        }
        if ("retry".equalsIgnoreCase(arguments[1])) {
            active.retryDelivery(playerUuid).whenComplete((result, failure) ->
                sendOnMain(
                    services,
                    sender,
                    failure == null
                        ? "Frontier delivery retry: " + result
                        : "Frontier delivery retry is unavailable."
                )
            );
            return true;
        }
        sender.sendMessage(
            "Usage: /wayfarer-frontier delivery <inspect|retry> <player-uuid>"
        );
        return true;
    }

    private boolean handleLaunchpad(
        CommandSender sender,
        String[] arguments,
        FrontierGameplayRuntime active,
        WayfarerServices services
    ) {
        UUID launchpadId = arguments.length >= 3
            ? parseUuid(arguments[2])
            : null;
        if (launchpadId == null) {
            sender.sendMessage(
                "Usage: /wayfarer-frontier launchpad "
                    + "<inspect|remove|reconcile> <launchpad-uuid> [confirm]"
            );
            return true;
        }
        if ("inspect".equalsIgnoreCase(arguments[1])
            && arguments.length == 3) {
            active.inspectLaunchpad(launchpadId)
                .whenComplete((inspection, failure) ->
                    sendOnMain(
                        services,
                        sender,
                        failure == null
                            ? "Launchpad: state="
                                + Optional.ofNullable(inspection.state())
                                    .map(Enum::name)
                                    .orElse("NONE")
                                + " classification="
                                + inspection.classification()
                            : "Launchpad inspection is unavailable."
                    )
                );
            return true;
        }
        boolean confirmed = arguments.length == 4
            && "confirm".equalsIgnoreCase(arguments[3]);
        UUID actorUuid = sender instanceof Player player
            ? player.getUniqueId()
            : null;
        if ("remove".equalsIgnoreCase(arguments[1]) && confirmed) {
            active.removeLaunchpad(launchpadId, actorUuid)
                .whenComplete((result, failure) -> sendOnMain(
                    services,
                    sender,
                    failure == null
                        ? "Launchpad removal: " + result
                        : "Launchpad removal is unavailable."
                ));
            return true;
        }
        if ("reconcile".equalsIgnoreCase(arguments[1])) {
            active.reconcileLaunchpad(
                launchpadId,
                actorUuid,
                confirmed
            ).whenComplete((result, failure) -> sendOnMain(
                services,
                sender,
                failure == null
                    ? "Launchpad reconcile: " + result
                    : "Launchpad reconcile is unavailable."
            ));
            return true;
        }
        sender.sendMessage(
            "Mutation requires: <launchpad-uuid> confirm"
        );
        return true;
    }

    private boolean handleTransaction(
        CommandSender sender,
        String[] arguments,
        WayfarerServices services
    ) {
        UUID purchaseId = arguments.length == 3
            && "inspect".equalsIgnoreCase(arguments[1])
            ? parseUuid(arguments[2])
            : null;
        JdbcFrontierPurchaseRepository repository = purchaseRepository;
        if (purchaseId == null || repository == null) {
            sender.sendMessage(
                "Usage: /wayfarer-frontier transaction inspect <purchase-uuid>"
            );
            return true;
        }
        services.tasks().database(() -> repository.find(purchaseId))
            .whenComplete((found, failure) -> {
                String message = failure == null
                    ? found.map(purchase ->
                        "Frontier purchase: state=" + purchase.state()
                            + " transaction="
                            + Optional.ofNullable(purchase.transactionId())
                                .map(UUID::toString)
                                .orElse("NONE")
                    ).orElse("Frontier purchase: NOT_FOUND")
                    : "Frontier purchase inspection is unavailable.";
                sendOnMain(services, sender, message);
            });
        return true;
    }

    private static boolean adminCommand(String command) {
        return switch (command.toLowerCase(java.util.Locale.ROOT)) {
            case "loadout", "delivery", "launchpad", "transaction", "audit" ->
                true;
            default -> false;
        };
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException failure) {
            return null;
        }
    }

    private static TraversalIdentity.ItemType parseItemType(String value) {
        try {
            return TraversalIdentity.ItemType.valueOf(
                value.toUpperCase(java.util.Locale.ROOT)
            );
        } catch (IllegalArgumentException failure) {
            return null;
        }
    }

    private static void sendOnMain(
        WayfarerServices services,
        CommandSender sender,
        String message
    ) {
        services.tasks().mainThread(() -> sender.sendMessage(message));
    }

    private void failClosed(String message) {
        runtimeState = "FAILED";
        getLogger().severe(message + " Disabling fail-closed.");
        getServer().getPluginManager().disablePlugin(this);
    }
}
