package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.frontier.application.TraversalDeliveryCoordinator;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadUseCoordinator;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import io.github.eariver.wayfarer.integration.leafgrapple.LeafGrappleBridge;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Clock;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.Instant;
import java.time.Duration;

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
    private final JavaPlugin plugin;
    private final FrontierModuleConfig config;
    private final LeafGrappleBridge leafGrapple;
    private final TraversalDeliveryCoordinator delivery;
    private final WayfarerServices services;
    private final TraversalLoadoutRepository loadouts;
    private final LaunchpadRepository launchpads;
    private final LaunchpadUseCoordinator launchpadUse;
    private final ConcurrentHashMap<UUID, Instant> cooldowns =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, TraversalLoadout> authorities =
        new ConcurrentHashMap<>();
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final Clock clock;
    private final BukkitTask reconcileTask;

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
            this::deliver,
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
    }

    public void stop() {
        accepting.set(false);
        reconcileTask.cancel();
        cooldowns.clear();
        authorities.clear();
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
        String owner = text(dropped.getItemStack(), OWNER_ID);
        if (owner != null && !owner.equals(player.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack used = event.getItemInHand();
        if (!config.exactWorldName().equals(player.getWorld().getName())
            || event.getBlockPlaced().getType()
            != Material.valueOf(config.launchpad().material())
            || !"LAUNCHPAD".equals(text(used, ITEM_TYPE))
            || !player.getUniqueId().toString().equals(text(used, OWNER_ID))) {
            return;
        }
        event.setCancelled(true);
        var block = event.getBlockPlaced();
        Launchpad launchpad = new Launchpad(
            UUID.randomUUID(),
            location(block),
            player.getLocation().getYaw(),
            player.getUniqueId(),
            0,
            config.launchpad().maximumSuccessfulUses(),
            Instant.now(),
            null,
            Instant.now().plus(config.launchpad().expiration()),
            "frontier-v1",
            Launchpad.State.ACTIVE,
            1,
            0
        );
        services.tasks().database(() ->
            launchpads.create(
                launchpad,
                config.launchpad().maximumActivePerPlayer(),
                Instant.now()
            )
        ).thenAccept(created -> services.tasks().mainThread(() -> {
            if (!accepting.get()) {
                return;
            }
            ItemStack current = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
            if (!created || !player.isOnline()
                || !config.exactWorldName().equals(player.getWorld().getName())
                || !block.getType().isAir()
                || !"LAUNCHPAD".equals(text(current, ITEM_TYPE))) {
                if (created) {
                    services.tasks().database(() -> launchpads.remove(
                        launchpad.launchpadId(),
                        launchpad.lockVersion(),
                        Launchpad.State.RECONCILED_REMOVED,
                        Instant.now()
                    ));
                }
                return;
            }
            block.setType(Material.valueOf(config.launchpad().material()), false);
            current.setAmount(current.getAmount() - 1);
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
        event.setCancelled(true);
        Launchpad.Location location = location(event.getClickedBlock());
        services.tasks().database(() -> launchpads.findAt(location))
            .thenAccept(found -> {
                if (!accepting.get()) {
                    return;
                }
                found.ifPresent(launchpad ->
                launchpadUse.use(new LaunchpadUseCoordinator.Request(
                    launchpad.launchpadId(),
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getWorld().getName(),
                    config.launchpad().disableWhileSneaking()
                        && event.getPlayer().isSneaking(),
                    cooldowns.get(event.getPlayer().getUniqueId())
                )).thenAccept(result -> {
                    if (result.outcome() == Launchpad.Outcome.LAUNCHED) {
                        cooldowns.put(
                            event.getPlayer().getUniqueId(),
                            Instant.now().plus(config.launchpad().cooldown())
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
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        if ((current != null && permanent(current)
            && !validPermanent(player, current))
            || (cursor != null && permanent(cursor)
                && !validPermanent(player, cursor))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!config.exactWorldName().equals(event.getBlock().getWorld().getName())
            || event.getBlock().getType()
            != Material.valueOf(config.launchpad().material())) {
            return;
        }
        event.setCancelled(true);
        Launchpad.Location location = location(event.getBlock());
        services.tasks().database(() -> launchpads.findAt(location))
            .thenAccept(found -> services.tasks().mainThread(() -> {
                if (!accepting.get()) {
                    return;
                }
                if (found.isEmpty()) {
                    event.getBlock().breakNaturally();
                    return;
                }
                Launchpad launchpad = found.orElseThrow();
                if (!config.launchpad().allowPlayerBreak()
                    || (!launchpad.placerUuid().equals(
                        event.getPlayer().getUniqueId()
                    ) && !event.getPlayer().hasPermission(
                        "wayfarer.frontier.admin"
                    ))) {
                    return;
                }
                services.tasks().database(() -> launchpads.remove(
                    launchpad.launchpadId(),
                    launchpad.lockVersion(),
                    Launchpad.State.PLAYER_BROKEN,
                    Instant.now()
                )).thenAccept(removed -> services.tasks().mainThread(() -> {
                    if (removed) {
                        event.getBlock().setType(Material.AIR, false);
                    }
                }));
            }));
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
        if (isProtectedLaunchpadMaterial(event.getBlock())) {
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
            .thenCompose(ignored -> services.tasks().database(() ->
                loadouts.find(playerUuid)
            )).thenAccept(found ->
                found.ifPresent(loadout -> authorities.put(playerUuid, loadout))
            ).exceptionally(ignored -> null);
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
        if (isPermanent(pending.itemType()) && pending.identity() == null) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.CAPABILITY_UNAVAILABLE;
        }
        if (isPermanent(pending.itemType())
            && containsIdentity(player, pending.identity())) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
        }
        if (isPermanent(pending.itemType())) {
            removeStalePermanent(player, pending);
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
        if (pending.identity() != null) {
            TraversalLoadout loadout = authorities.get(playerUuid);
            if (loadout != null) {
                authorities.put(
                    playerUuid,
                    replaceAuthority(loadout, pending.identity())
                );
            }
        }
        return TraversalDeliveryCoordinator.DeliveryOutcome.DELIVERED;
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
        return services.tasks().database(() ->
            loadouts.reissuePermanent(playerUuid, itemType, clock.instant())
        ).thenCompose(reissued -> {
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
            )).handle((ignored, failure) -> reissued)
                .thenCompose(ignored -> retryDelivery(playerUuid))
                .thenCompose(ignored -> services.tasks().database(() ->
                    loadouts.find(playerUuid)
                )).thenApply(found -> {
                    found.ifPresent(value -> authorities.put(playerUuid, value));
                    return AdminLoadoutResult.APPLIED;
                });
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
                new TraversalDeliveryCoordinator.Result(0, 0, false, true)
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
                    return CompletableFuture.completedFuture(
                        new LaunchpadInspection(
                            null,
                            ReconcileClassification.NOT_FOUND
                        )
                    );
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

    private ItemStack create(UUID ownerUuid, PendingDelivery pending) {
        return switch (pending.itemType()) {
            case ELYTRA -> new ItemStack(Material.ELYTRA);
            case NAVIGATION -> new ItemStack(Material.COMPASS);
            case LAUNCHPAD -> new ItemStack(
                Material.valueOf(config.launchpad().material()),
                pending.quantity()
            );
            case FIREWORK_ROCKET -> new ItemStack(
                Material.FIREWORK_ROCKET,
                pending.quantity()
            );
            case GRAPPLING_HOOK -> leafGrapple.capability()
                == LeafGrappleBridge.Capability.AVAILABLE
                ? leafGrapple.createHook(ownerUuid, 1)
                : null;
        };
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

    private static boolean containsIdentity(
        Player player,
        TraversalIdentity identity
    ) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null
                && identity.itemInstanceId().toString().equals(
                    text(item, ITEM_INSTANCE_ID)
                )
                && identity.instanceEpoch() == number(item, INSTANCE_EPOCH)) {
                return true;
            }
        }
        return false;
    }

    private static void removeStalePermanent(
        Player player,
        PendingDelivery pending
    ) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack item = contents[slot];
            if (item != null
                && pending.itemType().name().equals(text(item, ITEM_TYPE))
                && pending.playerUuid().toString().equals(text(item, OWNER_ID))
                && !pending.identity().itemInstanceId().toString().equals(
                    text(item, ITEM_INSTANCE_ID)
                )) {
                player.getInventory().setItem(slot, null);
            }
        }
    }

    private static boolean permanent(ItemStack item) {
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
        try {
            TraversalIdentity.ItemType type = TraversalIdentity.ItemType.valueOf(
                text(item, ITEM_TYPE)
            );
            UUID itemInstanceId = UUID.fromString(
                text(item, ITEM_INSTANCE_ID)
            );
            long epoch = number(item, INSTANCE_EPOCH);
            int schema = Math.toIntExact(number(item, SCHEMA_VERSION));
            TraversalLoadout.LogicalItem logical = authority.permanentItems()
                .stream()
                .filter(value -> value.itemType() == type)
                .findFirst()
                .orElse(null);
            if (logical == null
                || logical.state()
                != TraversalLoadout.LogicalItem.State.ACTIVE
                || !logical.itemInstanceId().equals(itemInstanceId)) {
                return false;
            }
            TraversalIdentity identity = new TraversalIdentity(
                itemInstanceId,
                type,
                UUID.fromString(text(item, OWNER_ID)),
                text(item, THEME_ID),
                epoch,
                schema
            );
            return identity.validate(
                player.getUniqueId(),
                TraversalIdentity.WORLDS_BEYOND,
                logical.instanceEpoch()
            ) == TraversalIdentity.Validation.VALID;
        } catch (RuntimeException failure) {
            return false;
        }
    }

    public void openNavigation(Player player) {
        var inventory = org.bukkit.Bukkit.createInventory(
            null,
            27,
            net.kyori.adventure.text.Component.text("Wayfarer Navigation")
        );
        inventory.setItem(11, new ItemStack(Material.LIGHT_WEIGHTED_PRESSURE_PLATE));
        inventory.setItem(13, new ItemStack(Material.COMPASS));
        inventory.setItem(15, new ItemStack(Material.FIREWORK_ROCKET));
        player.openInventory(inventory);
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
            && block.getType() == Material.valueOf(config.launchpad().material());
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

    private static final class RetryCapture {
        private String worldName;
    }
}
