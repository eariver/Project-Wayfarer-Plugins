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
import java.util.Optional;
import java.util.UUID;
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
        accepting.set(true);
        runtimeState = "WAITING_FOR_CORE";
        startWhenCoreReady(moduleConfig, 0);
    }

    private void startWhenCoreReady(
        MainModuleConfig moduleConfig,
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
        MainModuleConfig moduleConfig,
        WayfarerServices services
    ) {
        runtimeState = "INITIALIZING";
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
                    scheduleFailClosed(
                        "Wayfarer_Main runtime initialization failed."
                    );
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
        PluginCommand command = getCommand("wayfarer-main");
        if (command != null) {
            command.setExecutor((sender, ignored, label, arguments) ->
                handleCommand(sender, arguments, config, services)
            );
        }
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
        if (arguments.length == 0 || "status".equalsIgnoreCase(arguments[0])) {
            sender.sendMessage(
                "Wayfarer Main: " + runtimeState
                    + " | config=" + config.configVersion()
            );
            return true;
        }
        if ("debug".equalsIgnoreCase(arguments[0])) {
            if (!config.debugCommandsEnabled()
                || !sender.hasPermission("wayfarer.main.debug")
                || !(sender instanceof Player player)
                || arguments.length != 2) {
                sender.sendMessage("Wayfarer Main debug is unavailable.");
                return true;
            }
            MainGameplayRuntime active = gameplay;
            boolean applied = active != null
                && active.debug(
                    player,
                    arguments[1].toLowerCase(java.util.Locale.ROOT)
                );
            sender.sendMessage(
                applied
                    ? "Wayfarer Main debug mutation applied."
                    : "Wayfarer Main debug mutation is unavailable."
            );
            return true;
        }
        if (adminCommand(arguments[0])) {
            return handleAdminCommand(sender, arguments, services);
        }
        if ("branch".equalsIgnoreCase(arguments[0])) {
            if (!(sender instanceof Player player)
                || !player.hasPermission("wayfarer.main.admin")
                || arguments.length != 2) {
                sender.sendMessage("Wayfarer Main branch change is unavailable.");
                return true;
            }
            GrowthTool.Branch branch;
            try {
                branch = GrowthTool.Branch.valueOf(
                    arguments[1].toUpperCase(java.util.Locale.ROOT)
                );
            } catch (IllegalArgumentException failure) {
                sender.sendMessage("Use FORTUNE or SILK_TOUCH.");
                return true;
            }
            MainGameplayRuntime active = gameplay;
            boolean changed = active != null && active.switchBranch(player, branch);
            sender.sendMessage(changed
                ? "Wayfarer Main branch updated."
                : "Hold your active bound Growth Tool.");
            return true;
        }
        if (!"repair".equalsIgnoreCase(arguments[0])) {
            sender.sendMessage("Usage: /wayfarer-main <status|repair|branch>");
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

    private boolean handleAdminCommand(
        CommandSender sender,
        String[] arguments,
        WayfarerServices services
    ) {
        if (!sender.hasPermission("wayfarer.main.admin")) {
            sender.sendMessage("Wayfarer Main administration is unavailable.");
            return true;
        }
        MainGameplayRuntime active = gameplay;
        if (active == null) {
            sender.sendMessage("Wayfarer Main administration is unavailable.");
            return true;
        }
        try {
            return switch (arguments[0].toLowerCase(java.util.Locale.ROOT)) {
                case "inspect" -> inspect(sender, arguments, active, services);
                case "grant", "delivery" ->
                    retryDelivery(sender, arguments, active, services);
                case "revoke" ->
                    mutateAuthority(sender, arguments, active, services, false);
                case "reissue" ->
                    mutateAuthority(sender, arguments, active, services, true);
                case "reconcile" ->
                    reconcileRepair(sender, arguments, services);
                default -> false;
            };
        } catch (RuntimeException failure) {
            sender.sendMessage("Wayfarer Main administration is unavailable.");
            return true;
        }
    }

    private boolean inspect(
        CommandSender sender,
        String[] arguments,
        MainGameplayRuntime active,
        WayfarerServices services
    ) {
        if (arguments.length != 3) {
            sender.sendMessage(
                "Usage: /wayfarer-main inspect <tool|repair> <uuid>"
            );
            return true;
        }
        UUID id = parseUuid(arguments[2]);
        if (id == null) {
            sender.sendMessage("A canonical UUID is required.");
            return true;
        }
        if ("tool".equalsIgnoreCase(arguments[1])) {
            active.inspect(id).whenComplete((found, failure) -> {
                String message = failure == null
                    ? found.map(tool ->
                        "Growth Tool: status=" + tool.status()
                            + " delivery=" + tool.deliveryStatus()
                            + " epoch=" + tool.instanceEpoch()
                    ).orElse("Growth Tool: NOT_FOUND")
                    : "Wayfarer Main inspection is unavailable.";
                sendOnMain(services, sender, message);
            });
            return true;
        }
        if ("repair".equalsIgnoreCase(arguments[1])) {
            JdbcRepairOperationRepository repository = repairRepository;
            if (repository == null) {
                sender.sendMessage("Wayfarer Main inspection is unavailable.");
                return true;
            }
            services.tasks().database(() -> repository.find(id))
                .whenComplete((found, failure) -> {
                    String message = failure == null
                        ? found.map(operation ->
                            "Repair: state=" + operation.state()
                                + " transaction="
                                + Optional.ofNullable(operation.transactionId())
                                    .map(UUID::toString)
                                    .orElse("NONE")
                        ).orElse("Repair: NOT_FOUND")
                        : "Wayfarer Main inspection is unavailable.";
                    sendOnMain(services, sender, message);
                });
            return true;
        }
        sender.sendMessage("Use tool or repair.");
        return true;
    }

    private boolean retryDelivery(
        CommandSender sender,
        String[] arguments,
        MainGameplayRuntime active,
        WayfarerServices services
    ) {
        UUID playerUuid = arguments.length == 2
            ? parseUuid(arguments[1])
            : null;
        if (playerUuid == null) {
            sender.sendMessage(
                "Usage: /wayfarer-main delivery <player-uuid>"
            );
            return true;
        }
        active.retryDelivery(playerUuid).whenComplete((outcome, failure) ->
            sendOnMain(
                services,
                sender,
                failure == null
                    ? "Growth Tool delivery: " + outcome
                    : "Growth Tool delivery is unavailable."
            )
        );
        return true;
    }

    private boolean mutateAuthority(
        CommandSender sender,
        String[] arguments,
        MainGameplayRuntime active,
        WayfarerServices services,
        boolean reissue
    ) {
        UUID playerUuid = arguments.length == 3
            && "confirm".equalsIgnoreCase(arguments[2])
            ? parseUuid(arguments[1])
            : null;
        if (playerUuid == null) {
            sender.sendMessage(
                "Usage: /wayfarer-main "
                    + (reissue ? "reissue" : "revoke")
                    + " <player-uuid> confirm"
            );
            return true;
        }
        var stage = reissue
            ? active.reissue(playerUuid)
            : active.revoke(playerUuid);
        stage.whenComplete((result, failure) -> sendOnMain(
            services,
            sender,
            failure == null
                ? "Growth Tool authority: " + result
                : "Growth Tool authority update is unavailable."
        ));
        return true;
    }

    private boolean reconcileRepair(
        CommandSender sender,
        String[] arguments,
        WayfarerServices services
    ) {
        UUID repairId = arguments.length == 2
            ? parseUuid(arguments[1])
            : null;
        JdbcRepairOperationRepository repository = repairRepository;
        if (repairId == null || repository == null) {
            sender.sendMessage(
                "Usage: /wayfarer-main reconcile <repair-uuid>"
            );
            return true;
        }
        services.tasks().database(() -> repository.find(repairId))
            .thenCompose(found -> {
                if (found.isEmpty() || found.orElseThrow().transactionId() == null) {
                    return java.util.concurrent.CompletableFuture.completedFuture(
                        "Repair reconcile: MANUAL_REVIEW_REQUIRED"
                    );
                }
                return services.transactions().reconcile(
                    found.orElseThrow().transactionId()
                ).handle((result, failure) ->
                    failure == null
                        ? "Repair reconcile: core=" + result.state()
                            + " module=" + found.orElseThrow().state()
                        : "Repair reconcile is unavailable."
                );
            }).exceptionally(ignored ->
                "Repair reconcile is unavailable."
            ).thenAccept(message -> sendOnMain(services, sender, message));
        return true;
    }

    private static boolean adminCommand(String command) {
        return switch (command.toLowerCase(java.util.Locale.ROOT)) {
            case "inspect", "grant", "delivery", "revoke", "reissue",
                 "reconcile" -> true;
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
