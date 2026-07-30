package io.github.eariver.wayfarer.main.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.main.application.GrowthCheckpointService;
import io.github.eariver.wayfarer.main.application.GrowthSessionStore;
import io.github.eariver.wayfarer.main.application.GrowthToolDeliveryCoordinator;
import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.application.RepairCoordinator;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.DurabilitySemantics;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim;
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
import org.bukkit.event.player.PlayerRespawnEvent;
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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MainGameplayRuntime implements Listener, AutoCloseable {
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
    private final ConcurrentHashMap<UUID, List<ItemStack>> deathRetained =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RepairGuiSession> repairGuiSessions =
        new ConcurrentHashMap<>();
    private final Set<UUID> repairInFlight = ConcurrentHashMap.newKeySet();
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
            clock
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
        open(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        checkpoints.checkpoint(playerId).whenComplete((ignored, failure) ->
            sessions.close(playerId)
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (repairInFlight.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (!config.progressPolicy().allowsWorld(player.getWorld().getName())
            || (player.getGameMode() != GameMode.SURVIVAL
                && player.getGameMode() != GameMode.ADVENTURE)
            || !Tag.MINEABLE_PICKAXE.isTagged(event.getBlock().getType())) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        if (tool == null || !canonical(item, player.getUniqueId(), tool)) {
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
        } catch (RuntimeException failure) {
            plugin.getLogger().severe("Growth progress update failed closed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onDamage(PlayerItemDamageEvent event) {
        if (repairInFlight.contains(event.getPlayer().getUniqueId())
            && wayfarerTool(event.getItem())) {
            event.setCancelled(true);
            return;
        }
        ItemStack item = event.getItem();
        if (!(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }
        GrowthTool tool = sessions.current(event.getPlayer().getUniqueId()).orElse(null);
        if (tool == null || !canonical(item, event.getPlayer().getUniqueId(), tool)
            || damageable.getDamage() + event.getDamage()
            < item.getType().getMaxDurability()) {
            return;
        }
        event.setCancelled(true);
        GrowthTool broken = sessions.update(
            event.getPlayer().getUniqueId(),
            current -> current.broken(item.getType().getMaxDurability(), clock.instant())
        );
        item.setType(Material.GRAY_DYE);
        writeIdentity(item, broken, "BROKEN_GROWTH_TOOL");
        checkpoints.checkpoint(broken.ownerUuid());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        String type = text(event.getItemDrop().getItemStack(), ITEM_TYPE);
        if ("GROWTH_TOOL".equals(type) || "BROKEN_GROWTH_TOOL".equals(type)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)
            || !wayfarerTool(event.getItem().getItemStack())) {
            return;
        }
        if (!validAuthority(player, event.getItem().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        List<ItemStack> retained = event.getDrops().stream()
            .filter(MainGameplayRuntime::wayfarerTool)
            .map(ItemStack::clone)
            .toList();
        if (retained.isEmpty()) {
            return;
        }
        event.getDrops().removeIf(MainGameplayRuntime::wayfarerTool);
        deathRetained.put(event.getEntity().getUniqueId(), retained);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        List<ItemStack> retained = deathRetained.remove(
            event.getPlayer().getUniqueId()
        );
        if (retained == null) {
            return;
        }
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = event.getPlayer();
            for (ItemStack item : retained) {
                if (validAuthority(player, item)
                    && player.getInventory().firstEmpty() >= 0) {
                    player.getInventory().addItem(item);
                }
            }
        });
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
        boolean carriesTool = wayfarerTool(current) || wayfarerTool(cursor);
        InventoryType type = event.getView().getTopInventory().getType();
        ItemStack hotbar = event.getHotbarButton() >= 0
            ? player.getInventory().getItem(event.getHotbarButton())
            : null;
        boolean externalContainer = type != InventoryType.CRAFTING
            && type != InventoryType.PLAYER;
        boolean targetsRestrictedInventory = externalContainer
            && (event.getClickedInventory() == event.getView().getTopInventory()
                || event.isShiftClick()
                || wayfarerTool(cursor)
                || wayfarerTool(hotbar));
        if (externalContainer
            && (wayfarerTool(current) || wayfarerTool(cursor)
                || wayfarerTool(hotbar))
            && targetsRestrictedInventory) {
            event.setCancelled(true);
            return;
        }
        if (carriesTool && targetsRestrictedInventory
            && (type == InventoryType.ANVIL
            || type == InventoryType.GRINDSTONE
            || type == InventoryType.SMITHING
            || type == InventoryType.CRAFTING
            || type == InventoryType.WORKBENCH)) {
            event.setCancelled(true);
            return;
        }
        if ((wayfarerTool(current) && !validAuthority(player, current))
            || (wayfarerTool(cursor) && !validAuthority(player, cursor))) {
            event.setCancelled(true);
            return;
        }
        plugin.getServer().getScheduler().runTask(
            plugin,
            () -> reconcileMainHand(player)
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
            instanceof GrowthToolGuiHolder) {
            event.setCancelled(true);
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)
            || !wayfarerTool(event.getOldCursor())) {
            return;
        }
        int topSize = event.getView().getTopInventory().getSize();
        boolean externalContainer =
            event.getView().getTopInventory().getType()
                != InventoryType.CRAFTING
                && event.getView().getTopInventory().getType()
                != InventoryType.PLAYER;
        if (externalContainer && event.getRawSlots().stream()
            .anyMatch(slot -> slot < topSize)) {
            event.setCancelled(true);
        }
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
        plugin.getServer().getScheduler().runTask(
            plugin,
            () -> reconcileMainHand(event.getPlayer())
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        plugin.getServer().getScheduler().runTask(
            plugin,
            () -> reconcileMainHand(event.getPlayer())
        );
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
        GrowthTool tool = sessions.current(event.getPlayer().getUniqueId()).orElse(null);
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        if (tool == null || !validAuthority(event.getPlayer(), held)) {
            return;
        }
        event.setCancelled(true);
        openMainGui(event.getPlayer(), tool, held);
    }

    public CompletionStage<Integer> stopAndFlush() {
        checkpointTask.cancel();
        deathRetained.clear();
        repairGuiSessions.clear();
        repairInFlight.clear();
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
        return replaceAuthority(playerUuid, false);
    }

    public CompletionStage<AdminMutation> reissue(UUID playerUuid) {
        return replaceAuthority(playerUuid, true).thenCompose(result -> {
            if (result != AdminMutation.APPLIED) {
                return CompletableFuture.completedFuture(result);
            }
            return delivery.onJoin(playerUuid)
                .thenCompose(ignored -> refreshSession(playerUuid))
                .thenApply(ignored -> result);
        });
    }

    public Optional<RepairSnapshot> repairSnapshot(Player player, GrowthTool tool) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (tool.status() == GrowthTool.Status.BROKEN) {
            for (ItemStack candidate : player.getInventory().getContents()) {
                if (candidate != null
                    && tool.toolId().toString().equals(text(candidate, TOOL_ID))
                    && tool.itemInstanceId().toString().equals(
                        text(candidate, ITEM_INSTANCE_ID)
                    )
                    && tool.ownerUuid().toString().equals(text(candidate, OWNER_ID))
                    && number(candidate, EPOCH) == tool.instanceEpoch()) {
                    item = candidate;
                    break;
                }
            }
        } else if (!canonical(item, player.getUniqueId(), tool)) {
            return Optional.empty();
        }
        if (!tool.toolId().toString().equals(text(item, TOOL_ID))) {
            return Optional.empty();
        }
        if (!tool.itemInstanceId().toString().equals(
            text(item, ITEM_INSTANCE_ID)
        )) {
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
        Player player = plugin.getServer().getPlayer(playerUuid);
        GrowthTool current = sessions.current(playerUuid).orElse(null);
        if (player == null || current == null
            || !current.toolId().equals(toolId)
            || current.instanceEpoch() != instanceEpoch
            || current.status() == GrowthTool.Status.REVOKED) {
            return false;
        }
        ItemStack target = null;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && toolId.toString().equals(text(item, TOOL_ID))
                && current.itemInstanceId().toString().equals(
                    text(item, ITEM_INSTANCE_ID)
                )
                && playerUuid.toString().equals(text(item, OWNER_ID))
                && number(item, EPOCH) == instanceEpoch) {
                target = item;
                break;
            }
        }
        if (target == null) {
            return false;
        }
        GrowthTool repaired = sessions.update(
            playerUuid,
            value -> value.repaired(clock.instant())
        );
        applyEvolution(target, repaired);
        if (target.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(0);
            target.setItemMeta(damageable);
        }
        checkpoints.checkpoint(playerUuid);
        return true;
    }

    public boolean switchBranch(Player player, GrowthTool.Branch branch) {
        GrowthTool current = sessions.current(player.getUniqueId()).orElse(null);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (current == null || !canonical(held, player.getUniqueId(), current)) {
            return false;
        }
        GrowthTool updated = sessions.update(
            player.getUniqueId(),
            tool -> tool.withBranch(branch, clock.instant())
        );
        applyEvolution(held, updated);
        checkpoints.checkpoint(player.getUniqueId());
        return true;
    }

    public boolean debug(Player player, String action) {
        GrowthTool current = sessions.current(player.getUniqueId()).orElse(null);
        ItemStack held = player.getInventory().getItemInMainHand();
        if (current == null || !canonical(held, player.getUniqueId(), current)) {
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
            case "repair-free" -> applyFullRepair(
                player.getUniqueId(),
                current.toolId(),
                current.instanceEpoch(),
                UUID.randomUUID()
            );
            default -> false;
        };
        if (applied) {
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
        delivery.onJoin(playerUuid)
            .thenCompose(outcome -> notifyDelivery(playerUuid, outcome))
            .thenCompose(ignored -> services.tasks().database(() ->
                repository.findOrCreate(playerUuid, clock.instant())
            ))
            .thenCompose(tool -> services.tasks().mainThread(() -> {
                Player online = plugin.getServer().getPlayer(playerUuid);
                if (online == null || !online.isOnline()) {
                    return;
                }
                sessions.open(tool);
                removeStaleTools(online, tool);
                reconcileInventory(online, tool);
            }))
            .exceptionally(ignored -> null);
    }

    private CompletionStage<AdminMutation> replaceAuthority(
        UUID playerUuid,
        boolean reissue
    ) {
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
        }).thenCompose(mutation -> {
            if (mutation.tool() == null) {
                return CompletableFuture.completedFuture(mutation.result());
            }
            return services.tasks().mainThread(() -> {
                Player online = plugin.getServer().getPlayer(playerUuid);
                if (online != null && online.isOnline()) {
                    sessions.open(mutation.tool());
                    removeStaleTools(online, mutation.tool());
                }
            }).thenCompose(ignored -> recordAdmin(
                mutation.tool(),
                reissue ? "GROWTH_TOOL_REISSUED" : "GROWTH_TOOL_REVOKED"
            ).handle((recorded, failure) -> mutation.result()));
        }).exceptionally(ignored -> AdminMutation.UNAVAILABLE);
    }

    private CompletionStage<Void> refreshSession(UUID playerUuid) {
        return services.tasks().database(() ->
            repository.findByOwner(playerUuid)
        ).thenCompose(found -> services.tasks().mainThread(() -> {
            Player online = plugin.getServer().getPlayer(playerUuid);
            if (online != null && online.isOnline()) {
                found.ifPresent(tool -> {
                    sessions.open(tool);
                    removeStaleTools(online, tool);
                    reconcileInventory(online, tool);
                });
            }
        }));
    }

    private CompletionStage<Void> notifyDelivery(
        UUID playerUuid,
        GrowthToolDeliveryCoordinator.Outcome outcome
    ) {
        if (outcome != GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL
            && outcome != GrowthToolDeliveryCoordinator.Outcome.PLAYER_OFFLINE
            && outcome != GrowthToolDeliveryCoordinator.Outcome.CONFLICT
            && outcome != GrowthToolDeliveryCoordinator.Outcome.UNAVAILABLE) {
            return CompletableFuture.completedFuture(null);
        }
        plugin.getLogger().warning(
            "Growth Tool delivery remains pending; retry on join or by admin."
        );
        return services.tasks().mainThread(() -> {
            Player online = plugin.getServer().getPlayer(playerUuid);
            if (online != null && online.isOnline()) {
                online.sendMessage(
                    "Growth Tool delivery is pending; free inventory space "
                        + "and rejoin or ask an administrator to retry."
                );
            }
        });
    }

    private void reconcileMainHand(Player player) {
        GrowthTool tool = sessions.current(player.getUniqueId()).orElse(null);
        if (tool == null || tool.status() != GrowthTool.Status.ACTIVE) {
            return;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (canonicalIdentity(item, tool)) {
            applyEvolution(item, tool, false);
        }
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
            } else if (tool.status() == GrowthTool.Status.BROKEN
                && item.getType() != Material.GRAY_DYE) {
                item.setType(Material.GRAY_DYE);
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
            new AtomicBoolean()
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
            if (rawSlot == 14) {
                openRepairPreview(player);
            } else if (rawSlot == 16) {
                player.sendMessage(
                    "Progress evolves the bound tool; repair requires "
                        + "preview and confirmation."
                );
            }
            return;
        }
        if (rawSlot == 15) {
            repairGuiSessions.remove(player.getUniqueId());
            player.closeInventory();
            return;
        }
        if (rawSlot != 11) {
            return;
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
            || !session.accepted().compareAndSet(false, true)) {
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
        Player player = plugin.getServer().getPlayer(tool.ownerUuid());
        if (player == null || !player.isOnline()) {
            return GrowthToolDeliveryCoordinator.Outcome.PLAYER_OFFLINE;
        }
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null
                && tool.itemInstanceId().toString().equals(
                    text(item, ITEM_INSTANCE_ID)
                )) {
                return GrowthToolDeliveryCoordinator.Outcome.ALREADY_PRESENT;
            }
        }
        if (player.getInventory().firstEmpty() < 0) {
            return GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL;
        }
        ItemStack item = new ItemStack(Material.WOODEN_PICKAXE);
        writeIdentity(item, tool, "GROWTH_TOOL");
        applyEvolution(item, tool, false);
        player.getInventory().addItem(item);
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
        if (!materialChanged && !enchantmentsChanged
            && !evolutionCountIncreased
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

    private static void add(ItemStack item, Enchantment enchantment, int level) {
        if (level > 0) {
            item.addUnsafeEnchantment(enchantment, level);
        }
    }

    private static boolean canonical(
        ItemStack item,
        UUID actor,
        GrowthTool authority
    ) {
        GrowthToolPhysicalClaim claim = claim(item).orElse(null);
        return claim != null
            && claim.validate(actor, authority)
                == GrowthToolPhysicalClaim.Validation.VALID
            && authority.status() == GrowthTool.Status.ACTIVE;
    }

    private boolean validAuthority(Player player, ItemStack item) {
        GrowthTool authority = sessions.current(player.getUniqueId()).orElse(null);
        GrowthToolPhysicalClaim claim = claim(item).orElse(null);
        return authority != null
            && claim != null
            && claim.validate(player.getUniqueId(), authority)
                == GrowthToolPhysicalClaim.Validation.VALID;
    }

    private static void removeStaleTools(
        Player player,
        GrowthTool authority
    ) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (!wayfarerTool(item)) {
                continue;
            }
            boolean current = authority.status() != GrowthTool.Status.REVOKED
                && authority.toolId().toString().equals(text(item, TOOL_ID))
                && authority.itemInstanceId().toString().equals(
                    text(item, ITEM_INSTANCE_ID)
                )
                && authority.ownerUuid().toString().equals(text(item, OWNER_ID))
                && authority.instanceEpoch() == number(item, EPOCH)
                && authority.schemaVersion() == number(item, SCHEMA);
            if (!current) {
                player.getInventory().setItem(slot, null);
            }
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
        String type = text(item, ITEM_TYPE);
        return "GROWTH_TOOL".equals(type)
            || "BROKEN_GROWTH_TOOL".equals(type);
    }

    private static void writeIdentity(
        ItemStack item,
        GrowthTool tool,
        String itemType
    ) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE, PersistentDataType.STRING, itemType);
        pdc.set(
            ITEM_INSTANCE_ID,
            PersistentDataType.STRING,
            tool.itemInstanceId().toString()
        );
        pdc.set(TOOL_ID, PersistentDataType.STRING, tool.toolId().toString());
        pdc.set(OWNER_ID, PersistentDataType.STRING, tool.ownerUuid().toString());
        pdc.set(TOOL_TYPE, PersistentDataType.STRING, GrowthTool.TOOL_TYPE);
        pdc.set(EPOCH, PersistentDataType.LONG, tool.instanceEpoch());
        pdc.set(SCHEMA, PersistentDataType.INTEGER, tool.schemaVersion());
        pdc.set(REVISION, PersistentDataType.LONG, tool.displayRevision());
        item.setItemMeta(meta);
    }

    private static Optional<GrowthToolPhysicalClaim> claim(ItemStack item) {
        java.util.Map<String, String> raw = new java.util.HashMap<>();
        put(raw, "item_type", text(item, ITEM_TYPE));
        put(raw, "item_instance_id", text(item, ITEM_INSTANCE_ID));
        put(raw, "tool_id", text(item, TOOL_ID));
        put(raw, "owner_uuid", text(item, OWNER_ID));
        put(raw, "tool_type", text(item, TOOL_TYPE));
        put(raw, "instance_epoch", numericText(item, EPOCH));
        put(raw, "schema_version", numericText(item, SCHEMA));
        put(raw, "display_revision", numericText(item, REVISION));
        return GrowthToolPhysicalClaim.parse(raw).claim();
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
        Long longValue = pdc.get(key, PersistentDataType.LONG);
        if (longValue != null) {
            return longValue;
        }
        Integer integer = pdc.get(key, PersistentDataType.INTEGER);
        return integer == null ? Long.MIN_VALUE : integer.longValue();
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
        UNAVAILABLE
    }

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
        AtomicBoolean accepted
    ) {}
}
