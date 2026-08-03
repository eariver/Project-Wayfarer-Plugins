package io.github.eariver.wayfarer.main.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.main.application.GrowthCheckpointService;
import io.github.eariver.wayfarer.main.application.GrowthSessionStore;
import io.github.eariver.wayfarer.main.application.GrowthToolDeliveryCoordinator;
import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.application.RepairCoordinator;
import io.github.eariver.wayfarer.main.application.DeliveryOutcome;
import io.github.eariver.wayfarer.main.application.ReissueDeliveryGateway;
import io.github.eariver.wayfarer.main.application.ReissueEligibilityPort;
import io.github.eariver.wayfarer.main.application.ReissueEligibilitySnapshot;
import io.github.eariver.wayfarer.main.application.PhysicalItemPresence;
import io.github.eariver.wayfarer.main.application.ReissueDeliveryPolicy;
import io.github.eariver.wayfarer.main.application.ReissueEligibilityPolicy;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.DurabilitySemantics;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim;
import io.github.eariver.wayfarer.common.SingleUseGate;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import java.util.logging.Level;

public final class MainGameplayRuntime implements
    Listener,
    AutoCloseable,
    ReissueEligibilityPort,
    ReissueDeliveryGateway {
    private static final NamespacedKey ITEM_TYPE =
        new NamespacedKey("wayfarer", "item_type");
    private static final NamespacedKey TOOL_ID =
        new NamespacedKey("wayfarer", "tool_id");
    private static final NamespacedKey ITEM_INSTANCE_ID =
        new NamespacedKey("wayfarer", "item_instance_id");
    private static final NamespacedKey TOOL_TYPE =
        new NamespacedKey("wayfarer", "tool_type");
    private static final NamespacedKey OWNER_ID =
        new NamespacedKey("wayfarer", "owner_uuid");
    private static final NamespacedKey EPOCH =
        new NamespacedKey("wayfarer", "instance_epoch");
    private static final NamespacedKey SCHEMA =
        new NamespacedKey("wayfarer", "schema_version");
    private static final NamespacedKey REVISION =
        new NamespacedKey("wayfarer", "display_revision");

    private final JavaPlugin plugin;
    private final MainModuleConfig config;
    private final WayfarerServices services;
    private final GrowthToolRepository repository;
    private final GrowthSessionStore sessions = new GrowthSessionStore();
    private final GrowthCheckpointService checkpoints;
    private final GrowthToolDeliveryCoordinator delivery;
    private final Clock clock;
    private final BukkitTask checkpointTask;
    private final ConcurrentHashMap<UUID, RepairGuiSession> repairGuiSessions =
        new ConcurrentHashMap<>();
    private final Set<UUID> repairInFlight = ConcurrentHashMap.newKeySet();
    private final Set<UUID> evolutionRestorePending =
        ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<UUID, HeldGrowthToolAuthorization>
        heldAuthorizations = new ConcurrentHashMap<>();
    private final AtomicLong authorityRequestSequence = new AtomicLong();
    private final ConcurrentHashMap<UUID, Long> currentAuthorityRequests =
        new ConcurrentHashMap<>();
    private volatile boolean accepting = true;
    private volatile RepairCoordinator repairCoordinator;

    public MainGameplayRuntime(
        JavaPlugin plugin,
        MainModuleConfig config,
        WayfarerServices services,
        GrowthToolRepository repository,
        Clock clock
    ) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.config = java.util.Objects.requireNonNull(config, "config");
        this.services = java.util.Objects.requireNonNull(services, "services");
        this.repository = java.util.Objects.requireNonNull(repository, "repository");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        checkpoints = new GrowthCheckpointService(
            sessions,
            repository,
            services.tasks(),
            clock
        );
        delivery = new GrowthToolDeliveryCoordinator(
            repository,
            services.tasks(),
            services.audit(),
            this::deliver,
            services.serverId(),
            clock,
            this::logDeliveryFailure
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        long period = Math.max(
            20L,
            config.checkpointInterval().toSeconds() * 20L
        );
        checkpointTask = plugin.getServer().getScheduler().runTaskTimer(
            plugin,
            this::checkpointAll,
            period,
            period
        );
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            open(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        authorizeMainHand(event.getPlayer());
        open(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        long generation = authorityRequestSequence.incrementAndGet();
        currentAuthorityRequests.put(playerId, generation);
        heldAuthorizations.remove(playerId);
        checkpoints.checkpoint(playerId).whenComplete((ignored, failure) ->
            services.tasks().mainThread(() -> {
                Player online = plugin.getServer().getPlayer(playerId);
                if (online == null || !online.isOnline()) {
                    sessions.close(playerId);
                }
            })
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void guardManagedBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (repairInFlight.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (wayfarerTool(item)
            && !currentMainHandAllows(player, GrowthTool.Status.ACTIVE)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!config.progressPolicy().allowsWorld(player.getWorld().getName())
            || (player.getGameMode() != GameMode.SURVIVAL
                && player.getGameMode() != GameMode.ADVENTURE)
            || !Tag.MINEABLE_PICKAXE.isTagged(event.getBlock().getType())) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        if (!wayfarerTool(item)
            || tool == null
            || !currentMainHandAllows(player, GrowthTool.Status.ACTIVE)) {
            return;
        }
        long units;
        try {
            units = config.progressPolicy().unitsFor(
                event.getBlock().getType().name(),
                oreGroup(event.getBlock().getType())
            );
        } catch (RuntimeException failure) {
            plugin.getLogger().severe(
                "Growth progress calculation failed closed."
            );
            return;
        }
        try {
            EvolutionPlan.EvolutionSnapshot before =
                config.evolutionPlan().evaluate(
                    tool.cumulativeProgressUnits(),
                    tool.branch(),
                    config.enchantmentCaps()
                );
            applyEvolution(item, tool, false);
            GrowthTool updated = sessions.addProgress(
                player.getUniqueId(),
                units,
                clock.instant()
            );
            EvolutionPlan.EvolutionSnapshot after =
                config.evolutionPlan().evaluate(
                    updated.cumulativeProgressUnits(),
                    updated.branch(),
                    config.enchantmentCaps()
                );
            applyEvolution(
                item,
                updated,
                after.evolutionCount() > before.evolutionCount()
            );
            if (after.evolutionCount() > before.evolutionCount()) {
                evolutionRestorePending.add(player.getUniqueId());
                plugin.getServer().getScheduler().runTask(
                    plugin,
                    () -> evolutionRestorePending.remove(
                        player.getUniqueId()
                    )
                );
            }
        } catch (RuntimeException failure) {
            plugin.getLogger().severe("Growth progress update failed closed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onDamage(PlayerItemDamageEvent event) {
        if (evolutionRestorePending.remove(
            event.getPlayer().getUniqueId()
        ) && wayfarerTool(event.getItem())) {
            event.setCancelled(true);
            return;
        }
        if (repairInFlight.contains(event.getPlayer().getUniqueId())
            && wayfarerTool(event.getItem())) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem();
        Player player = event.getPlayer();
        if (wayfarerTool(item)
            && item != player.getInventory().getItemInMainHand()) {
            event.setCancelled(true);
            return;
        }
        if (wayfarerTool(item)
            && !currentMainHandAllows(player, GrowthTool.Status.ACTIVE)) {
            event.setCancelled(true);
            return;
        }
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        if (tool == null
            || damageable.getDamage() + event.getDamage()
            < item.getType().getMaxDurability()) {
            return;
        }
        event.setCancelled(true);
        GrowthTool broken = sessions.update(
            player.getUniqueId(),
            current -> current.broken(item.getType().getMaxDurability(), clock.instant())
        );
        item.setType(Material.GRAY_DYE);
        writeIdentity(item, broken, "BROKEN_GROWTH_TOOL");
        heldAuthorizations.put(
            player.getUniqueId(),
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER
            )
        );
        checkpoints.checkpoint(broken.ownerUuid());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        invalidateAndScheduleAuthorization(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        invalidateAndScheduleAuthorization(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        heldAuthorizations.remove(event.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMend(PlayerItemMendEvent event) {
        if (wayfarerTool(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (containsWayfarerTool(event.getInventory().getContents())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        if (containsWayfarerTool(event.getInventory().getContents())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (containsWayfarerTool(event.getInventory().getContents())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (containsWayfarerTool(event.getInventory().getMatrix())) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getTopInventory().getHolder()
            instanceof GrowthToolGuiHolder holder) {
            event.setCancelled(true);
            handleGuiClick(player, event.getRawSlot(), holder);
            return;
        }
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        InventoryType type = event.getView().getTopInventory().getType();
        ItemStack hotbar = event.getHotbarButton() >= 0
            ? player.getInventory().getItem(event.getHotbarButton())
            : null;
        boolean clickedTop = event.getClickedInventory()
            == event.getView().getTopInventory();
        boolean processing = GrowthToolInventoryPolicy.isProcessingInventory(type)
            && (type != InventoryType.CRAFTING || clickedTop);
        if (processing && (wayfarerTool(current)
            || wayfarerTool(cursor)
            || wayfarerTool(hotbar))) {
            event.setCancelled(true);
            return;
        }
        invalidateAndScheduleAuthorization(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
            instanceof GrowthToolGuiHolder) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        InventoryType type = event.getView().getTopInventory().getType();
        boolean clickedTop = event.getRawSlots().stream()
            .anyMatch(slot -> slot < topSize);
        boolean processing = GrowthToolInventoryPolicy.isProcessingInventory(type)
            && (type != InventoryType.CRAFTING || clickedTop);
        boolean managed = wayfarerTool(event.getOldCursor())
            || event.getNewItems().values().stream().anyMatch(MainGameplayRuntime::wayfarerTool);
        if (processing && clickedTop && managed) {
            event.setCancelled(true);
            return;
        }
        invalidateAndScheduleAuthorization(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null && event.getHand() != null
            && wayfarerTool(player.getInventory().getItem(event.getHand()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (wayfarerTool(event.getPlayerItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame
            || event.getRightClicked() instanceof ArmorStand)) {
            return;
        }
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if (wayfarerTool(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onHeldSlot(PlayerItemHeldEvent event) {
        invalidateAndScheduleAuthorization(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        invalidateAndScheduleAuthorization(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent event) {
        invalidateAndScheduleAuthorization(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)
            || !(event.getInventory().getHolder()
                instanceof GrowthToolGuiHolder holder)
            || holder.mode() != GuiMode.REPAIR_PREVIEW) {
            return;
        }
        repairGuiSessions.computeIfPresent(
            player.getUniqueId(),
            (ignored, session) ->
                session.token().equals(holder.token()) ? null : session
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        HeldGrowthToolAuthorization authorization =
            authorization(event.getPlayer().getUniqueId());
        GrowthTool tool = sessions.current(event.getPlayer().getUniqueId()).orElse(null);
        if (!wayfarerTool(held)) {
            return;
        }
        if (tool == null
            || !currentMainHandAllows(event.getPlayer(), tool.status())) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        openMainGui(event.getPlayer(), tool, held);
    }

    public CompletionStage<Integer> stopAndFlush() {
        accepting = false;
        currentAuthorityRequests.clear();
        checkpointTask.cancel();
        repairGuiSessions.clear();
        repairInFlight.clear();
        evolutionRestorePending.clear();
        heldAuthorizations.clear();
        return checkpoints.stopAndFlush();
    }

    public void bindRepairCoordinator(RepairCoordinator coordinator) {
        if (repairCoordinator != null) {
            throw new IllegalStateException(
                "Repair coordinator is already bound"
            );
        }
        repairCoordinator = java.util.Objects.requireNonNull(
            coordinator,
            "coordinator"
        );
    }

    public Optional<GrowthTool> current(UUID playerUuid) {
        return sessions.current(playerUuid);
    }

    /**
     * Captures only Main Thread Bukkit state for the paid reissue boundary.
     * No repository, MVI, or container lookup is allowed here.
     */
    @Override
    public ReissueEligibilitySnapshot snapshot(UUID playerUuid) {
        java.util.Objects.requireNonNull(playerUuid, "playerUuid");
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (!accepting || player == null || !player.isOnline()) {
            return new ReissueEligibilitySnapshot(
                playerUuid,
                false,
                "",
                false,
                new PhysicalItemPresence(false, false, false, false)
            );
        }
        String worldName = player.getWorld().getName();
        boolean allowed = ReissueEligibilityPolicy.isAllowedWorld(worldName);
        GrowthTool authority = sessions.current(playerUuid).orElse(null);
        if (authority == null) {
            // No loaded authority means physical presence cannot be proven
            // safely; reject the paid path fail-closed.
            return new ReissueEligibilitySnapshot(
                playerUuid,
                true,
                worldName,
                allowed,
                ReissueEligibilityPolicy.failClosedPresence()
            );
        }
        return new ReissueEligibilitySnapshot(
            playerUuid,
            true,
            worldName,
            allowed,
            ReissueEligibilityPolicy.scan(
                player.getInventory().getStorageContents(),
                player.getInventory().getArmorContents(),
                player.getInventory().getItemInOffHand(),
                player.getOpenInventory().getCursor(),
                item -> currentPhysical(item, playerUuid, authority)
            )
        );
    }

    /**
     * Main Thread physical delivery for the rotated durable authority.
     * This method never performs persistence or rolls authority back.
     */
    @Override
    public DeliveryOutcome deliverReissued(GrowthTool rotatedTool) {
        if (!accepting || rotatedTool == null
            || rotatedTool.status() != GrowthTool.Status.ACTIVE
            || rotatedTool.deliveryStatus() != GrowthTool.DeliveryStatus.PENDING) {
            return DeliveryOutcome.UNAVAILABLE;
        }
        Player player = plugin.getServer().getPlayer(rotatedTool.ownerUuid());
        if (player == null || !player.isOnline()) {
            return ReissueDeliveryPolicy.classify(
                true,
                false,
                false,
                0,
                false
            );
        }
        if (!ReissueEligibilityPolicy.isAllowedWorld(
            player.getWorld().getName()
        )) {
            return ReissueDeliveryPolicy.classify(
                true,
                true,
                false,
                0,
                false
            );
        }

        invalidateAuthorization(player.getUniqueId());
        sessions.open(rotatedTool);
        authorizeMainHand(player);
        int currentCount = countCurrent(
            player,
            rotatedTool.ownerUuid(),
            rotatedTool
        );
        DeliveryOutcome classification = ReissueDeliveryPolicy.classify(
            true,
            true,
            true,
            currentCount,
            player.getInventory().firstEmpty() >= 0
        );
        if (!ReissueDeliveryPolicy.createsOneItem(classification)) {
            return classification;
        }

        int emptySlot = player.getInventory().firstEmpty();
        ReissueDeliveryPolicy.ReissueDeliveryIdentity identity =
            ReissueDeliveryPolicy.identity(rotatedTool);
        ItemStack item = new ItemStack(Material.WOODEN_PICKAXE);
        applyEvolution(item, rotatedTool, false);
        if (item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(identity.damage());
            item.setItemMeta(damageable);
        }
        writeIdentity(item, identity, rotatedTool, "GROWTH_TOOL");
        player.getInventory().setItem(emptySlot, item);
        authorizeMainHand(player);
        return DeliveryOutcome.DELIVERED;
    }

    @Override
    public void notifyReissueDelivered(UUID playerUuid) {
        notifyDeliverySuccess(playerUuid);
    }

    public CompletionStage<Void> refreshSessionFromAuthority(UUID playerUuid) {
        return refreshSession(playerUuid);
    }

    public CompletionStage<Optional<GrowthTool>> inspect(UUID playerUuid) {
        return services.tasks().database(() ->
            repository.findByOwner(playerUuid)
        );
    }

    public CompletionStage<GrowthToolDeliveryCoordinator.Outcome> retryDelivery(
        UUID playerUuid
    ) {
        return delivery.onJoin(playerUuid).thenCompose(outcome ->
            notifyDelivery(playerUuid, outcome)
                .thenCompose(ignored -> refreshSession(playerUuid))
                .thenApply(ignored -> outcome)
        );
    }

    public CompletionStage<AdminMutation> revoke(UUID playerUuid) {
        return beginAuthorityRequest(playerUuid).thenCompose(request -> {
            if (request.isEmpty()) {
                return CompletableFuture.completedFuture(
                    AdminMutation.UNAVAILABLE
                );
            }
            return replaceAuthority(request.orElseThrow(), false);
        });
    }

    public CompletionStage<AdminMutation> reissue(UUID playerUuid) {
        return beginAuthorityRequest(playerUuid).thenCompose(request -> {
            if (request.isEmpty()) {
                return CompletableFuture.completedFuture(
                    AdminMutation.UNAVAILABLE
                );
            }
            AuthorityRequest authorityRequest = request.orElseThrow();
            return replaceAuthority(authorityRequest, true)
                .thenCompose(result -> {
                    if (result != AdminMutation.APPLIED) {
                        return CompletableFuture.completedFuture(result);
                    }
                    return startAdminDelivery(authorityRequest, playerUuid)
                        .thenCompose(started -> {
                            if (started.isEmpty()) {
                                return CompletableFuture.completedFuture(
                                    AdminMutation.SUPERSEDED
                                );
                            }
                            return notifyDelivery(
                                playerUuid,
                                started.orElseThrow(),
                                authorityRequest
                            ).thenCompose(notified -> {
                                if (!notified) {
                                    return CompletableFuture.completedFuture(
                                        AdminMutation.SUPERSEDED
                                    );
                                }
                                return refreshSession(authorityRequest)
                                    .thenCompose(ignored ->
                                        currentOnMainThread(authorityRequest)
                                    )
                                    .thenApply(current -> current
                                        ? AdminMutation.APPLIED
                                        : AdminMutation.SUPERSEDED);
                            });
                        });
                });
        });
    }

    public Optional<RepairSnapshot> repairSnapshot(Player player, GrowthTool tool) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!wayfarerTool(item)
            || !currentMainHandAllows(player, tool.status())) {
            return Optional.empty();
        }
        if (tool.status() == GrowthTool.Status.BROKEN) {
            int maximum = Math.max(1, tool.storedDamage());
            return Optional.of(new RepairSnapshot(maximum, maximum));
        }
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return Optional.empty();
        }
        return Optional.of(new RepairSnapshot(
            damageable.getDamage(),
            Math.max(1, item.getType().getMaxDurability())
        ));
    }

    public boolean applyFullRepair(
        UUID playerUuid,
        UUID toolId,
        long instanceEpoch,
        UUID repairId
    ) {
        if (!accepting) {
            return false;
        }
        Player player = plugin.getServer().getPlayer(playerUuid);
        GrowthTool current = sessions.current(playerUuid).orElse(null);
        if (player == null || current == null
            || !current.toolId().equals(toolId)
            || current.instanceEpoch() != instanceEpoch
            || current.status() == GrowthTool.Status.REVOKED
            || !wayfarerTool(player.getInventory().getItemInMainHand())
            || !currentMainHandAllows(player, current.status())) {
            return false;
        }
        ItemStack target = player.getInventory().getItemInMainHand();
        GrowthTool repaired = sessions.update(
            playerUuid,
            value -> value.repaired(clock.instant())
        );
        applyEvolution(target, repaired);
        if (target.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            target.setItemMeta(damageable);
        }
        authorizeMainHand(player);
        checkpoints.checkpoint(playerUuid);
        return true;
    }

    public boolean switchBranch(Player player, GrowthTool.Branch branch) {
        GrowthTool current = sessions.current(player.getUniqueId()).orElse(null);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (current == null
            || current.status() != GrowthTool.Status.ACTIVE
            || !wayfarerTool(held)
            || !currentMainHandAllows(player, GrowthTool.Status.ACTIVE)) {
            return false;
        }
        GrowthTool updated = sessions.update(
            player.getUniqueId(),
            tool -> tool.withBranch(branch, clock.instant())
        );
        applyEvolution(held, updated);
        authorizeMainHand(player);
        checkpoints.checkpoint(player.getUniqueId());
        return true;
    }

    public boolean debug(Player player, String action) {
        GrowthTool current = sessions.current(player.getUniqueId()).orElse(null);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (current == null) {
            return false;
        }
        if ("repair-free".equals(action)) {
            return applyFullRepair(
                player.getUniqueId(),
                current.toolId(),
                current.instanceEpoch(),
                UUID.randomUUID()
            );
        }
        if (current.status() != GrowthTool.Status.ACTIVE
            || !wayfarerTool(held)
            || !currentMainHandAllows(player, GrowthTool.Status.ACTIVE)) {
            return false;
        }
        boolean applied = switch (action) {
            case "progress-next" -> {
                EvolutionPlan.EvolutionSnapshot snapshot =
                    config.evolutionPlan().evaluate(
                        current.cumulativeProgressUnits(),
                        current.branch(),
                        config.enchantmentCaps()
                    );
                Long next = snapshot.nextThresholdUnits();
                if (next == null || next <= current.cumulativeProgressUnits()) {
                    yield false;
                }
                GrowthTool updated = sessions.addProgress(
                    player.getUniqueId(),
                    Math.subtractExact(
                        next,
                        current.cumulativeProgressUnits()
                    ),
                    clock.instant()
                );
                applyEvolution(held, updated);
                authorizeMainHand(player);
                checkpoints.checkpoint(player.getUniqueId());
                yield true;
            }
            case "durability-one" -> {
                if (!(held.getItemMeta() instanceof Damageable damageable)) {
                    yield false;
                }
                damageable.setDamage(
                    Math.max(0, held.getType().getMaxDurability() - 1)
                );
                held.setItemMeta(damageable);
                yield true;
            }
            default -> false;
        };
        if (applied) {
            authorizeMainHand(player);
            recordAdmin(current, "GROWTH_TOOL_DEBUG_MUTATION")
                .exceptionally(ignored -> null);
        }
        return applied;
    }

    @Override
    public void close() {
        stopAndFlush();
    }

    private void open(Player player) {
        UUID playerUuid = player.getUniqueId();
        authorizeMainHand(player);
        delivery.onJoin(playerUuid)
            .thenCompose(outcome -> notifyDelivery(playerUuid, outcome))
            .thenCompose(ignored -> services.tasks().database(() ->
                repository.findOrCreate(playerUuid, clock.instant())
            ))
            .thenCompose(tool -> services.tasks().mainThread(() -> {
                if (!accepting) {
                    return;
                }
                Player online = plugin.getServer().getPlayer(playerUuid);
                if (online == null || !online.isOnline()) {
                    return;
                }
                invalidateAuthorization(playerUuid);
                sessions.open(tool);
                reconcileInventory(online, tool);
                authorizeMainHand(online);
            }))
            .exceptionally(ignored -> null);
    }

    private CompletionStage<AdminMutation> replaceAuthority(
        AuthorityRequest request,
        boolean reissue
    ) {
        UUID playerUuid = request.playerUuid();
        return services.tasks().database(() -> {
                GrowthTool current = repository.findByOwner(playerUuid).orElse(null);
                if (current == null) {
                    return new AuthorityMutation(null, AdminMutation.NOT_FOUND);
                }
                GrowthTool next = reissue
                    ? current.reissued(clock.instant())
                    : current.revoked(clock.instant());
                if (next == current) {
                    return new AuthorityMutation(current, AdminMutation.NO_CHANGE);
                }
                Optional<GrowthTool> persisted = repository.replaceAuthority(
                    next,
                    current.lockVersion(),
                    clock.instant()
                );
                return persisted
                    .map(value -> new AuthorityMutation(value, AdminMutation.APPLIED))
                    .orElseGet(() ->
                        new AuthorityMutation(null, AdminMutation.CONFLICT)
                    );
            })
            .thenCompose(mutation -> applyAuthorityMutation(
                request,
                mutation,
                reissue
            ))
            .exceptionallyCompose(ignored ->
                recoverAuthoritativeState(request, false)
                    .thenApply(recovered -> recovered
                        ? AdminMutation.UNAVAILABLE
                        : AdminMutation.SUPERSEDED)
            );
    }

    private CompletionStage<Void> refreshSession(UUID playerUuid) {
        return beginAuthorityRequest(playerUuid).thenCompose(request -> {
            if (request.isEmpty()) {
                return CompletableFuture.completedFuture(null);
            }
            return refreshSession(request.orElseThrow());
        });
    }

    private CompletionStage<Void> refreshSession(AuthorityRequest request) {
        UUID playerUuid = request.playerUuid();
        return services.tasks().database(() ->
            repository.findByOwner(playerUuid)
        )
            .thenCompose(found -> applyAuthoritativeState(
                request,
                found == null ? Optional.empty() : found,
                true
            ))
            .thenApply(ignored -> (Void) null)
            .exceptionallyCompose(ignored ->
                recoverAuthoritativeState(request, true)
                    .thenApply(ignoredRecovery -> null)
            );
    }

    private CompletionStage<Optional<AuthorityRequest>> beginAuthorityRequest(
        UUID playerUuid
    ) {
        java.util.Objects.requireNonNull(playerUuid, "playerUuid");
        AtomicReference<Optional<AuthorityRequest>> request =
            new AtomicReference<>(Optional.empty());
        return services.tasks().mainThread(() -> {
            if (!accepting) {
                return;
            }
            long generation = authorityRequestSequence.incrementAndGet();
            currentAuthorityRequests.put(playerUuid, generation);
            invalidateAuthorization(playerUuid);
            request.set(Optional.of(new AuthorityRequest(
                playerUuid,
                generation
            )));
        }).thenApply(ignored -> request.get());
    }

    private boolean isCurrent(AuthorityRequest request) {
        Long current = currentAuthorityRequests.get(request.playerUuid());
        return accepting
            && current != null
            && current.longValue() == request.requestGeneration();
    }

    private CompletionStage<Boolean> currentOnMainThread(
        AuthorityRequest request
    ) {
        AtomicBoolean current = new AtomicBoolean();
        return services.tasks().mainThread(() ->
            current.set(isCurrent(request))
        ).thenApply(ignored -> current.get());
    }

    private CompletionStage<AdminMutation> applyAuthorityMutation(
        AuthorityRequest request,
        AuthorityMutation mutation,
        boolean reissue
    ) {
        if (mutation.tool() == null
            && mutation.result() == AdminMutation.CONFLICT) {
            return recoverAuthoritativeState(request, false)
                .thenApply(current -> current
                    ? mutation.result()
                    : AdminMutation.SUPERSEDED);
        }
        if (mutation.tool() == null) {
            return applyAuthoritativeState(
                request,
                Optional.empty(),
                false
            ).thenApply(current -> current
                ? mutation.result()
                : AdminMutation.SUPERSEDED);
        }
        return applyAuthoritativeState(
            request,
            Optional.of(mutation.tool()),
            false
        ).thenCompose(current -> recordAdmin(
            mutation.tool(),
            reissue ? "GROWTH_TOOL_REISSUED" : "GROWTH_TOOL_REVOKED"
        ).handle((recorded, failure) -> current
            ? mutation.result()
            : AdminMutation.SUPERSEDED));
    }

    private CompletionStage<Boolean> recoverAuthoritativeState(
        AuthorityRequest request,
        boolean reconcile
    ) {
        CompletionStage<Optional<GrowthTool>> load =
            services.tasks().database(() ->
                repository.findByOwner(request.playerUuid())
            );
        return load
            .handle((found, failure) ->
                failure == null && found != null
                    ? found
                    : Optional.<GrowthTool>empty()
            )
            .thenCompose(found -> applyAuthoritativeState(
                request,
                found,
                reconcile
            ))
            .exceptionally(ignored -> false);
    }

    private CompletionStage<Boolean> applyAuthoritativeState(
        AuthorityRequest request,
        Optional<GrowthTool> found,
        boolean reconcile
    ) {
        Optional<GrowthTool> authoritative = found == null
            ? Optional.empty()
            : found;
        AtomicBoolean current = new AtomicBoolean();
        return services.tasks().mainThread(() -> {
            if (!isCurrent(request)) {
                return;
            }
            current.set(true);
            UUID playerUuid = request.playerUuid();
            Player online = plugin.getServer().getPlayer(playerUuid);
            if (authoritative.isEmpty()) {
                sessions.close(playerUuid);
                if (online != null && online.isOnline()) {
                    invalidateAuthorization(playerUuid);
                } else {
                    heldAuthorizations.remove(playerUuid);
                }
                return;
            }
            if (online == null || !online.isOnline()) {
                return;
            }
            GrowthTool tool = authoritative.orElseThrow();
            GrowthTool loaded = sessions.current(playerUuid).orElse(null);
            if (loaded != null
                && loaded.lockVersion() > tool.lockVersion()) {
                authorizeMainHand(online);
                return;
            }
            if (loaded != null
                && loaded.lockVersion() == tool.lockVersion()) {
                if (!sameAuthorityFields(loaded, tool)) {
                    invalidateAuthorization(playerUuid);
                    return;
                }
                authorizeMainHand(online);
                return;
            }
            sessions.open(tool);
            if (reconcile) {
                reconcileInventory(online, tool);
            }
            authorizeMainHand(online);
        }).thenApply(ignored -> current.get());
    }

    private CompletionStage<Optional<GrowthToolDeliveryCoordinator.Outcome>>
        startAdminDelivery(
            AuthorityRequest request,
            UUID playerUuid
        ) {
        AtomicReference<CompletionStage<GrowthToolDeliveryCoordinator.Outcome>>
            started = new AtomicReference<>();
        return services.tasks().mainThread(() -> {
            if (isCurrent(request)) {
                started.set(delivery.onJoin(playerUuid));
            }
        }).thenCompose(ignored -> {
            CompletionStage<GrowthToolDeliveryCoordinator.Outcome> stage =
                started.get();
            if (stage == null) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return stage.thenApply(Optional::of);
        });
    }

    private CompletionStage<Boolean> notifyDelivery(
        UUID playerUuid,
        GrowthToolDeliveryCoordinator.Outcome outcome,
        AuthorityRequest request
    ) {
        AtomicBoolean current = new AtomicBoolean();
        return services.tasks().mainThread(() -> {
            if (!isCurrent(request)) {
                return;
            }
            current.set(true);
            if (outcome == GrowthToolDeliveryCoordinator.Outcome.DELIVERED) {
                notifyDeliverySuccess(playerUuid);
                return;
            }
            if (outcome != GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL
                && outcome
                    != GrowthToolDeliveryCoordinator.Outcome.PLAYER_OFFLINE
                && outcome != GrowthToolDeliveryCoordinator.Outcome.CONFLICT
                && outcome != GrowthToolDeliveryCoordinator.Outcome.UNAVAILABLE) {
                return;
            }
            String reference = delivery.lastFailure(playerUuid)
                .map(snapshot -> " Reference: " + snapshot.correlationId() + ".")
                .orElse("");
            plugin.getLogger().warning(
                "Growth Tool delivery remains pending; retry on join or by admin."
                    + reference
            );
            Player online = plugin.getServer().getPlayer(playerUuid);
            if (online != null && online.isOnline()) {
                online.sendMessage(
                    "Growth Tool delivery is pending; free inventory space "
                        + "and rejoin or ask an administrator to retry."
                        + (reference.isEmpty() ? "" : reference)
                );
            }
        }).thenApply(ignored -> current.get());
    }

    private static boolean sameAuthorityFields(
        GrowthTool first,
        GrowthTool second
    ) {
        return first.toolId().equals(second.toolId())
            && first.itemInstanceId().equals(second.itemInstanceId())
            && first.instanceEpoch() == second.instanceEpoch()
            && first.status() == second.status();
    }

    private CompletionStage<Void> notifyDelivery(
        UUID playerUuid,
        GrowthToolDeliveryCoordinator.Outcome outcome
    ) {
        if (outcome == GrowthToolDeliveryCoordinator.Outcome.DELIVERED) {
            return notifyDeliverySuccessStage(playerUuid);
        }
        if (outcome != GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL
            && outcome != GrowthToolDeliveryCoordinator.Outcome.PLAYER_OFFLINE
            && outcome != GrowthToolDeliveryCoordinator.Outcome.CONFLICT
            && outcome != GrowthToolDeliveryCoordinator.Outcome.UNAVAILABLE) {
            return CompletableFuture.completedFuture(null);
        }
        String reference = delivery.lastFailure(playerUuid)
            .map(snapshot -> " Reference: " + snapshot.correlationId() + ".")
            .orElse("");
        plugin.getLogger().warning(
            "Growth Tool delivery remains pending; retry on join or by admin."
                + reference
        );
        return services.tasks().mainThread(() -> {
            if (!accepting) {
                return;
            }
            Player online = plugin.getServer().getPlayer(playerUuid);
            if (online != null && online.isOnline()) {
                online.sendMessage(
                    "Growth Tool delivery is pending; free inventory space "
                        + "and rejoin or ask an administrator to retry."
                        + (reference.isEmpty() ? "" : reference)
                );
            }
        });
    }

    private CompletionStage<Void> notifyDeliverySuccessStage(UUID playerUuid) {
        return services.tasks().mainThread(() -> notifyDeliverySuccess(playerUuid));
    }

    private void notifyDeliverySuccess(UUID playerUuid) {
        if (!accepting) {
            return;
        }
        Player online = plugin.getServer().getPlayer(playerUuid);
        if (online != null && online.isOnline()) {
            online.sendMessage(GrowthToolDeliveryPresentation.SUCCESS_MESSAGE);
        }
    }

    private void logDeliveryFailure(
        UUID ignoredPlayerUuid,
        String correlationId,
        GrowthToolDeliveryCoordinator.DiagnosticStage stage,
        Throwable failure
    ) {
        plugin.getLogger().log(
            Level.WARNING,
            "Growth Tool delivery failed; correlation=" + correlationId
                + "; stage=" + stage
                + "; exception=" + failure.getClass().getName(),
            failure
        );
    }

    private void authorizeMainHand(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        UUID playerUuid = player.getUniqueId();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!wayfarerTool(item)) {
            heldAuthorizations.put(
                playerUuid,
                new HeldGrowthToolAuthorization(
                    HeldGrowthToolAuthorization.State.NO_MANAGED_ITEM
                )
            );
            return;
        }
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        heldAuthorizations.put(
            playerUuid,
            HeldGrowthToolAuthorizer.authorize(
                true,
                claimResult(item),
                playerUuid,
                tool
            )
        );
        if (tool != null
            && authorization(playerUuid).state()
                == HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER) {
            applyEvolution(item, tool, false);
        }
    }

    private void scheduleAuthorization(Player player) {
        plugin.getServer().getScheduler().runTask(
            plugin,
            () -> authorizeMainHand(player)
        );
    }

    private void invalidateAndScheduleAuthorization(Player player) {
        if (player == null) {
            return;
        }
        invalidateAuthorization(player.getUniqueId());
        scheduleAuthorization(player);
    }

    private void invalidateAuthorization(UUID playerUuid) {
        heldAuthorizations.put(
            playerUuid,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE
            )
        );
    }

    private HeldGrowthToolAuthorization authorization(UUID playerUuid) {
        return heldAuthorizations.getOrDefault(
            playerUuid,
            new HeldGrowthToolAuthorization(
                HeldGrowthToolAuthorization.State.AUTHORITY_UNAVAILABLE
            )
        );
    }

    private static boolean allowsForStatus(
        HeldGrowthToolAuthorization authorization,
        GrowthTool.Status status
    ) {
        if (authorization == null || status == null) {
            return false;
        }
        return switch (status) {
            case ACTIVE -> authorization.state()
                == HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER;
            case BROKEN -> authorization.state()
                == HeldGrowthToolAuthorization.State.VALID_BROKEN_OWNER;
            case REVOKED -> false;
        };
    }

    /**
     * Re-checks the immutable physical claim at security-sensitive Main
     * entry points. The held capability remains a fast cache, but it must
     * agree with the item actually present in Main Hand before a mutation or
     * GUI action can proceed.
     */
    private boolean currentMainHandAllows(
        Player player,
        GrowthTool.Status status
    ) {
        if (player == null || status == null) {
            return false;
        }
        GrowthTool current = sessions.current(player.getUniqueId()).orElse(null);
        if (current == null || current.status() != status) {
            return false;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!wayfarerTool(item)) {
            return false;
        }
        HeldGrowthToolAuthorization actual = HeldGrowthToolAuthorizer.authorize(
            true,
            claimResult(item),
            player.getUniqueId(),
            current
        );
        return allowsForStatus(actual, status)
            && actual.state() == authorization(player.getUniqueId()).state();
    }

    @SuppressWarnings("deprecation")
    private void reconcileInventory(Player player, GrowthTool tool) {
        if (tool.status() == GrowthTool.Status.REVOKED) {
            return;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || !canonicalIdentity(item, tool)) {
                continue;
            }
            if (tool.status() == GrowthTool.Status.ACTIVE) {
                applyEvolution(item, tool, false);
            } else if (tool.status() == GrowthTool.Status.BROKEN) {
                if (item.getType() != Material.GRAY_DYE) {
                    item.setType(Material.GRAY_DYE);
                }
                writeIdentity(item, tool, "BROKEN_GROWTH_TOOL");
            }
        }
    }

    private void openMainGui(
        Player player,
        GrowthTool tool,
        ItemStack physicalItem
    ) {
        GrowthToolGuiHolder holder = new GrowthToolGuiHolder(
            GuiMode.MAIN,
            UUID.randomUUID()
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Growth Tool")
        );
        holder.inventory = inventory;
        EvolutionPlan.EvolutionSnapshot evolution =
            config.evolutionPlan().evaluate(
                tool.cumulativeProgressUnits(),
                tool.branch(),
                config.enchantmentCaps()
            );
        RepairSnapshot repair = repairSnapshot(player, tool).orElse(null);
        String durability = repair == null
            ? "Unavailable"
            : (repair.maximumDurability() - repair.damage())
                + "/" + repair.maximumDurability();
        long remaining = evolution.nextThresholdUnits() == null
            ? 0
            : Math.max(
                0,
                evolution.nextThresholdUnits()
                    - tool.cumulativeProgressUnits()
            );
        inventory.setItem(4, physicalItem.clone());
        inventory.setItem(10, named(
            Material.BOOK,
            "Tool Status",
            List.of(
                "Type: " + GrowthTool.TOOL_TYPE,
                "Status: " + tool.status(),
                "Material: " + physicalItem.getType(),
                "Evolution: " + evolution.evolutionCount()
            )
        ));
        inventory.setItem(12, named(
            Material.EXPERIENCE_BOTTLE,
            "Progress",
            List.of(
                "Cumulative: " + tool.cumulativeProgressUnits(),
                "Until next: " + remaining,
                "Branch: " + tool.branch()
            )
        ));
        inventory.setItem(14, named(
            Material.ANVIL,
            "Repair Preview",
            List.of(
                "Durability: " + durability,
                "Open preview and confirmation"
            )
        ));
        inventory.setItem(16, named(
            Material.PAPER,
            "Help / Effective State",
            List.of(
                "Efficiency: " + evolution.efficiency(),
                "Unbreaking: " + evolution.unbreaking(),
                "Fortune: " + evolution.fortune(),
                "Silk Touch: " + evolution.silkTouch(),
                "Config clamp: active"
            )
        ));
        player.openInventory(inventory);
    }

    private void openRepairPreview(Player player) {
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        RepairCoordinator repairs = repairCoordinator;
        if (tool == null || repairs == null || repairInFlight.contains(
            player.getUniqueId()
        )) {
            player.sendMessage("Growth Tool repair is unavailable.");
            return;
        }
        RepairSnapshot snapshot = repairSnapshot(player, tool).orElse(null);
        if (snapshot == null) {
            player.sendMessage("Your current bound Growth Tool is unavailable.");
            return;
        }
        int evolutionCount = config.evolutionPlan().evaluate(
            tool.cumulativeProgressUnits(),
            tool.branch(),
            config.enchantmentCaps()
        ).evolutionCount();
        var quote = config.repairPricing().quote(
            tool.status(),
            evolutionCount,
            snapshot.damage(),
            snapshot.maximumDurability()
        );
        if (!quote.available()) {
            player.sendMessage("Growth Tool repair is not required.");
            return;
        }
        UUID token = UUID.randomUUID();
        RepairGuiSession session = new RepairGuiSession(
            token,
            tool.toolId(),
            tool.itemInstanceId(),
            tool.instanceEpoch(),
            tool.displayRevision(),
            snapshot.damage(),
            snapshot.maximumDurability(),
            quote.amountWaymark(),
            new SingleUseGate()
        );
        repairGuiSessions.put(player.getUniqueId(), session);
        GrowthToolGuiHolder holder = new GrowthToolGuiHolder(
            GuiMode.REPAIR_PREVIEW,
            token
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Growth Tool Repair")
        );
        holder.inventory = inventory;
        inventory.setItem(11, named(
            Material.LIME_CONCRETE,
            "Confirm Repair",
            List.of(
                "Cost: " + quote.amountWaymark() + " WM",
                "Damage: " + snapshot.damage() + "/"
                    + snapshot.maximumDurability()
            )
        ));
        inventory.setItem(13, named(
            Material.ANVIL,
            "Repair Quote",
            List.of(
                "Status: " + tool.status(),
                "Evolution: " + evolutionCount,
                "This quote is revalidated on confirm"
            )
        ));
        inventory.setItem(15, named(
            Material.BARRIER,
            "Cancel",
            List.of("No Waymark will be charged")
        ));
        player.openInventory(inventory);
    }

    private void handleGuiClick(
        Player player,
        int rawSlot,
        GrowthToolGuiHolder holder
    ) {
        if (rawSlot < 0 || rawSlot >= 27) {
            return;
        }
        if (holder.mode() == GuiMode.MAIN) {
            switch (GrowthToolGuiAction.main(rawSlot)) {
                case OPEN_REPAIR_PREVIEW -> openRepairPreview(player);
                case HELP -> player.sendMessage(
                    "Progress evolves the bound tool; repair requires "
                        + "preview and confirmation."
                );
                default -> {
                    // Display-only slot.
                }
            }
            return;
        }
        switch (GrowthToolGuiAction.repairPreview(rawSlot)) {
            case CANCEL -> {
                repairGuiSessions.remove(player.getUniqueId());
                player.closeInventory();
                return;
            }
            case CONFIRM_REPAIR -> {
                // Continue through the authoritative session checks below.
            }
            default -> {
                return;
            }
        }
        RepairGuiSession session = repairGuiSessions.get(
            player.getUniqueId()
        );
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        RepairCoordinator repairs = repairCoordinator;
        if (session == null || tool == null || repairs == null
            || !session.token().equals(holder.token())
            || !session.toolId().equals(tool.toolId())
            || !session.itemInstanceId().equals(tool.itemInstanceId())
            || session.instanceEpoch() != tool.instanceEpoch()
            || session.displayRevision() != tool.displayRevision()
            || !session.accepted().tryAcquire()) {
            player.sendMessage("Growth Tool repair quote expired.");
            return;
        }
        RepairSnapshot current = repairSnapshot(player, tool).orElse(null);
        if (current == null || current.damage() != session.damage()
            || current.maximumDurability() != session.maximumDurability()) {
            repairGuiSessions.remove(player.getUniqueId(), session);
            player.sendMessage("Growth Tool repair quote changed; reopen it.");
            return;
        }
        var quote = config.repairPricing().quote(
            tool.status(),
            config.evolutionPlan().evaluate(
                tool.cumulativeProgressUnits(),
                tool.branch(),
                config.enchantmentCaps()
            ).evolutionCount(),
            current.damage(),
            current.maximumDurability()
        );
        if (!quote.available() || quote.amountWaymark() != session.amountWaymark()
            || !repairInFlight.add(player.getUniqueId())) {
            repairGuiSessions.remove(player.getUniqueId(), session);
            player.sendMessage("Growth Tool repair quote is unavailable.");
            return;
        }
        repairGuiSessions.remove(player.getUniqueId(), session);
        player.closeInventory();
        String key = "main-repair:" + tool.toolId() + ":"
            + tool.itemInstanceId() + ":" + tool.instanceEpoch() + ":"
            + tool.displayRevision() + ":" + current.damage();
        repairs.repair(new RepairCoordinator.Request(
            key,
            player.getUniqueId(),
            tool.toolId(),
            tool.instanceEpoch(),
            quote.amountWaymark()
        )).whenComplete((result, failure) ->
            services.tasks().mainThread(() -> {
                repairInFlight.remove(player.getUniqueId());
                Player online = plugin.getServer().getPlayer(
                    player.getUniqueId()
                );
                if (online != null && online.isOnline()) {
                    online.sendMessage(
                        failure == null
                            ? "Growth Tool repair: " + result.status()
                            : "Growth Tool repair is unavailable."
                    );
                }
            })
        );
    }

    private static ItemStack named(
        Material material,
        String name,
        List<String> lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(net.kyori.adventure.text.Component.text(name));
        meta.lore(lore.stream()
            .map(net.kyori.adventure.text.Component::text)
            .toList());
        item.setItemMeta(meta);
        return item;
    }

    private CompletionStage<Void> recordAdmin(
        GrowthTool tool,
        String eventType
    ) {
        return services.audit().record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            eventType,
            tool.ownerUuid(),
            "GROWTH_TOOL",
            tool.toolId().toString(),
            services.serverId(),
            "{\"result\":\"APPLIED\"}",
            clock.instant()
        ));
    }

    private GrowthToolDeliveryCoordinator.Outcome deliver(GrowthTool tool) {
        if (!accepting) {
            return GrowthToolDeliveryCoordinator.Outcome.UNAVAILABLE;
        }
        Player player = plugin.getServer().getPlayer(tool.ownerUuid());
        if (player == null || !player.isOnline()) {
            return GrowthToolDeliveryCoordinator.Outcome.PLAYER_OFFLINE;
        }
        if (countCurrent(player, tool.ownerUuid(), tool) > 0) {
            return GrowthToolDeliveryCoordinator.Outcome.ALREADY_PRESENT;
        }
        if (player.getInventory().firstEmpty() < 0) {
            return GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL;
        }
        ItemStack item;
        try {
            item = new ItemStack(Material.WOODEN_PICKAXE);
            writeIdentity(item, tool, "GROWTH_TOOL");
            applyEvolution(item, tool, false);
        } catch (RuntimeException failure) {
            throw new GrowthToolDeliveryCoordinator.DeliveryStageException(
                GrowthToolDeliveryCoordinator.DiagnosticStage.CREATE_AND_ANNOTATE_ITEM,
                failure
            );
        }
        try {
            if (!player.getInventory().addItem(item).isEmpty()) {
                return GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL;
            }
        } catch (RuntimeException failure) {
            throw new GrowthToolDeliveryCoordinator.DeliveryStageException(
                GrowthToolDeliveryCoordinator.DiagnosticStage.INSERT_PHYSICAL_ITEM,
                failure
            );
        }
        return GrowthToolDeliveryCoordinator.Outcome.DELIVERED;
    }

    @SuppressWarnings("deprecation")
    private void applyEvolution(ItemStack item, GrowthTool tool) {
        applyEvolution(item, tool, false);
    }

    @SuppressWarnings("deprecation")
    private void applyEvolution(
        ItemStack item,
        GrowthTool tool,
        boolean evolutionCountIncreased
    ) {
        EvolutionPlan.EvolutionSnapshot snapshot = config.evolutionPlan().evaluate(
            tool.cumulativeProgressUnits(),
            tool.branch(),
            config.enchantmentCaps()
        );
        Material expectedMaterial = switch (snapshot.material()) {
            case WOOD -> Material.WOODEN_PICKAXE;
            case STONE -> Material.STONE_PICKAXE;
            case IRON -> Material.IRON_PICKAXE;
            case DIAMOND -> Material.DIAMOND_PICKAXE;
        };
        int oldMaximum = Math.max(1, item.getType().getMaxDurability());
        int oldDamage = item.getItemMeta() instanceof Damageable damageable
            ? damageable.getDamage()
            : 0;
        boolean materialChanged = item.getType() != expectedMaterial;
        boolean enchantmentsChanged =
            item.getEnchantmentLevel(Enchantment.EFFICIENCY)
                != snapshot.efficiency()
                || item.getEnchantmentLevel(Enchantment.UNBREAKING)
                != snapshot.unbreaking()
                || item.getEnchantmentLevel(Enchantment.FORTUNE)
                != snapshot.fortune()
                || item.getEnchantmentLevel(Enchantment.SILK_TOUCH)
                != snapshot.silkTouch();
        boolean presentationChanged = !hasExpectedDisplayName(
            item,
            GrowthToolDeliveryPresentation.displayName(
                GrowthToolPhysicalClaim.ItemType.GROWTH_TOOL
            )
        );
        if (!materialChanged && !enchantmentsChanged
            && !evolutionCountIncreased
            && !presentationChanged
            && canonicalIdentity(item, tool)) {
            return;
        }
        if (materialChanged) {
            item.setType(expectedMaterial);
        }
        item.removeEnchantment(Enchantment.EFFICIENCY);
        item.removeEnchantment(Enchantment.UNBREAKING);
        item.removeEnchantment(Enchantment.FORTUNE);
        item.removeEnchantment(Enchantment.SILK_TOUCH);
        add(item, Enchantment.EFFICIENCY, snapshot.efficiency());
        add(item, Enchantment.UNBREAKING, snapshot.unbreaking());
        add(item, Enchantment.FORTUNE, snapshot.fortune());
        add(item, Enchantment.SILK_TOUCH, snapshot.silkTouch());
        if (item.getItemMeta() instanceof Damageable damageable) {
            int maximum = Math.max(1, item.getType().getMaxDurability());
            int damage;
            if (evolutionCountIncreased) {
                damage = DurabilitySemantics.afterEvolution(
                    oldDamage,
                    true
                );
            } else if (materialChanged && oldDamage < oldMaximum) {
                damage = DurabilitySemantics.reconcileActive(
                    oldMaximum,
                    oldDamage,
                    maximum
                );
            } else {
                damage = Math.min(oldDamage, maximum - 1);
            }
            damageable.setDamage(damage);
            item.setItemMeta(damageable);
        }
        writeIdentity(item, tool, "GROWTH_TOOL");
    }

    private static boolean canonicalIdentity(ItemStack item, GrowthTool tool) {
        GrowthToolPhysicalClaim parsed = claim(item).orElse(null);
        return parsed != null
            && parsed.validate(tool.ownerUuid(), tool)
                == GrowthToolPhysicalClaim.Validation.VALID;
    }

    private static boolean currentPhysical(
        ItemStack item,
        UUID ownerUuid,
        GrowthTool authority
    ) {
        return item != null
            && canonicalIdentity(item, authority)
            && authority.ownerUuid().equals(ownerUuid);
    }

    private static int countCurrent(
        Player player,
        UUID ownerUuid,
        GrowthTool authority
    ) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (currentPhysical(item, ownerUuid, authority)) {
                count++;
            }
        }
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (currentPhysical(item, ownerUuid, authority)) {
                count++;
            }
        }
        if (currentPhysical(
            player.getInventory().getItemInOffHand(),
            ownerUuid,
            authority
        )) {
            count++;
        }
        if (currentPhysical(
            player.getOpenInventory().getCursor(),
            ownerUuid,
            authority
        )) {
            count++;
        }
        return count;
    }

    private static void add(ItemStack item, Enchantment enchantment, int level) {
        if (level > 0) {
            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    private static boolean containsWayfarerTool(ItemStack[] contents) {
        for (ItemStack item : contents) {
            if (wayfarerTool(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean wayfarerTool(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isManagedDeathType(text(item, ITEM_TYPE));
    }

    static boolean isManagedDeathType(String itemType) {
        return "GROWTH_TOOL".equals(itemType)
            || "BROKEN_GROWTH_TOOL".equals(itemType);
    }

    private static void writeIdentity(
        ItemStack item,
        GrowthTool tool,
        String itemType
    ) {
        writeIdentity(
            item,
            ReissueDeliveryPolicy.identity(tool),
            tool,
            itemType
        );
    }

    private static void writeIdentity(
        ItemStack item,
        ReissueDeliveryPolicy.ReissueDeliveryIdentity identity,
        GrowthTool tool,
        String itemType
    ) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE, PersistentDataType.STRING, itemType);
        pdc.set(
            ITEM_INSTANCE_ID,
            PersistentDataType.STRING,
            identity.itemInstanceId().toString()
        );
        pdc.set(TOOL_ID, PersistentDataType.STRING, tool.toolId().toString());
        pdc.set(OWNER_ID, PersistentDataType.STRING, tool.ownerUuid().toString());
        pdc.set(TOOL_TYPE, PersistentDataType.STRING, GrowthTool.TOOL_TYPE);
        pdc.set(EPOCH, PersistentDataType.LONG, identity.instanceEpoch());
        pdc.set(SCHEMA, PersistentDataType.INTEGER, tool.schemaVersion());
        pdc.set(REVISION, PersistentDataType.LONG, tool.displayRevision());
        GrowthToolPhysicalClaim.ItemType presentationType =
            "BROKEN_GROWTH_TOOL".equals(itemType)
                ? GrowthToolPhysicalClaim.ItemType.BROKEN_GROWTH_TOOL
                : "GROWTH_TOOL".equals(itemType)
                    ? GrowthToolPhysicalClaim.ItemType.GROWTH_TOOL
                    : null;
        if (presentationType != null) {
            meta.displayName(
                GrowthToolDeliveryPresentation.displayName(presentationType)
            );
        }
        item.setItemMeta(meta);
    }

    private static Optional<GrowthToolPhysicalClaim> claim(ItemStack item) {
        return claimResult(item).claim();
    }

    private static GrowthToolPhysicalClaim.ParseResult claimResult(
        ItemStack item
    ) {
        java.util.Map<String, String> raw = new java.util.HashMap<>();
        put(raw, "item_type", text(item, ITEM_TYPE));
        put(raw, "item_instance_id", text(item, ITEM_INSTANCE_ID));
        put(raw, "tool_id", text(item, TOOL_ID));
        put(raw, "owner_uuid", text(item, OWNER_ID));
        put(raw, "tool_type", text(item, TOOL_TYPE));
        put(raw, "instance_epoch", numericText(item, EPOCH));
        put(raw, "schema_version", numericText(item, SCHEMA));
        put(raw, "display_revision", numericText(item, REVISION));
        return GrowthToolPhysicalClaim.parse(raw);
    }

    private static boolean hasExpectedDisplayName(
        ItemStack item,
        net.kyori.adventure.text.Component expected
    ) {
        return expected != null
            && item != null
            && item.hasItemMeta()
            && java.util.Objects.equals(item.getItemMeta().displayName(), expected);
    }

    private static void put(
        java.util.Map<String, String> values,
        String key,
        String value
    ) {
        if (value != null) {
            values.put(key, value);
        }
    }

    private static String numericText(ItemStack item, NamespacedKey key) {
        long value = number(item, key);
        return value == Long.MIN_VALUE ? null : Long.toString(value);
    }

    private static String text(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        return item.getItemMeta().getPersistentDataContainer().get(
            key,
            PersistentDataType.STRING
        );
    }

    private static long number(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) {
            return Long.MIN_VALUE;
        }
        PersistentDataContainer pdc =
            item.getItemMeta().getPersistentDataContainer();
        return number(pdc, key);
    }

    static long number(PersistentDataContainer pdc, NamespacedKey key) {
        if (pdc.has(key, PersistentDataType.LONG)) {
            Long longValue = pdc.get(key, PersistentDataType.LONG);
            if (longValue != null) {
                return longValue;
            }
        }
        if (pdc.has(key, PersistentDataType.INTEGER)) {
            Integer integer = pdc.get(key, PersistentDataType.INTEGER);
            if (integer != null) {
                return integer.longValue();
            }
        }
        return Long.MIN_VALUE;
    }

    private static String oreGroup(Material material) {
        String name = material.name().toUpperCase(Locale.ROOT);
        if (name.equals("NETHER_QUARTZ_ORE")) {
            return "NETHER_QUARTZ";
        }
        if (name.equals("NETHER_GOLD_ORE")) {
            return "NETHER_GOLD";
        }
        if (name.equals("ANCIENT_DEBRIS")) {
            return "ANCIENT_DEBRIS";
        }
        String simplified = name
            .replace("DEEPSLATE_", "")
            .replace("_ORE", "");
        return switch (simplified) {
            case "COAL", "COPPER", "REDSTONE", "IRON", "LAPIS", "GOLD",
                 "DIAMOND", "EMERALD" -> simplified;
            default -> null;
        };
    }

    private void checkpointAll() {
        for (UUID playerId : sessions.ownerUuids()) {
            checkpoints.checkpoint(playerId);
        }
    }

    public record RepairSnapshot(int damage, int maximumDurability) {}

    public enum AdminMutation {
        APPLIED,
        NO_CHANGE,
        NOT_FOUND,
        CONFLICT,
        SUPERSEDED,
        UNAVAILABLE
    }

    private record AuthorityRequest(
        UUID playerUuid,
        long requestGeneration
    ) {}

    private record AuthorityMutation(
        GrowthTool tool,
        AdminMutation result
    ) {}

    private enum GuiMode {
        MAIN,
        REPAIR_PREVIEW
    }

    private static final class GrowthToolGuiHolder
        implements InventoryHolder {
        private final GuiMode mode;
        private final UUID token;
        private Inventory inventory;

        private GrowthToolGuiHolder(GuiMode mode, UUID token) {
            this.mode = mode;
            this.token = token;
        }

        private GuiMode mode() {
            return mode;
        }

        private UUID token() {
            return token;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private record RepairGuiSession(
        UUID token,
        UUID toolId,
        UUID itemInstanceId,
        long instanceEpoch,
        long displayRevision,
        int damage,
        int maximumDurability,
        long amountWaymark,
        SingleUseGate accepted
    ) {}
}
