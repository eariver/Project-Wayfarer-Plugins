package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.frontier.application.TraversalDeliveryCoordinator;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseCoordinator;
import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadUseCoordinator;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.DeliveryCompletion;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import io.github.eariver.wayfarer.frontier.domain.LaunchpadPlacementPolicy;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import io.github.eariver.wayfarer.frontier.identity.LaunchpadItemClaim;
import io.github.eariver.wayfarer.frontier.integration.WorldGuardPlacementBridge;
import io.github.eariver.wayfarer.frontier.integration.WorldEditLaunchpadProtection;
import io.github.eariver.wayfarer.common.SingleUseGate;
import io.github.eariver.wayfarer.common.BoundItemTransferPolicy;
import io.github.eariver.wayfarer.integration.leafgrapple.LeafGrappleBridge;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Clock;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Instant;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

public final class FrontierGameplayRuntime implements Listener {
    private static final NamespacedKey ITEM_TYPE =
        new NamespacedKey("wayfarer", "item_type");
    private static final NamespacedKey OWNER_ID =
        new NamespacedKey("wayfarer", "owner_uuid");
    private static final NamespacedKey DELIVERY_ID =
        new NamespacedKey("wayfarer", "delivery_id");
    private static final NamespacedKey THEME_ID =
        new NamespacedKey("wayfarer", "theme_id");
    private static final NamespacedKey ITEM_INSTANCE_ID =
        new NamespacedKey("wayfarer", "item_instance_id");
    private static final NamespacedKey INSTANCE_EPOCH =
        new NamespacedKey("wayfarer", "instance_epoch");
    private static final NamespacedKey SCHEMA_VERSION =
        new NamespacedKey("wayfarer", "schema_version");
    private static final NamespacedKey DEFINITION_ID =
        new NamespacedKey("wayfarer", "definition_id");
    private static final String LAUNCHPAD_DEFINITION = "frontier-v1";
    private final JavaPlugin plugin;
    private final FrontierModuleConfig config;
    private final LeafGrappleBridge leafGrapple;
    private final TraversalDeliveryCoordinator delivery;
    private final WayfarerServices services;
    private final TraversalLoadoutRepository loadouts;
    private final LaunchpadRepository launchpads;
    private final LaunchpadUseCoordinator launchpadUse;
    private final LaunchpadPlacementPolicy placementPolicy;
    private final ConcurrentHashMap<UUID, Instant> cooldowns =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, TraversalLoadout> authorities =
        new ConcurrentHashMap<>();
    private final LaunchpadRuntimeIndex activeLaunchpads =
        new LaunchpadRuntimeIndex();
    private final ConcurrentHashMap<UUID, NavigationSession> navigationSessions =
        new ConcurrentHashMap<>();
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicBoolean launchpadIndexReady = new AtomicBoolean();
    private final Clock clock;
    private final BukkitTask reconcileTask;
    private final WorldGuardPlacementBridge worldGuard;
    private final WorldEditLaunchpadProtection worldEditProtection;
    private volatile FrontierPurchaseCoordinator purchaseCoordinator;

    public FrontierGameplayRuntime(
        JavaPlugin plugin,
        FrontierModuleConfig config,
        WayfarerServices services,
        TraversalLoadoutRepository repository,
        LaunchpadRepository launchpads,
        LeafGrappleBridge leafGrapple,
        Clock clock
    ) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.config = java.util.Objects.requireNonNull(config, "config");
        this.leafGrapple = java.util.Objects.requireNonNull(
            leafGrapple,
            "leafGrapple"
        );
        this.services = java.util.Objects.requireNonNull(services, "services");
        this.loadouts = java.util.Objects.requireNonNull(repository, "repository");
        this.launchpads = java.util.Objects.requireNonNull(launchpads, "launchpads");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
        delivery = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(java.util.Set.of(config.exactWorldName())),
            repository,
            services.tasks(),
            new DeliveryGatewayAdapter(),
            new DeliveryAuditAdapter(),
            clock
        );
        launchpadUse = new LaunchpadUseCoordinator(
            launchpads,
            services.tasks(),
            new LaunchpadGateway(),
            clock,
            Duration.ofSeconds(5),
            config.launchpad().expiration(),
            config.launchpad().extendExpirationOnUse()
        );
        placementPolicy = new LaunchpadPlacementPolicy(
            new FrontierWorldGate(java.util.Set.of(config.exactWorldName()))
        );
        worldGuard = plugin.getServer().getPluginManager()
            .isPluginEnabled("WorldGuard")
            ? new WorldGuardPlacementBridge()
            : null;
        worldEditProtection = plugin.getServer().getPluginManager()
            .isPluginEnabled("WorldEdit")
            ? new WorldEditLaunchpadProtection(coordinate ->
                (config.exactWorldName().equals(coordinate.worldName())
                    && !launchpadIndexReady.get())
                    || activeLaunchpads.contains(new Launchpad.Location(
                        coordinate.worldName(),
                        coordinate.x(),
                        coordinate.y(),
                        coordinate.z()
                    ))
            )
            : null;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        long period = Math.max(
            20L,
            config.checkpointInterval().toSeconds() * 20L
        );
        reconcileTask = plugin.getServer().getScheduler().runTaskTimer(
            plugin,
            this::reconcileExpirations,
            1L,
            period
        );
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            enter(player);
        }
        services.tasks().database(() -> launchpads.findActive(100_000))
            .thenAccept(active -> {
                active.forEach(activeLaunchpads::activate);
                launchpadIndexReady.set(true);
            }).exceptionally(ignored -> {
                services.tasks().mainThread(() -> {
                    plugin.getLogger().severe(
                        "Launchpad authority index failed closed."
                    );
                    plugin.getServer().getPluginManager().disablePlugin(plugin);
                });
                return null;
            });
    }

    public void stop() {
        accepting.set(false);
        delivery.shutdown();
        reconcileTask.cancel();
        cooldowns.clear();
        authorities.clear();
        activeLaunchpads.clear();
        navigationSessions.clear();
        if (worldEditProtection != null) {
            worldEditProtection.close();
        }
    }

    public void bindPurchaseCoordinator(
        FrontierPurchaseCoordinator coordinator
    ) {
        if (purchaseCoordinator != null) {
            throw new IllegalStateException(
                "Purchase coordinator is already bound"
            );
        }
        purchaseCoordinator = java.util.Objects.requireNonNull(
            coordinator,
            "coordinator"
        );
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        enter(event.getPlayer());
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        enter(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent event) {
        if (config.exactWorldName().equals(event.getPlayer().getWorld().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        if (permanent(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Item dropped = event.getItem();
        if (permanent(dropped.getItemStack())
            && !validPermanent(player, dropped.getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        if (!accepting.get()) {
            return;
        }
        Player player = event.getEntity();
        UUID playerUuid = player.getUniqueId();
        List<DeathIdentitySnapshot> snapshots = new ArrayList<>();
        event.getDrops().removeIf(item -> {
            CompleteManagedPermanent managed = parseCompleteManagedPermanent(item);
            if (managed == null) {
                return false;
            }
            if (managed.ownerUuid.equals(playerUuid)) {
                snapshots.add(new DeathIdentitySnapshot(
                    playerUuid,
                    managed.itemType,
                    managed.itemInstanceId,
                    managed.instanceEpoch,
                    managed.schemaVersion,
                    managed.themeId
                ));
            }
            return true;
        });
        if (!snapshots.isEmpty()) {
            delivery.persistDeathSnapshots(playerUuid, List.copyOf(snapshots));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        enter(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPermanentDamage(PlayerItemDamageEvent event) {
        if (permanent(event.getItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack used = event.getItemInHand();
        if (!"LAUNCHPAD".equals(text(used, ITEM_TYPE))) {
            return;
        }
        event.setCancelled(true);
        if (!launchpadIndexReady.get()) {
            player.sendMessage("Launchpad authority is still unavailable.");
            return;
        }
        LaunchpadItemClaim claim = launchpadClaim(used).orElse(null);
        UUID deliveryId = parseUuid(text(used, DELIVERY_ID));
        org.bukkit.block.Block block = event.getBlockPlaced();
        LaunchpadPlacementPolicy.Snapshot snapshot = placementSnapshot(
            player,
            block,
            event.getBlockReplacedState().getType().isAir()
        );
        if (claim == null || deliveryId == null
            || !LAUNCHPAD_DEFINITION.equals(claim.definitionId())
            || !player.getUniqueId().toString().equals(text(used, OWNER_ID))
            || placementPolicy.validate(snapshot)
                != LaunchpadPlacementPolicy.Result.ALLOWED) {
            player.sendMessage(
                "Launchpad placement is unavailable at this location."
            );
            return;
        }
        Launchpad.Location location = location(block);
        if (activeLaunchpads.contains(location)) {
            player.sendMessage("A launchpad already occupies this location.");
            return;
        }
        Instant now = clock.instant();
        Launchpad launchpad = new Launchpad(
            UUID.randomUUID(),
            location,
            player.getLocation().getYaw(),
            player.getUniqueId(),
            0,
            config.launchpad().maximumSuccessfulUses(),
            now,
            null,
            now.plus(config.launchpad().expiration()),
            claim.definitionId(),
            Launchpad.State.ACTIVE,
            claim.schemaVersion(),
            0
        );
        PlacementRequest request = new PlacementRequest(
            player.getUniqueId(),
            location,
            event.getHand(),
            claim,
            deliveryId,
            launchpad
        );
        services.tasks().database(() ->
            launchpads.createFromItem(
                launchpad,
                claim.itemInstanceId(),
                config.launchpad().maximumActivePerPlayer(),
                now
            )
        ).thenAccept(created -> services.tasks().mainThread(() -> {
            if (!accepting.get()) {
                return;
            }
            Player online = plugin.getServer().getPlayer(request.playerUuid());
            org.bukkit.World world = plugin.getServer().getWorld(
                request.location().worldId()
            );
            org.bukkit.block.Block currentBlock = world == null
                ? null
                : world.getBlockAt(
                    request.location().x(),
                    request.location().y(),
                    request.location().z()
                );
            ItemStack current = online == null
                ? null
                : request.hand() == EquipmentSlot.HAND
                    ? online.getInventory().getItemInMainHand()
                    : online.getInventory().getItemInOffHand();
            LaunchpadItemClaim currentClaim = launchpadClaim(current)
                .orElse(null);
            boolean stillAllowed = online != null
                && online.isOnline()
                && world != null
                && currentBlock != null
                && request.claim().equals(currentClaim)
                && request.deliveryId().equals(
                    parseUuid(text(current, DELIVERY_ID))
                )
                && request.playerUuid().toString().equals(
                    text(current, OWNER_ID)
                )
                && placementPolicy.validate(
                    placementSnapshot(online, currentBlock, currentBlock.isEmpty())
                ) == LaunchpadPlacementPolicy.Result.ALLOWED
                && !activeLaunchpads.contains(request.location());
            if (!created || !stillAllowed) {
                if (created) {
                    services.tasks().database(() ->
                        launchpads.rollbackCreatedPlacement(
                        launchpad,
                        request.claim().itemInstanceId(),
                        clock.instant()
                    ));
                }
                if (online != null) {
                    online.sendMessage(
                        "Launchpad placement could not be committed."
                    );
                }
                return;
            }
            currentBlock.setType(
                Material.valueOf(config.launchpad().material()),
                false
            );
            current.setAmount(current.getAmount() - 1);
            activeLaunchpads.activate(request.launchpad());
            recordLaunchpad(
                request.launchpad(),
                request.playerUuid(),
                "LAUNCHPAD_PLACED",
                "APPLIED"
            ).exceptionally(ignored -> null);
        }));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onUse(PlayerInteractEvent event) {
        ItemStack used = event.getItem();
        if (used != null && permanent(used)) {
            if (!validPermanent(event.getPlayer(), used)) {
                event.setCancelled(true);
                return;
            }
            if ("NAVIGATION".equals(text(used, ITEM_TYPE))
                && event.getAction().isRightClick()) {
                event.setCancelled(true);
                openNavigation(event.getPlayer());
                return;
            }
        }
        if (event.getAction() != Action.PHYSICAL
            || event.getClickedBlock() == null
            || !config.exactWorldName().equals(event.getPlayer().getWorld().getName())
            || event.getClickedBlock().getType()
            != Material.valueOf(config.launchpad().material())) {
            return;
        }
        Launchpad.Location location = location(event.getClickedBlock());
        if (!launchpadIndexReady.get()) {
            event.setCancelled(true);
            return;
        }
        if (!activeLaunchpads.contains(location)) {
            return;
        }
        event.setCancelled(true);
        UUID playerUuid = event.getPlayer().getUniqueId();
        String worldName = event.getPlayer().getWorld().getName();
        boolean sneaking = config.launchpad().disableWhileSneaking()
            && event.getPlayer().isSneaking();
        Instant cooldownUntil = cooldowns.get(playerUuid);
        services.tasks().database(() -> launchpads.findAt(location))
            .thenAccept(found -> {
                if (!accepting.get()) {
                    return;
                }
                found.ifPresent(launchpad ->
                launchpadUse.use(new LaunchpadUseCoordinator.Request(
                    launchpad.launchpadId(),
                    playerUuid,
                    worldName,
                    sneaking,
                    cooldownUntil
                )).thenAccept(result -> {
                    if (result.outcome() == Launchpad.Outcome.LAUNCHED) {
                        cooldowns.put(
                            playerUuid,
                            clock.instant().plus(config.launchpad().cooldown())
                        );
                    }
                })
            );
            });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getView().getTopInventory().getHolder()
            instanceof NavigationHolder holder) {
            event.setCancelled(true);
            handleNavigationClick(player, event.getRawSlot(), holder);
            return;
        }
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        ItemStack hotbar = event.getHotbarButton() >= 0
            ? player.getInventory().getItem(event.getHotbarButton())
            : null;
        InventoryType topType = event.getView().getTopInventory().getType();
        boolean externalContainer = topType != InventoryType.CRAFTING
            && topType != InventoryType.PLAYER;
        if (BoundItemTransferPolicy.denyContainerClick(
            externalContainer,
            permanent(current),
            permanent(cursor),
            permanent(hotbar),
            event.isShiftClick(),
            event.getClickedInventory() == event.getView().getTopInventory()
        )) {
            event.setCancelled(true);
            return;
        }
        if ((current != null && permanent(current)
            && !validPermanent(player, current))
            || (cursor != null && permanent(cursor)
                && !validPermanent(player, cursor))
            || (hotbar != null && permanent(hotbar)
                && !validPermanent(player, hotbar))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder()
            instanceof NavigationHolder) {
            event.setCancelled(true);
            return;
        }
        if (!permanent(event.getOldCursor())) {
            return;
        }
        InventoryType topType = event.getView().getTopInventory().getType();
        if (BoundItemTransferPolicy.denyContainerDrag(
            topType != InventoryType.CRAFTING
                && topType != InventoryType.PLAYER,
            true,
            event.getRawSlots().stream().anyMatch(
                slot -> slot < event.getView().getTopInventory().getSize()
            )
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmorStand(PlayerArmorStandManipulateEvent event) {
        if (permanent(event.getPlayerItem())
            && !validPermanent(event.getPlayer(), event.getPlayerItem())) {
            event.setCancelled(true);
            return;
        }
        if (permanent(event.getPlayerItem())) {
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
        if (permanent(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onNavigationClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)
            || !(event.getInventory().getHolder()
                instanceof NavigationHolder holder)) {
            return;
        }
        navigationSessions.computeIfPresent(
            player.getUniqueId(),
            (ignored, session) ->
                session.token().equals(holder.token()) ? null : session
        );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!config.exactWorldName().equals(event.getBlock().getWorld().getName())
            || event.getBlock().getType()
            != Material.valueOf(config.launchpad().material())) {
            return;
        }
        Launchpad.Location location = location(event.getBlock());
        if (!launchpadIndexReady.get()) {
            event.setCancelled(true);
            return;
        }
        UUID launchpadId = activeLaunchpads.idAt(location);
        if (launchpadId == null) {
            return;
        }
        event.setCancelled(true);
        UUID actorUuid = event.getPlayer().getUniqueId();
        services.tasks().database(() -> launchpads.find(launchpadId))
            .thenAccept(found -> services.tasks().mainThread(() -> {
                if (!accepting.get()) {
                    return;
                }
                if (found.isEmpty()) {
                    plugin.getLogger().warning(
                        "Launchpad BLOCK_ONLY inconsistency requires "
                            + "confirmed administrator reconciliation."
                    );
                    recordBlockOnly(
                        launchpadId,
                        actorUuid,
                        location,
                        "BLOCK_ONLY"
                    ).exceptionally(ignored -> null);
                    return;
                }
                Launchpad launchpad = found.orElseThrow();
                if (!config.launchpad().allowPlayerBreak()) {
                    return;
                }
                services.tasks().database(() -> launchpads.remove(
                    launchpad.launchpadId(),
                    launchpad.lockVersion(),
                    Launchpad.State.PLAYER_BROKEN,
                    Instant.now()
                )).thenAccept(removed -> services.tasks().mainThread(() -> {
                    if (removed) {
                        org.bukkit.World world = plugin.getServer().getWorld(
                            location.worldId()
                        );
                        if (world != null) {
                            world.getBlockAt(
                                location.x(),
                                location.y(),
                                location.z()
                            ).setType(Material.AIR, false);
                        }
                        activeLaunchpads.deactivate(
                            location,
                            launchpad.launchpadId()
                        );
                        recordLaunchpad(
                            launchpad,
                            actorUuid,
                            "LAUNCHPAD_PLAYER_BROKEN",
                            "APPLIED"
                        ).exceptionally(ignored -> null);
                    }
                }));
            })).exceptionally(ignored -> {
                plugin.getLogger().warning(
                    "Launchpad break persistence is unavailable; block retained."
                );
                return null;
            });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().removeIf(this::isProtectedLaunchpadMaterial);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().removeIf(this::isProtectedLaunchpadMaterial);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isProtectedLaunchpadMaterial)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (event.getBlocks().stream().anyMatch(this::isProtectedLaunchpadMaterial)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFluid(BlockFromToEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())
            || isProtectedLaunchpadMaterial(event.getToBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFade(BlockFadeEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPhysics(BlockPhysicsEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpread(BlockSpreadEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())
            || isProtectedLaunchpadMaterial(event.getSource())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGrow(BlockGrowEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (isProtectedLaunchpadMaterial(event.getBlock())
            || (event.getEntity() instanceof org.bukkit.entity.FallingBlock
                && isProtectedLaunchpadMaterial(
                    event.getBlock().getRelative(
                        org.bukkit.block.BlockFace.DOWN
                    )
                ))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        if (event.getBlocks().stream().anyMatch(state ->
            isProtectedLaunchpadMaterial(state.getBlock())
        )) {
            event.setCancelled(true);
        }
    }

    private void enter(Player player) {
        if (!accepting.get()) {
            return;
        }
        UUID playerUuid = player.getUniqueId();
        if (!config.exactWorldName().equals(player.getWorld().getName())) {
            authorities.remove(playerUuid);
            return;
        }
        delivery.onSafeEntry(playerUuid, player.getWorld().getName())
            .exceptionally(ignored -> null);
    }

    private TraversalDeliveryCoordinator.DeliveryOutcome deliver(
        UUID playerUuid,
        PendingDelivery pending
    ) {
        if (!accepting.get()) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.PLAYER_OFFLINE;
        }
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.PLAYER_OFFLINE;
        }
        if (!config.exactWorldName().equals(player.getWorld().getName())) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.LEFT_THEME;
        }
        TraversalLoadout authority = authorities.get(playerUuid);
        if (isPermanent(pending.itemType())) {
            if (pending.identity() == null) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.UNKNOWN;
            }
            if (authority != null) {
                cleanupNonCurrentManaged(player, authority);
            }
            if (isExactCurrentPhysical(
                player,
                pending.identity(),
                authority,
                true
            )) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
            }
        }
        if (pending.itemType() == PendingDelivery.ItemType.LAUNCHPAD) {
            return deliverLaunchpads(player, pending);
        }
        if (player.getInventory().firstEmpty() < 0) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.INVENTORY_FULL;
        }
        ItemStack item = create(playerUuid, pending);
        if (item == null) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.CAPABILITY_UNAVAILABLE;
        }
        annotate(item, playerUuid, pending);
        if (!player.getInventory().addItem(item).isEmpty()) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.INVENTORY_FULL;
        }
        return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
    }

    private TraversalDeliveryCoordinator.DeliveryOutcome deliverLaunchpads(
        Player player,
        PendingDelivery pending
    ) {
        int present = 0;
        for (int index = 0; index < pending.quantity(); index++) {
            if (containsItemInstance(
                player,
                launchpadItemInstance(pending.deliveryId(), index)
            )) {
                present++;
            }
        }
        if (present == pending.quantity()) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
        }
        if (emptySlots(player) < pending.quantity() - present) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.INVENTORY_FULL;
        }
        for (int index = 0; index < pending.quantity(); index++) {
            UUID itemInstanceId = launchpadItemInstance(
                pending.deliveryId(),
                index
            );
            if (containsItemInstance(player, itemInstanceId)) {
                continue;
            }
            ItemStack item = new ItemStack(
                Material.valueOf(config.launchpad().material())
            );
            annotateLaunchpad(item, player.getUniqueId(), pending, itemInstanceId);
            if (!player.getInventory().addItem(item).isEmpty()) {
                return TraversalDeliveryCoordinator.DeliveryOutcome.INVENTORY_FULL;
            }
        }
        return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
    }

    private static int emptySlots(Player player) {
        int empty = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.isEmpty()) {
                empty++;
            }
        }
        return empty;
    }

    static UUID launchpadItemInstance(UUID deliveryId, int index) {
        if (index < 0) {
            throw new IllegalArgumentException(
                "Launchpad delivery index cannot be negative"
            );
        }
        return UUID.nameUUIDFromBytes(
            ("launchpad-delivery:" + deliveryId + ":" + index)
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private static boolean containsItemInstance(
        Player player,
        UUID itemInstanceId
    ) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (itemInstanceId.toString().equals(
                text(item, ITEM_INSTANCE_ID)
            )) {
                return true;
            }
        }
        return false;
    }

    public boolean deliverPurchase(
        FrontierPurchaseRepository.Purchase purchase,
        UUID deliveryId
    ) {
        PendingDelivery pending = new PendingDelivery(
            deliveryId,
            purchase.playerUuid(),
            TraversalIdentity.WORLDS_BEYOND,
            purchase.offer().itemType(),
            purchase.offer().quantity(),
            "frontier-shop-delivery:" + purchase.purchaseId(),
            PendingDelivery.State.PENDING,
            0,
            java.time.Instant.now()
        );
        return deliver(purchase.playerUuid(), pending)
            == TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
    }

    public CompletionStage<Optional<io.github.eariver.wayfarer.frontier.domain.TraversalLoadout>>
        inspectLoadout(UUID playerUuid) {
        return services.tasks().database(() -> loadouts.find(playerUuid));
    }

    public CompletionStage<AdminLoadoutResult> reissueLoadout(
        UUID playerUuid,
        TraversalIdentity.ItemType itemType,
        UUID actorUuid
    ) {
        return delivery.adminReissueCritical(playerUuid, itemType)
            .thenCompose(reissued -> {
                if (!reissued) {
                    return CompletableFuture.completedFuture(
                        AdminLoadoutResult.NOT_FOUND_OR_CONFLICT
                    );
                }
                return services.audit().record(new WayfarerAudit.AuditEvent(
                    UUID.randomUUID(),
                    "FRONTIER_LOADOUT_REISSUED",
                    actorUuid,
                    "FRONTIER_LOADOUT",
                    playerUuid.toString(),
                    services.serverId(),
                    "{\"result\":\"APPLIED\",\"item_type\":\""
                        + itemType.name() + "\"}",
                    clock.instant()
                )).handle((ignored, failure) -> true)
                    .thenCompose(ignored -> retryDelivery(playerUuid))
                    .thenApply(ignored -> AdminLoadoutResult.APPLIED);
            }).exceptionally(ignored -> AdminLoadoutResult.UNAVAILABLE);
    }

    public CompletionStage<List<PendingDelivery>> inspectDeliveries(
        UUID playerUuid
    ) {
        return services.tasks().database(() -> loadouts.pending(playerUuid));
    }

    public CompletionStage<TraversalDeliveryCoordinator.Result>
        retryDelivery(UUID playerUuid) {
        RetryCapture capture = new RetryCapture();
        return services.tasks().mainThread(() -> {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (accepting.get() && player != null && player.isOnline()) {
                capture.worldName = player.getWorld().getName();
            }
        }).thenCompose(ignored -> capture.worldName == null
            ? CompletableFuture.completedFuture(
                TraversalDeliveryCoordinator.Result.unavailable()
            )
            : delivery.onSafeEntry(playerUuid, capture.worldName)
        );
    }

    public CompletionStage<LaunchpadInspection> inspectLaunchpad(
        UUID launchpadId
    ) {
        return services.tasks().database(() -> launchpads.find(launchpadId))
            .thenCompose(found -> {
                if (found.isEmpty()) {
                    Launchpad.Location cached = cachedLocation(launchpadId);
                    if (cached == null) {
                        return CompletableFuture.completedFuture(
                            new LaunchpadInspection(
                                null,
                                ReconcileClassification.NOT_FOUND
                            )
                        );
                    }
                    InspectionCapture capture = new InspectionCapture();
                    return services.tasks().mainThread(() -> {
                        org.bukkit.World world = plugin.getServer().getWorld(
                            cached.worldId()
                        );
                        boolean blockExists = world != null
                            && world.getBlockAt(
                                cached.x(),
                                cached.y(),
                                cached.z()
                            ).getType() == Material.valueOf(
                                config.launchpad().material()
                            );
                        capture.classification = blockExists
                            ? ReconcileClassification.BLOCK_ONLY
                            : ReconcileClassification.NOT_FOUND;
                        if (!blockExists) {
                            activeLaunchpads.deactivate(cached, launchpadId);
                        }
                    }).thenApply(ignored -> new LaunchpadInspection(
                        null,
                        capture.classification
                    ));
                }
                Launchpad launchpad = found.orElseThrow();
                InspectionCapture capture = new InspectionCapture();
                return services.tasks().mainThread(() ->
                    capture.classification = classify(launchpad)
                ).thenApply(ignored -> new LaunchpadInspection(
                    launchpad.state(),
                    capture.classification
                ));
            });
    }

    public CompletionStage<AdminLaunchpadResult> removeLaunchpad(
        UUID launchpadId,
        UUID actorUuid
    ) {
        return removeLaunchpad(
            launchpadId,
            actorUuid,
            Launchpad.State.ADMIN_REMOVED
        );
    }

    public CompletionStage<AdminLaunchpadResult> reconcileLaunchpad(
        UUID launchpadId,
        UUID actorUuid,
        boolean confirmed
    ) {
        return inspectLaunchpad(launchpadId).thenCompose(inspection -> {
            if (inspection.classification() == ReconcileClassification.NOT_FOUND) {
                return CompletableFuture.completedFuture(
                    AdminLaunchpadResult.NOT_FOUND
                );
            }
            if (inspection.classification()
                == ReconcileClassification.DB_AND_BLOCK_MATCH) {
                return CompletableFuture.completedFuture(
                    AdminLaunchpadResult.NO_CHANGE
                );
            }
            if (inspection.classification()
                == ReconcileClassification.BLOCK_ONLY) {
                return confirmed
                    ? removeBlockOnly(launchpadId, actorUuid)
                    : CompletableFuture.completedFuture(
                        AdminLaunchpadResult.MANUAL_REVIEW_REQUIRED
                    );
            }
            if (!confirmed || inspection.classification()
                == ReconcileClassification.CONFLICT) {
                return CompletableFuture.completedFuture(
                    AdminLaunchpadResult.MANUAL_REVIEW_REQUIRED
                );
            }
            return removeLaunchpad(
                launchpadId,
                actorUuid,
                Launchpad.State.RECONCILED_REMOVED
            );
        });
    }

    private CompletionStage<AdminLaunchpadResult> removeBlockOnly(
        UUID launchpadId,
        UUID actorUuid
    ) {
        Launchpad.Location location = cachedLocation(launchpadId);
        if (location == null) {
            return CompletableFuture.completedFuture(
                AdminLaunchpadResult.NOT_FOUND
            );
        }
        return services.tasks().mainThread(() -> {
            org.bukkit.World world = plugin.getServer().getWorld(
                location.worldId()
            );
            if (world != null) {
                org.bukkit.block.Block block = world.getBlockAt(
                    location.x(),
                    location.y(),
                    location.z()
                );
                if (block.getType()
                    == Material.valueOf(config.launchpad().material())) {
                    block.setType(Material.AIR, false);
                }
            }
            activeLaunchpads.deactivate(location, launchpadId);
        }).thenCompose(ignored -> recordBlockOnly(
            launchpadId,
            actorUuid,
            location,
            "RECONCILED_REMOVED"
        )).handle((ignored, failure) ->
            failure == null
                ? AdminLaunchpadResult.APPLIED
                : AdminLaunchpadResult.UNAVAILABLE
        );
    }

    private Launchpad.Location cachedLocation(UUID launchpadId) {
        return activeLaunchpads.locationOf(launchpadId);
    }

    private CompletionStage<AdminLaunchpadResult> removeLaunchpad(
        UUID launchpadId,
        UUID actorUuid,
        Launchpad.State removalState
    ) {
        return services.tasks().database(() -> {
            Launchpad launchpad = launchpads.find(launchpadId).orElse(null);
            if (launchpad == null) {
                return new RemovalCapture(null, false);
            }
            boolean removed = launchpads.remove(
                launchpad.launchpadId(),
                launchpad.lockVersion(),
                removalState,
                clock.instant()
            );
            return new RemovalCapture(launchpad, removed);
        }).thenCompose(capture -> {
            if (capture.launchpad() == null) {
                return CompletableFuture.completedFuture(
                    AdminLaunchpadResult.NOT_FOUND
                );
            }
            if (!capture.removed()) {
                return CompletableFuture.completedFuture(
                    AdminLaunchpadResult.CONFLICT
                );
            }
            activeLaunchpads.deactivate(
                capture.launchpad().location(),
                capture.launchpad().launchpadId()
            );
            return services.tasks().mainThread(() -> {
                if (!accepting.get()) {
                    return;
                }
                org.bukkit.World world = plugin.getServer().getWorld(
                    capture.launchpad().location().worldId()
                );
                if (world == null) {
                    return;
                }
                org.bukkit.block.Block block = world.getBlockAt(
                    capture.launchpad().location().x(),
                    capture.launchpad().location().y(),
                    capture.launchpad().location().z()
                );
                if (block.getType()
                    == Material.valueOf(config.launchpad().material())) {
                    block.setType(Material.AIR, false);
                }
            }).thenCompose(ignored -> recordLaunchpad(
                capture.launchpad(),
                actorUuid,
                "LAUNCHPAD_" + removalState.name(),
                "APPLIED"
            ).handle((recorded, failure) -> AdminLaunchpadResult.APPLIED));
        }).exceptionally(ignored -> AdminLaunchpadResult.UNAVAILABLE);
    }

    private void reconcileExpirations() {
        if (!accepting.get()) {
            return;
        }
        Instant now = clock.instant();
        services.tasks().database(() ->
            launchpads.findExpirationCandidates(now, 100)
        ).thenAccept(candidates ->
            candidates.forEach(candidate ->
                expireCandidate(candidate, now)
            )
        ).exceptionally(ignored -> null);
    }

    private void expireCandidate(Launchpad launchpad, Instant now) {
        InspectionCapture capture = new InspectionCapture();
        services.tasks().mainThread(() -> {
            if (accepting.get()) {
                capture.classification = classify(launchpad);
            }
        }).thenCompose(ignored -> services.tasks().database(() ->
            launchpads.remove(
                launchpad.launchpadId(),
                launchpad.lockVersion(),
                Launchpad.State.EXPIRED,
                now
            )
        )).thenCompose(removed -> {
            if (!removed) {
                return CompletableFuture.completedFuture(null);
            }
            activeLaunchpads.deactivate(
                launchpad.location(),
                launchpad.launchpadId()
            );
            return services.tasks().mainThread(() -> {
                if (!accepting.get()
                    || capture.classification
                    != ReconcileClassification.DB_AND_BLOCK_MATCH) {
                    return;
                }
                org.bukkit.World world = plugin.getServer().getWorld(
                    launchpad.location().worldId()
                );
                if (world == null) {
                    return;
                }
                org.bukkit.block.Block block = world.getBlockAt(
                    launchpad.location().x(),
                    launchpad.location().y(),
                    launchpad.location().z()
                );
                if (block.getType()
                    == Material.valueOf(config.launchpad().material())) {
                    block.setType(Material.AIR, false);
                }
            }).thenCompose(nothing -> recordLaunchpad(
                launchpad,
                null,
                "LAUNCHPAD_EXPIRED",
                capture.classification.name()
            ));
        }).exceptionally(ignored -> null);
    }

    private ReconcileClassification classify(Launchpad launchpad) {
        org.bukkit.World world = plugin.getServer().getWorld(
            launchpad.location().worldId()
        );
        if (world == null) {
            return ReconcileClassification.UNKNOWN;
        }
        org.bukkit.block.Block block = world.getBlockAt(
            launchpad.location().x(),
            launchpad.location().y(),
            launchpad.location().z()
        );
        boolean blockMatches = block.getType()
            == Material.valueOf(config.launchpad().material());
        if (launchpad.state() == Launchpad.State.ACTIVE) {
            return blockMatches
                ? ReconcileClassification.DB_AND_BLOCK_MATCH
                : ReconcileClassification.DB_ONLY;
        }
        return blockMatches
            ? ReconcileClassification.CONFLICT
            : ReconcileClassification.DB_ONLY;
    }

    private CompletionStage<Void> recordLaunchpad(
        Launchpad launchpad,
        UUID actorUuid,
        String eventType,
        String result
    ) {
        return services.audit().record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            eventType,
            actorUuid,
            "LAUNCHPAD",
            launchpad.launchpadId().toString(),
            services.serverId(),
            "{\"result\":\"" + result + "\"}",
            clock.instant()
        ));
    }

    private CompletionStage<Void> recordBlockOnly(
        UUID launchpadId,
        UUID actorUuid,
        Launchpad.Location location,
        String result
    ) {
        return services.audit().record(new WayfarerAudit.AuditEvent(
            UUID.randomUUID(),
            "LAUNCHPAD_BLOCK_ONLY",
            actorUuid,
            "LAUNCHPAD",
            launchpadId.toString(),
            services.serverId(),
            "{\"result\":\"" + result + "\",\"world\":\""
                + location.worldId() + "\"}",
            clock.instant()
        ));
    }

    private ItemStack create(UUID ownerUuid, PendingDelivery pending) {
        return switch (pending.itemType()) {
            case ELYTRA -> {
                ItemStack elytra = new ItemStack(Material.ELYTRA);
                configureUnbreakable(elytra);
                yield elytra;
            }
            case NAVIGATION -> new ItemStack(Material.COMPASS);
            case LAUNCHPAD -> new ItemStack(
                Material.valueOf(config.launchpad().material()),
                pending.quantity()
            );
            case FIREWORK_ROCKET -> {
                ItemStack rockets = new ItemStack(
                    Material.FIREWORK_ROCKET,
                    pending.quantity()
                );
                int power = config.shopCatalog()
                    .findV002("firework_rocket")
                    .orElseThrow()
                    .flightDuration();
                configureRocket(rockets, power);
                yield rockets;
            }
            case GRAPPLING_HOOK -> leafGrapple.capability()
                == LeafGrappleBridge.Capability.AVAILABLE
                ? leafGrapple.createHook(ownerUuid, 1)
                : null;
        };
    }

    static void configureUnbreakable(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
    }

    static void configureRocket(ItemStack item, int power) {
        if (power != 3 || !(item.getItemMeta() instanceof FireworkMeta meta)) {
            throw new IllegalArgumentException(
                "Frontier rockets require FireworkMeta power 3"
            );
        }
        meta.setPower(3);
        item.setItemMeta(meta);
    }

    private static void annotate(
        ItemStack item,
        UUID ownerUuid,
        PendingDelivery pending
    ) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE, PersistentDataType.STRING, pending.itemType().name());
        pdc.set(OWNER_ID, PersistentDataType.STRING, ownerUuid.toString());
        pdc.set(DELIVERY_ID, PersistentDataType.STRING, pending.deliveryId().toString());
        pdc.set(THEME_ID, PersistentDataType.STRING, pending.themeId());
        if (pending.identity() != null) {
            pdc.set(
                ITEM_INSTANCE_ID,
                PersistentDataType.STRING,
                pending.identity().itemInstanceId().toString()
            );
            pdc.set(
                INSTANCE_EPOCH,
                PersistentDataType.LONG,
                pending.identity().instanceEpoch()
            );
            pdc.set(
                SCHEMA_VERSION,
                PersistentDataType.INTEGER,
                pending.identity().schemaVersion()
            );
        }
        item.setItemMeta(meta);
    }

    private static void annotateLaunchpad(
        ItemStack item,
        UUID ownerUuid,
        PendingDelivery pending,
        UUID itemInstanceId
    ) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ITEM_TYPE, PersistentDataType.STRING, "LAUNCHPAD");
        pdc.set(OWNER_ID, PersistentDataType.STRING, ownerUuid.toString());
        pdc.set(
            DELIVERY_ID,
            PersistentDataType.STRING,
            pending.deliveryId().toString()
        );
        pdc.set(
            ITEM_INSTANCE_ID,
            PersistentDataType.STRING,
            itemInstanceId.toString()
        );
        pdc.set(
            DEFINITION_ID,
            PersistentDataType.STRING,
            LAUNCHPAD_DEFINITION
        );
        pdc.set(SCHEMA_VERSION, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
    }

    private static CompleteManagedPermanent parseCompleteManagedPermanent(
        ItemStack item
    ) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        try {
            String typeText = text(item, ITEM_TYPE);
            if (typeText == null) {
                return null;
            }
            TraversalIdentity.ItemType type =
                TraversalIdentity.ItemType.valueOf(typeText);
            UUID owner = UUID.fromString(text(item, OWNER_ID));
            String theme = text(item, THEME_ID);
            if (!TraversalIdentity.WORLDS_BEYOND.equals(theme)) {
                return null;
            }
            UUID itemInstanceId = UUID.fromString(text(item, ITEM_INSTANCE_ID));
            long epoch = number(item, INSTANCE_EPOCH);
            long schemaLong = number(item, SCHEMA_VERSION);
            if (epoch < 1 || schemaLong < 1) {
                return null;
            }
            int schema = Math.toIntExact(schemaLong);
            return new CompleteManagedPermanent(
                type,
                owner,
                theme,
                itemInstanceId,
                epoch,
                schema
            );
        } catch (RuntimeException failure) {
            return null;
        }
    }

    private boolean isExactCurrentPhysical(
        Player player,
        TraversalIdentity identity,
        TraversalLoadout authority,
        boolean inExactWorld
    ) {
        if (identity == null || authority == null || !inExactWorld) {
            return false;
        }
        TraversalLoadout.LogicalItem logical = authority.permanentItems().stream()
            .filter(value -> value.itemType() == identity.itemType())
            .findFirst()
            .orElse(null);
        if (logical == null
            || logical.state() != TraversalLoadout.LogicalItem.State.ACTIVE
            || !logical.itemInstanceId().equals(identity.itemInstanceId())
            || logical.instanceEpoch() != identity.instanceEpoch()
            || identity.schemaVersion() != 1
            || !identity.ownerUuid().equals(player.getUniqueId())
            || !TraversalIdentity.WORLDS_BEYOND.equals(identity.themeId())) {
            return false;
        }
        return hasExactCurrentPhysical(player, logical, true);
    }

    private boolean hasExactCurrentPhysical(
        Player player,
        TraversalLoadout.LogicalItem logical,
        boolean inExactWorld
    ) {
        if (!inExactWorld
            || logical.state() != TraversalLoadout.LogicalItem.State.ACTIVE) {
            return false;
        }
        for (ItemStack item : allPlayerItems(player)) {
            if (matchesExactCurrent(item, player.getUniqueId(), logical)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesExactCurrent(
        ItemStack item,
        UUID playerUuid,
        TraversalLoadout.LogicalItem logical
    ) {
        CompleteManagedPermanent managed = parseCompleteManagedPermanent(item);
        return managed != null
            && managed.ownerUuid.equals(playerUuid)
            && TraversalIdentity.WORLDS_BEYOND.equals(managed.themeId)
            && managed.itemType == logical.itemType()
            && managed.itemInstanceId.equals(logical.itemInstanceId())
            && managed.instanceEpoch == logical.instanceEpoch()
            && managed.schemaVersion == 1
            && logical.state() == TraversalLoadout.LogicalItem.State.ACTIVE;
    }

    private void cleanupNonCurrentManaged(
        Player player,
        TraversalLoadout authority
    ) {
        clearNonCurrentInArray(player.getInventory().getStorageContents(), slot ->
            player.getInventory().setItem(slot, null), authority, player
        );
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean armorChanged = false;
        for (int slot = 0; slot < armor.length; slot++) {
            if (shouldClearNonCurrent(armor[slot], authority, player)) {
                armor[slot] = null;
                armorChanged = true;
            }
        }
        if (armorChanged) {
            player.getInventory().setArmorContents(armor);
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (shouldClearNonCurrent(offhand, authority, player)) {
            player.getInventory().setItemInOffHand(null);
        }
        ItemStack cursor = player.getItemOnCursor();
        if (shouldClearNonCurrent(cursor, authority, player)) {
            player.setItemOnCursor(null);
        }
    }

    private void clearNonCurrentInArray(
        ItemStack[] contents,
        java.util.function.IntConsumer clearer,
        TraversalLoadout authority,
        Player player
    ) {
        for (int slot = 0; slot < contents.length; slot++) {
            if (shouldClearNonCurrent(contents[slot], authority, player)) {
                clearer.accept(slot);
            }
        }
    }

    private boolean shouldClearNonCurrent(
        ItemStack item,
        TraversalLoadout authority,
        Player player
    ) {
        CompleteManagedPermanent managed = parseCompleteManagedPermanent(item);
        if (managed == null) {
            return false;
        }
        return authority.permanentItems().stream().noneMatch(logical ->
            matchesExactCurrent(item, player.getUniqueId(), logical)
        );
    }

    private EnumSet<TraversalIdentity.ItemType> currentPhysicalPresence(
        Player player,
        TraversalLoadout authority
    ) {
        EnumSet<TraversalIdentity.ItemType> present =
            EnumSet.noneOf(TraversalIdentity.ItemType.class);
        boolean inExact = config.exactWorldName().equals(
            player.getWorld().getName()
        );
        for (TraversalLoadout.LogicalItem logical : authority.permanentItems()) {
            if (hasExactCurrentPhysical(player, logical, inExact)) {
                present.add(logical.itemType());
            }
        }
        return present;
    }

    private static List<ItemStack> allPlayerItems(Player player) {
        List<ItemStack> items = new ArrayList<>();
        ItemStack[] storage = player.getInventory().getStorageContents();
        if (storage != null) {
            for (ItemStack item : storage) {
                if (item != null) {
                    items.add(item);
                }
            }
        }
        ItemStack[] armor = player.getInventory().getArmorContents();
        if (armor != null) {
            for (ItemStack item : armor) {
                if (item != null) {
                    items.add(item);
                }
            }
        }
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            items.add(offhand);
        }
        ItemStack cursor = player.getItemOnCursor();
        if (cursor != null && !cursor.getType().isAir()) {
            items.add(cursor);
        }
        return items;
    }

    private void compensateRemove(Player player, TraversalIdentity identity) {
        clearMatchingIdentity(player.getInventory().getStorageContents(), slot ->
            player.getInventory().setItem(slot, null), identity
        );
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean armorChanged = false;
        for (int slot = 0; slot < armor.length; slot++) {
            if (matchesIdentity(armor[slot], identity)) {
                armor[slot] = null;
                armorChanged = true;
            }
        }
        if (armorChanged) {
            player.getInventory().setArmorContents(armor);
        }
        if (matchesIdentity(player.getInventory().getItemInOffHand(), identity)) {
            player.getInventory().setItemInOffHand(null);
        }
        if (matchesIdentity(player.getItemOnCursor(), identity)) {
            player.setItemOnCursor(null);
        }
    }

    private void clearMatchingIdentity(
        ItemStack[] contents,
        java.util.function.IntConsumer clearer,
        TraversalIdentity identity
    ) {
        for (int slot = 0; slot < contents.length; slot++) {
            if (matchesIdentity(contents[slot], identity)) {
                clearer.accept(slot);
            }
        }
    }

    private static boolean matchesIdentity(
        ItemStack item,
        TraversalIdentity identity
    ) {
        CompleteManagedPermanent managed = parseCompleteManagedPermanent(item);
        return managed != null
            && managed.itemType == identity.itemType()
            && managed.itemInstanceId.equals(identity.itemInstanceId())
            && managed.instanceEpoch == identity.instanceEpoch()
            && managed.schemaVersion == identity.schemaVersion()
            && managed.ownerUuid.equals(identity.ownerUuid())
            && managed.themeId.equals(identity.themeId());
    }

    private static boolean permanent(ItemStack item) {
        return parseCompleteManagedPermanent(item) != null
            || isPermanentTypeTag(item);
    }

    private static boolean isPermanentTypeTag(ItemStack item) {
        String type = text(item, ITEM_TYPE);
        return type != null && switch (type) {
            case "ELYTRA", "GRAPPLING_HOOK", "NAVIGATION" -> true;
            default -> false;
        };
    }

    private boolean validPermanent(Player player, ItemStack item) {
        TraversalLoadout authority = authorities.get(player.getUniqueId());
        if (authority == null
            || !config.exactWorldName().equals(player.getWorld().getName())) {
            return false;
        }
        CompleteManagedPermanent managed = parseCompleteManagedPermanent(item);
        if (managed == null || !managed.ownerUuid.equals(player.getUniqueId())) {
            return false;
        }
        return authority.permanentItems().stream().anyMatch(logical ->
            matchesExactCurrent(item, player.getUniqueId(), logical)
        );
    }

    private void notifySafeEntryResult(
        Player player,
        TraversalDeliveryCoordinator.Result result
    ) {
        if (result.inventoryFull() > 0) {
            player.sendMessage(
                "Your Worlds Beyond loadout could not fit. Free inventory space and re-enter."
            );
        }
        if (result.capabilityUnavailable() > 0) {
            player.sendMessage(
                "A Worlds Beyond loadout item could not be granted right now."
            );
        }
        if (result.conflict() > 0 || result.unknown() > 0) {
            player.sendMessage(
                "A Worlds Beyond loadout delivery needs admin review. Do not retry blindly."
            );
        } else if (result.delivered() > 0) {
            player.sendMessage("Worlds Beyond loadout items were delivered.");
        }
    }

    private final class DeliveryGatewayAdapter
        implements TraversalDeliveryCoordinator.DeliveryGateway {
        @Override
        public TraversalDeliveryCoordinator.DeliveryOutcome deliverIfStillEligible(
            UUID playerUuid,
            PendingDelivery delivery
        ) {
            return deliver(playerUuid, delivery);
        }

        @Override
        public boolean isOnline(UUID playerUuid) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            return accepting.get() && player != null && player.isOnline();
        }

        @Override
        public boolean isOnlineInExactWorld(UUID playerUuid, String exactWorldName) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            return accepting.get()
                && player != null
                && player.isOnline()
                && exactWorldName.equals(player.getWorld().getName());
        }

        @Override
        public void cleanupNonCurrentManaged(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                FrontierGameplayRuntime.this.cleanupNonCurrentManaged(
                    player,
                    loadout
                );
            }
        }

        @Override
        public EnumSet<TraversalIdentity.ItemType> currentPhysicalPresence(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player == null || !player.isOnline()) {
                return EnumSet.noneOf(TraversalIdentity.ItemType.class);
            }
            return FrontierGameplayRuntime.this.currentPhysicalPresence(
                player,
                loadout
            );
        }

        @Override
        public void applyAuthorityCache(
            UUID playerUuid,
            TraversalLoadout loadout
        ) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player == null
                || !player.isOnline()
                || !config.exactWorldName().equals(player.getWorld().getName())) {
                authorities.remove(playerUuid);
                return;
            }
            authorities.put(playerUuid, loadout);
        }

        @Override
        public void notifySafeEntryResult(
            UUID playerUuid,
            TraversalDeliveryCoordinator.Result result
        ) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player == null || !player.isOnline()) {
                return;
            }
            if (result.playerOffline() > 0 || result.leftTheme() > 0) {
                return;
            }
            FrontierGameplayRuntime.this.notifySafeEntryResult(player, result);
        }

        @Override
        public void compensateRemove(
            UUID playerUuid,
            TraversalIdentity identity
        ) {
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                FrontierGameplayRuntime.this.compensateRemove(player, identity);
            }
        }
    }

    private final class DeliveryAuditAdapter
        implements TraversalDeliveryCoordinator.DeliveryAudit {
        @Override
        public void deathTransition(
            DeathIdentitySnapshot snapshot,
            DeathPersistResult result
        ) {
            services.audit().record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "FRONTIER_DEATH_PENDING_" + result.name(),
                snapshot.playerUuid(),
                "FRONTIER_LOADOUT",
                snapshot.itemType().name(),
                services.serverId(),
                "{\"result\":\"" + result.name() + "\"}",
                clock.instant()
            ));
        }

        @Override
        public void deathConflict(
            DeathIdentitySnapshot snapshot,
            DeathPersistResult result
        ) {
            plugin.getLogger().warning(
                "Frontier death pending conflict for loadout item type."
            );
        }

        @Override
        public void deliveryTransitioned(UUID deliveryId) {
            services.audit().record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "FRONTIER_DELIVERY_DELIVERED",
                null,
                "FRONTIER_DELIVERY",
                deliveryId.toString(),
                services.serverId(),
                "{\"result\":\"TRANSITIONED_TO_DELIVERED\"}",
                clock.instant()
            ));
        }

        @Override
        public void deliveryConflict(
            UUID deliveryId,
            DeliveryCompletion completion
        ) {
            plugin.getLogger().warning(
                "Frontier delivery completion requires admin review."
            );
            services.audit().record(new WayfarerAudit.AuditEvent(
                UUID.randomUUID(),
                "FRONTIER_DELIVERY_CONFLICT",
                null,
                "FRONTIER_DELIVERY",
                deliveryId.toString(),
                services.serverId(),
                "{\"result\":\"" + completion.name() + "\"}",
                clock.instant()
            ));
        }

        @Override
        public void deliveryRepositoryFailure(UUID deliveryId) {
            plugin.getLogger().warning(
                "Frontier delivery repository operation failed."
            );
        }
    }

    private static final class CompleteManagedPermanent {
        private final TraversalIdentity.ItemType itemType;
        private final UUID ownerUuid;
        private final String themeId;
        private final UUID itemInstanceId;
        private final long instanceEpoch;
        private final int schemaVersion;

        private CompleteManagedPermanent(
            TraversalIdentity.ItemType itemType,
            UUID ownerUuid,
            String themeId,
            UUID itemInstanceId,
            long instanceEpoch,
            int schemaVersion
        ) {
            this.itemType = itemType;
            this.ownerUuid = ownerUuid;
            this.themeId = themeId;
            this.itemInstanceId = itemInstanceId;
            this.instanceEpoch = instanceEpoch;
            this.schemaVersion = schemaVersion;
        }
    }

    public void openNavigation(Player player) {
        if (!config.exactWorldName().equals(player.getWorld().getName())
            || !authorities.containsKey(player.getUniqueId())) {
            player.sendMessage(
                "Wayfarer navigation is available only in frontier_iris "
                    + "after loadout initialization."
            );
            return;
        }
        NavigationHolder holder = new NavigationHolder(
            NavigationMode.MAIN,
            UUID.randomUUID(),
            null
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Wayfarer Navigation")
        );
        holder.inventory = inventory;
        inventory.setItem(10, named(
            Material.EMERALD,
            "Shop",
            List.of("Launchpads and Flight Duration 3 rockets")
        ));
        inventory.setItem(13, named(
            Material.ELYTRA,
            "Loadout",
            List.of("Permanent item status and reissue guidance")
        ));
        inventory.setItem(16, named(
            Material.PAPER,
            "Help",
            List.of("Traversal, launchpads, shop and delivery status")
        ));
        inventory.setItem(22, named(
            Material.BARRIER,
            "Waystone Unavailable",
            List.of("Deferred by V0.0.2 requirement")
        ));
        player.openInventory(inventory);
    }

    private void openShop(Player player) {
        NavigationHolder holder = new NavigationHolder(
            NavigationMode.SHOP,
            UUID.randomUUID(),
            null
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Wayfarer Shop")
        );
        holder.inventory = inventory;
        var launchpad = config.shopCatalog()
            .findV002("launchpad")
            .orElseThrow();
        var rockets = config.shopCatalog()
            .findV002("firework_rocket")
            .orElseThrow();
        inventory.setItem(11, named(
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
            "Launchpad",
            List.of(
                "Quantity: " + launchpad.quantity(),
                "Price: " + launchpad.priceWaymark() + " WM"
            )
        ));
        inventory.setItem(15, named(
            Material.FIREWORK_ROCKET,
            "Flight Duration 3 Rocket",
            List.of(
                "Quantity: " + rockets.quantity(),
                "Price: " + rockets.priceWaymark() + " WM"
            )
        ));
        player.openInventory(inventory);
    }

    private void openLoadout(Player player) {
        TraversalLoadout loadout = authorities.get(player.getUniqueId());
        if (loadout == null) {
            player.sendMessage("Frontier loadout is unavailable.");
            return;
        }
        NavigationHolder holder = new NavigationHolder(
            NavigationMode.LOADOUT,
            UUID.randomUUID(),
            null
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Frontier Loadout")
        );
        holder.inventory = inventory;
        int slot = 11;
        for (TraversalLoadout.LogicalItem item : loadout.permanentItems()) {
            Material material = switch (item.itemType()) {
                case ELYTRA -> Material.ELYTRA;
                case GRAPPLING_HOOK -> Material.FISHING_ROD;
                case NAVIGATION -> Material.COMPASS;
            };
            inventory.setItem(slot, named(
                material,
                item.itemType().name(),
                List.of(
                    "State: " + item.state(),
                    "Epoch: " + item.instanceEpoch(),
                    "Admin-assisted reissue is available if missing"
                )
            ));
            slot += 2;
        }
        player.openInventory(inventory);
    }

    private void openPurchaseConfirm(Player player, String offerId) {
        var offer = config.shopCatalog().findV002(offerId).orElse(null);
        if (offer == null || purchaseCoordinator == null) {
            player.sendMessage("Frontier purchase is unavailable.");
            return;
        }
        UUID token = UUID.randomUUID();
        NavigationSession session = new NavigationSession(
            token,
            offerId,
            new SingleUseGate()
        );
        navigationSessions.put(player.getUniqueId(), session);
        NavigationHolder holder = new NavigationHolder(
            NavigationMode.PURCHASE_CONFIRM,
            token,
            offerId
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Confirm Frontier Purchase")
        );
        holder.inventory = inventory;
        inventory.setItem(11, named(
            Material.LIME_CONCRETE,
            "Confirm",
            List.of(
                "Offer: " + offer.offerId(),
                "Price: " + offer.priceWaymark() + " WM"
            )
        ));
        inventory.setItem(15, named(
            Material.BARRIER,
            "Cancel",
            List.of("No Waymark will be charged")
        ));
        player.openInventory(inventory);
    }

    private void handleNavigationClick(
        Player player,
        int rawSlot,
        NavigationHolder holder
    ) {
        if (rawSlot < 0 || rawSlot >= 27
            || !config.exactWorldName().equals(
                player.getWorld().getName()
            )) {
            return;
        }
        switch (holder.mode()) {
            case MAIN -> {
                switch (NavigationGuiAction.main(rawSlot)) {
                    case OPEN_SHOP -> openShop(player);
                    case OPEN_LOADOUT -> openLoadout(player);
                    case HELP -> player.sendMessage(
                        "Use the navigation item for Shop, Loadout and Help. "
                            + "Waystones are unavailable in V0.0.2."
                    );
                    case WAYSTONE_UNAVAILABLE -> player.sendMessage(
                        "Waystone is unavailable in V0.0.2."
                    );
                    default -> {
                        // Display-only slot.
                    }
                }
            }
            case SHOP -> {
                switch (NavigationGuiAction.shop(rawSlot)) {
                    case SELECT_LAUNCHPAD ->
                        openPurchaseConfirm(player, "launchpad");
                    case SELECT_ROCKET ->
                        openPurchaseConfirm(player, "firework_rocket");
                    default -> {
                        // Display-only slot.
                    }
                }
            }
            case LOADOUT, PURCHASE_RESULT -> {
                // Display-only surfaces.
            }
            case PURCHASE_CONFIRM -> {
                switch (NavigationGuiAction.purchaseConfirm(rawSlot)) {
                    case CONFIRM_PURCHASE -> confirmPurchase(player, holder);
                    case CANCEL -> {
                        navigationSessions.remove(player.getUniqueId());
                        player.closeInventory();
                    }
                    default -> {
                        // Display-only slot.
                    }
                }
            }
        }
    }

    private void confirmPurchase(
        Player player,
        NavigationHolder holder
    ) {
        NavigationSession session = navigationSessions.get(
            player.getUniqueId()
        );
        FrontierPurchaseCoordinator coordinator = purchaseCoordinator;
        if (session == null || coordinator == null
            || !session.token().equals(holder.token())
            || !session.offerId().equals(holder.offerId())
            || !session.accepted().tryAcquire()) {
            player.sendMessage("Frontier purchase confirmation expired.");
            return;
        }
        navigationSessions.remove(player.getUniqueId(), session);
        player.closeInventory();
        coordinator.purchase(new FrontierPurchaseCoordinator.Request(
            "frontier-gui-shop:" + session.token(),
            player.getUniqueId(),
            player.getWorld().getName(),
            session.offerId()
        )).whenComplete((result, failure) ->
            services.tasks().mainThread(() -> {
                Player online = plugin.getServer().getPlayer(
                    player.getUniqueId()
                );
                if (online == null || !online.isOnline()) {
                    return;
                }
                if (failure != null) {
                    online.sendMessage("Frontier purchase is unavailable.");
                    return;
                }
                showPurchaseResult(online, result);
            })
        );
    }

    private void showPurchaseResult(
        Player player,
        FrontierPurchaseCoordinator.Result result
    ) {
        NavigationHolder holder = new NavigationHolder(
            NavigationMode.PURCHASE_RESULT,
            UUID.randomUUID(),
            null
        );
        Inventory inventory = org.bukkit.Bukkit.createInventory(
            holder,
            27,
            net.kyori.adventure.text.Component.text("Frontier Purchase Result")
        );
        holder.inventory = inventory;
        Material material = switch (result.status()) {
            case DELIVERED -> Material.LIME_CONCRETE;
            case PENDING -> Material.YELLOW_CONCRETE;
            case FAILED -> Material.RED_CONCRETE;
            case UNKNOWN -> Material.ORANGE_CONCRETE;
        };
        inventory.setItem(13, named(
            material,
            result.status().name(),
            List.of(switch (result.status()) {
                case DELIVERED -> "Item delivered.";
                case PENDING ->
                    "Payment accepted; durable delivery is pending.";
                case FAILED -> "Payment was not committed.";
                case UNKNOWN ->
                    "State requires administrator reconciliation.";
            })
        ));
        player.openInventory(inventory);
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

    private static TraversalLoadout replaceAuthority(
        TraversalLoadout loadout,
        TraversalIdentity identity
    ) {
        List<TraversalLoadout.LogicalItem> items =
            new java.util.ArrayList<>(loadout.permanentItems());
        items.removeIf(item -> item.itemType() == identity.itemType());
        items.add(new TraversalLoadout.LogicalItem(
            identity.itemType(),
            identity.itemInstanceId(),
            identity.instanceEpoch(),
            TraversalLoadout.LogicalItem.State.ACTIVE
        ));
        return new TraversalLoadout(
            loadout.playerUuid(),
            loadout.themeId(),
            loadout.firstJoinedAt(),
            loadout.initialLaunchpadsGranted(),
            items,
            loadout.lockVersion()
        );
    }

    private static boolean isPermanent(PendingDelivery.ItemType type) {
        return switch (type) {
            case ELYTRA, GRAPPLING_HOOK, NAVIGATION -> true;
            case LAUNCHPAD, FIREWORK_ROCKET -> false;
        };
    }

    private Optional<LaunchpadItemClaim> launchpadClaim(ItemStack item) {
        java.util.Map<String, String> raw = new java.util.HashMap<>();
        put(raw, "item_type", text(item, ITEM_TYPE));
        put(raw, "item_instance_id", text(item, ITEM_INSTANCE_ID));
        put(raw, "definition_id", text(item, DEFINITION_ID));
        long schema = number(item, SCHEMA_VERSION);
        if (schema != Long.MIN_VALUE) {
            raw.put("schema_version", Long.toString(schema));
        }
        return LaunchpadItemClaim.parse(raw).claim();
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

    private static UUID parseUuid(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (IllegalArgumentException failure) {
            return null;
        }
    }

    private LaunchpadPlacementPolicy.Snapshot placementSnapshot(
        Player player,
        org.bukkit.block.Block target,
        boolean targetAir
    ) {
        org.bukkit.World world = target.getWorld();
        org.bukkit.block.Block support = target.getRelative(
            org.bukkit.block.BlockFace.DOWN
        );
        boolean regionDenied = false;
        if (worldGuard != null) {
            try {
                regionDenied = worldGuard.denied(
                    player,
                    target.getLocation()
                );
            } catch (RuntimeException failure) {
                regionDenied = true;
            }
        }
        return new LaunchpadPlacementPolicy.Snapshot(
            world.getName(),
            world.isChunkLoaded(target.getX() >> 4, target.getZ() >> 4),
            targetAir,
            support.getType().isSolid(),
            target.isLiquid() || support.isLiquid(),
            world.getWorldBorder().isInside(target.getLocation()),
            protectedSystemArea(target),
            regionDenied,
            false,
            activeLaunchpads.contains(location(target))
        );
    }

    private static boolean protectedSystemArea(
        org.bukkit.block.Block target
    ) {
        org.bukkit.Location spawn = target.getWorld().getSpawnLocation();
        double dx = target.getX() + 0.5D - spawn.getX();
        double dz = target.getZ() + 0.5D - spawn.getZ();
        if (dx * dx + dz * dz <= 256D * 256D) {
            return true;
        }
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    Material material = target.getRelative(x, y, z).getType();
                    if (material == Material.NETHER_PORTAL
                        || material == Material.END_PORTAL
                        || material == Material.END_GATEWAY) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    private boolean isProtectedLaunchpadMaterial(org.bukkit.block.Block block) {
        return config.exactWorldName().equals(block.getWorld().getName())
            && ((!launchpadIndexReady.get()
                && block.getType()
                    == Material.valueOf(config.launchpad().material()))
                || activeLaunchpads.contains(location(block)));
    }

    private static Launchpad.Location location(org.bukkit.block.Block block) {
        return new Launchpad.Location(
            block.getWorld().getName(),
            block.getX(),
            block.getY(),
            block.getZ()
        );
    }

    private final class LaunchpadGateway
        implements LaunchpadUseCoordinator.LaunchGateway {
        @Override
        public boolean safeToLaunch(UUID playerUuid, Launchpad launchpad) {
            if (!accepting.get()) {
                return false;
            }
            Player player = plugin.getServer().getPlayer(playerUuid);
            if (player == null || !player.isOnline()
                || !config.exactWorldName().equals(
                    player.getWorld().getName()
                )) {
                return false;
            }
            var world = player.getWorld();
            var block = world.getBlockAt(
                launchpad.location().x(),
                launchpad.location().y(),
                launchpad.location().z()
            );
            return block.getType()
                == Material.valueOf(config.launchpad().material())
                && block.getRelative(0, 1, 0).isPassable()
                && block.getRelative(0, 2, 0).isPassable();
        }

        @Override
        public void launch(UUID playerUuid, Launchpad launchpad) {
            if (!accepting.get()) {
                return;
            }
            Player player = java.util.Objects.requireNonNull(
                plugin.getServer().getPlayer(playerUuid),
                "online player"
            );
            Vector direction = player.getLocation().getDirection()
                .setY(0)
                .normalize()
                .multiply(config.launchpad().horizontalVelocity());
            direction.setY(config.launchpad().verticalVelocity());
            player.setVelocity(direction);
            if (config.launchpad().autoDeployElytra()
                && player.getInventory().getChestplate() != null
                && player.getInventory().getChestplate().getType()
                == Material.ELYTRA) {
                player.setGliding(true);
            }
        }

        @Override
        public void afterPersisted(Launchpad launchpad) {
            if (!accepting.get()
                || launchpad.state() != Launchpad.State.EXHAUSTED) {
                return;
            }
            org.bukkit.World world = plugin.getServer().getWorld(
                launchpad.location().worldId()
            );
            if (world == null) {
                return;
            }
            org.bukkit.block.Block block = world.getBlockAt(
                launchpad.location().x(),
                launchpad.location().y(),
                launchpad.location().z()
            );
            if (block.getType()
                == Material.valueOf(config.launchpad().material())) {
                block.setType(Material.AIR, false);
            }
            activeLaunchpads.deactivate(
                launchpad.location(),
                launchpad.launchpadId()
            );
            recordLaunchpad(
                launchpad,
                null,
                "LAUNCHPAD_EXHAUSTED",
                "REMOVED"
            ).exceptionally(ignored -> null);
        }
    }

    public record LaunchpadInspection(
        Launchpad.State state,
        ReconcileClassification classification
    ) {}

    public enum ReconcileClassification {
        DB_AND_BLOCK_MATCH,
        DB_ONLY,
        BLOCK_ONLY,
        CONFLICT,
        UNKNOWN,
        NOT_FOUND
    }

    public enum AdminLaunchpadResult {
        APPLIED,
        NO_CHANGE,
        NOT_FOUND,
        CONFLICT,
        MANUAL_REVIEW_REQUIRED,
        UNAVAILABLE
    }

    public enum AdminLoadoutResult {
        APPLIED,
        NOT_FOUND_OR_CONFLICT,
        UNAVAILABLE
    }

    private static final class InspectionCapture {
        private ReconcileClassification classification =
            ReconcileClassification.UNKNOWN;
    }

    private record RemovalCapture(
        Launchpad launchpad,
        boolean removed
    ) {}

    private record PlacementRequest(
        UUID playerUuid,
        Launchpad.Location location,
        EquipmentSlot hand,
        LaunchpadItemClaim claim,
        UUID deliveryId,
        Launchpad launchpad
    ) {}

    private enum NavigationMode {
        MAIN,
        SHOP,
        LOADOUT,
        PURCHASE_CONFIRM,
        PURCHASE_RESULT
    }

    private static final class NavigationHolder implements InventoryHolder {
        private final NavigationMode mode;
        private final UUID token;
        private final String offerId;
        private Inventory inventory;

        private NavigationHolder(
            NavigationMode mode,
            UUID token,
            String offerId
        ) {
            this.mode = mode;
            this.token = token;
            this.offerId = offerId;
        }

        private NavigationMode mode() {
            return mode;
        }

        private UUID token() {
            return token;
        }

        private String offerId() {
            return offerId;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    private record NavigationSession(
        UUID token,
        String offerId,
        SingleUseGate accepted
    ) {}

    private static final class RetryCapture {
        private String worldName;
    }
}
