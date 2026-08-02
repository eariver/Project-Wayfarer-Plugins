­r‡^Ñf¥–Ø¦{OlyÊ'vÃ®¶›­package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.frontier.application.DeliveryRetryClassification;
import io.github.eariver.wayfarer.frontier.application.TraversalDeliveryCoordinator;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseCoordinator;
import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadUseCoordinator;
import io.github.eariver.wayfarer.frontier.domain.DeathIdentitySnapshot;
import io.github.eariver.wayfarer.frontier.domain.DeathPersistResult;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import io.github.eariver.wayfarer.frontier.domain.ManagedPermanentIdentity;
import io.github.eariver.wayfarer.frontier.domain.LaunchpadPlacementPolicy;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity;
import io.github.eariver.wayfarer.frontier.domain.TraversalLoadout;
import io.github.eariver.wayfarer.frontier.identity.LaunchpadItemClaim;
import io.github.eariver.wayfarer.frontier.integration.WorldGuardPlacementBridge;
import io.github.eariver.wayfarer.frontier.integration.WorldEditLaunchpadProtection;
import io.github.eariver.wayfarer.frontier.integration.MultiverseInventoriesReadinessListener;
import io.github.eariver.wayfarer.frontier.integration.MviShareObservation;
import io.github.eariver.wayfarer.frontier.application.SafeEntryReadiness;
import io.github.eariver.wayfarer.frontier.application.EntryCycleRegistry;
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
import org.bukkit.event.player.PlayerQuitEvent;
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
import java.util.Comparator;
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
    private static final long FINGERPRINT_POLL_PERIOD_TICKS = 5L;
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
    private final SafeEntryReadiness entryReadiness = new SafeEntryReadiness();
    private final ConcurrentHashMap<UUID, BukkitTask> entryReadinessTasks =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, EntryContext> entryContexts =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, LateEntryContext> lateEntryContexts =
        new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, BukkitTask> lateMviRestartTasks =
        new ConcurrentHashMap<>();
    private final EntryCycleRegistry entryCycles = new EntryCycleRegistry();
    private final Listener mviReadinessListener;
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
        if (plugin.getServer().getPluginManager()
            .isPluginEnabled("Multiverse-Inventories")) {
            mviReadinessListener = new MultiverseInventoriesReadinessListener(
                this::onMviObservation
            );
            plugin.getServer().getPluginManager().registerEvents(
                mviReadinessListener,
                plugin
            );
        } else {
            mviReadinessListener = null;
        }
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
        entryReadinessTasks.values().forEach(BukkitTask::cancel);
        entryReadinessTasks.clear();
        lateMviRestartTasks.values().forEach(BukkitTask::cancel);
        lateMviRestartTasks.clear();
        entryContexts.clear();
        lateEntryContexts.clear();
        entryReadiness.cancelAll();
        entryCycles.clearAll();
        if (mviReadinessListener != null) {
            org.bukkit.event.HandlerList.unregisterAll(mviReadinessListener);
        }
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
        plugin.getLogger().info(
            "PLAYER_JOIN; world=" + event.getPlayer().getWorld().getName()
        );
        enter(event.getPlayer());
    }

    @EventHandler
    public void onWorldChanged(PlayerChangedWorldEvent event) {
        plugin.getLogger().info(
            "PLAYER_CHANGED_WORLD; from=" + event.getFrom().getName()
                + "; to=" + event.getPlayer().getWorld().getName()
        );
        enter(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getLogger().info("PLAYER_QUIT; safe entry cancelled");
        cancelEntry(event.getPlayer().getUniqueId());
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
        ißOxÖÚ$z{-®éÜj×åW&6†6T6öæf—&Ò‡Æ–W"Â&f—&Wv÷&µ÷&ö6¶WB"“°¢FVfVÇBÓâ°¢òòF—7Æ’ÖöæÇ’6Æ÷Bà¢Ğ¢Ğ¢Ğ¢66RÄôDõUBÂU$4„4Uõ$U5TÅBÓâ°¢òòF—7Æ’ÖöæÇ’7W&f6W2à¢Ğ¢66RU$4„4Uô4ôäd•$ÒÓâ°¢7v—F6‚„æf–vF–öäwV”7F–öâçW&6†6T6öæf—&Ò‡&u6Æ÷B’’°¢66R4ôäd•$ÕõU$4„4RÓâ6öæf—&ÕW&6†6R‡Æ–W"Â†öÆFW"“°¢66R4ä4TÂÓâ°¢æf–vF–öå6W76–öç2ç&VÖ÷fR‡Æ–W"ævWEVæ—VT–B‚’“°¢Æ–W"æ6Æ÷6T–çfVçF÷'’‚“°¢Ğ¢FVfVÇBÓâ°¢òòF—7Æ’ÖöæÇ’6Æ÷Bà¢Ğ¢Ğ¢Ğ¢Ğ¢Ğ ¢&—fFRfö–B6öæf—&ÕW&6†6R€¢Æ–W"Æ–W"À¢æf–vF–öä†öÆFW"†öÆFW ¢’°¢æf–vF–öå6W76–öâ6W76–öâÒæf–vF–öå6W76–öç2ævWB€¢Æ–W"ævWEVæ—VT–B‚¢“°¢g&öçF–W%W&6†6T6ö÷&F–æF÷"6ö÷&F–æF÷"ÒW&6†6T6ö÷&F–æF÷#°¢–b‡6W76–öâÓÒçVÆÂÇÂ6ö÷&F–æF÷"ÓÒçVÆÀ¢ÇÂ6W76–öâçFö¶Vâ‚’æWVÇ2††öÆFW"çFö¶Vâ‚’¢ÇÂ6W76–öâæöffW$–B‚’æWVÇ2††öÆFW"æöffW$–B‚’¢ÇÂ6W76–öâæ66WFVB‚’çG'”7V—&R‚’’°¢Æ–W"ç6VæDÖW76vR‚$g&öçF–W"W&6†6R6öæf—&ÖF–öâW‡—&VBâ"“°¢&WGW&ã°¢Ğ¢æf–vF–öå6W76–öç2ç&VÖ÷fR‡Æ–W"ævWEVæ—VT–B‚’Â6W76–öâ“°¢Æ–W"æ6Æ÷6T–çfVçF÷'’‚“°¢6ö÷&F–æF÷"çW&6†6R†æWrg&öçF–W%W&6†6T6ö÷&F–æF÷"å&WVW7B€¢&g&öçF–W"ÖwV’×6†÷¢"²6W76–öâçFö¶Vâ‚’À¢Æ–W"ævWEVæ—VT–B‚’À¢Æ–W"ævWEv÷&ÆB‚’ævWDæÖR‚’À¢6W76–öâæöffW$–B‚¢’’çv†Vä6ö×ÆWFR‚‡&W7VÇBÂf–ÇW&R’Óà¢6W'f–6W2çF6·2‚’æÖ–åF‡&VB‚‚’Óâ°¢Æ–W"öæÆ–æRÒÇVv–âævWE6W'fW"‚’ævWEÆ–W"€¢Æ–W"ævWEVæ—VT–B‚¢“°¢–b†öæÆ–æRÓÒçVÆÂÇÂöæÆ–æRæ—4öæÆ–æR‚’’°¢&WGW&ã°¢Ğ¢–b†f–ÇW&RÒçVÆÂ’°¢öæÆ–æRç6VæDÖW76vR‚$g&öçF–W"W&6†6R—2Væf–Æ&ÆRâ"“°¢&WGW&ã°¢Ğ¢6†÷uW&6†6U&W7VÇB†öæÆ–æRÂ&W7VÇB“°¢Ò¢“°¢Ğ ¢&—fFRfö–B6†÷uW&6†6U&W7VÇB€¢Æ–W"Æ–W"À¢g&öçF–W%W&6†6T6ö÷&F–æF÷"å&W7VÇB&W7VÇ@¢’°¢æf–vF–öä†öÆFW"†öÆFW"ÒæWræf–vF–öä†öÆFW"€¢æf–vF–öäÖöFRåU$4„4Uõ$U5TÅBÀ¢UT”Bç&æFöÕUT”B‚’À¢çVÆÀ¢“°¢–çfVçF÷'’–çfVçF÷'’Ò÷&ræ'V¶¶—Bä'V¶¶—Bæ7&VFT–çfVçF÷'’€¢†öÆFW"À¢#rÀ¢æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçBçFW‡B‚$g&öçF–W"W&6†6R&W7VÇB"¢“°¢†öÆFW"æ–çfVçF÷'’Ò–çfVçF÷'“°¢ÖFW&–ÂÖFW&–ÂÒ7v—F6‚‡&W7VÇBç7FGW2‚’’°¢66RDTÄ•dU$TBÓâÖFW&–ÂäÄ”ÔUô4ôä5$UDS°¢66RTäD”ärÓâÖFW&–Âå”TÄÄõuô4ôä5$UDS°¢66Rd”ÄTBÓâÖFW&–Âå$TEô4ôä5$UDS°¢66RTä´äõtâÓâÖFW&–Âäõ$ätUô4ôä5$UDS°¢Ó°¢–çfVçF÷'’ç6WD—FVÒƒ2ÂæÖVB€¢ÖFW&–ÂÀ¢&W7VÇBç7FGW2‚’ææÖR‚’À¢Æ—7Bæöb‡7v—F6‚‡&W7VÇBç7FGW2‚’’°¢66RDTÄ•dU$TBÓâ$—FVÒFVÆ—fW&VBâ#°¢66RTäD”ärÓà¢%–ÖVçB66WFVC²GW&&ÆRFVÆ—fW'’—2VæF–ærâ#°¢66Rd”ÄTBÓâ%–ÖVçBv2æ÷B6öÖÖ—GFVBâ#°¢66RTä´äõtâÓà¢%7FFR&WV—&W2FÖ–æ—7G&F÷"&V6öæ6–Æ–F–öââ#°¢Ò¢’“°¢Æ–W"æ÷Vä–çfVçF÷'’†–çfVçF÷'’“°¢Ğ ¢&—fFR7FF–2—FVÕ7F6²æÖVB€¢ÖFW&–ÂÖFW&–ÂÀ¢7G&–æræÖRÀ¢Æ—7CÅ7G&–æsâÆ÷&P¢’°¢—FVÕ7F6²—FVÒÒæWr—FVÕ7F6²†ÖFW&–Â“°¢—FVÔÖWFÖWFÒ—FVÒævWD—FVÔÖWF‚“°¢ÖWFæF—7Æ”æÖR†æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçBçFW‡B†æÖR’“°¢ÖWFæÆ÷&R†Æ÷&Rç7G&VÒ‚¢æÖ†æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçC£§FW‡B¢çFôÆ—7B‚’“°¢—FVÒç6WD—FVÔÖWF†ÖWF“°¢&WGW&â—FVÓ°¢Ğ ¢&—fFR7FF–2G&fW'6ÄÆöF÷WB&WÆ6TWF†÷&—G’€¢G&fW'6ÄÆöF÷WBÆöF÷WBÀ¢G&fW'6Ä–FVçF—G’–FVçF—G¢’°¢Æ—7CÅG&fW'6ÄÆöF÷WBäÆöv–6Ä—FVÓâ—FV×2Ğ¢æWr¦fçWF–Âä'&”Æ—7CÃâ†ÆöF÷WBçW&ÖæVçD—FV×2‚’“°¢—FV×2ç&VÖ÷fT–b†—FVÒÓâ—FVÒæ—FVÕG—R‚’ÓÒ–FVçF—G’æ—FVÕG—R‚’“°¢—FV×2æFB†æWrG&fW'6ÄÆöF÷WBäÆöv–6Ä—FVÒ€¢–FVçF—G’æ—FVÕG—R‚’À¢–FVçF—G’æ—FVÔ–ç7Fæ6T–B‚’À¢–FVçF—G’æ–ç7Fæ6TWö6‚‚’À¢G&fW'6ÄÆöF÷WBäÆöv–6Ä—FVÒå7FFRä5D•dP¢’“°¢&WGW&âæWrG&fW'6ÄÆöF÷WB€¢ÆöF÷WBçÆ–W%WV–B‚’À¢ÆöF÷WBçF†VÖT–B‚’À¢ÆöF÷WBæf—'7D¦ö–æVDB‚’À¢ÆöF÷WBæ–æ—F–ÄÆVæ6‡G4w&çFVB‚’À¢—FV×2À¢ÆöF÷WBæÆö6µfW'6–öâ‚¢“°¢Ğ ¢&—fFR7FF–2&ööÆVâ—5W&ÖæVçB…VæF–ætFVÆ—fW'’ä—FVÕG—RG—R’°¢&WGW&â7v—F6‚‡G—R’°¢66RTÅ•E$Âu$Ä”äuô„ôô²Âäd”tD”ôâÓâG'VS°¢66RÄTä4…BÂd•$Utõ$µõ$ô4´UBÓâfÇ6S°¢Ó°¢Ğ ¢&—fFR÷F–öæÃÄÆVæ6‡D—FVÔ6Æ–ÓâÆVæ6‡D6Æ–Ò„—FVÕ7F6²—FVÒ’°¢¦fçWF–ÂäÖÅ7G&–ærÂ7G&–æsâ&rÒæWr¦fçWF–Âä†6„ÖÃâ‚“°¢WB‡&rÂ&—FVÕ÷G—R"ÂFW‡B†—FVÒÂ•DTÕõE•R’“°¢WB‡&rÂ&—FVÕö–ç7Fæ6Uö–B"ÂFW‡B†—FVÒÂ•DTÕô”å5Dä4Uô”B’“°¢WB‡&rÂ&FVf–æ—F–öåö–B"ÂFW‡B†—FVÒÂDTd”ä•D”ôåô”B’“°¢Æöær66†VÖÒçVÖ&W"†—FVÒÂ44„TÔõdU%4”ôâ“°¢–b‡66†VÖÒÆöæräÔ”åõdÅTR’°¢&rçWB‚'66†VÖ÷fW'6–öâ"ÂÆöærçFõ7G&–ær‡66†VÖ’“°¢Ğ¢&WGW&âÆVæ6‡D—FVÔ6Æ–Òç'6R‡&r’æ6Æ–Ò‚“°¢Ğ ¢&—fFR7FF–2fö–BWB€¢¦fçWF–ÂäÖÅ7G&–ærÂ7G&–æsâfÇVW2À¢7G&–ær¶W’À¢7G&–ærfÇVP¢’°¢–b‡fÇVRÒçVÆÂ’°¢fÇVW2çWB†¶W’ÂfÇVR“°¢Ğ¢Ğ ¢&—fFR7FF–2UT”B'6UWV–B…7G&–ærfÇVR’°¢G'’°¢&WGW&âfÇVRÓÒçVÆÂòçVÆÂ¢UT”Bæg&öÕ7G&–ær‡fÇVR“°¢Ò6F6‚„–ÆÆVvÄ&wVÖVçDW†6WF–öâf–ÇW&R’°¢&WGW&âçVÆÃ°¢Ğ¢Ğ ¢&—fFRÆVæ6‡EÆ6VÖVçEöÆ–7’å6æ6†÷BÆ6VÖVçE6æ6†÷B€¢Æ–W"Æ–W"À¢÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²F&vWBÀ¢&ööÆVâF&vWD— ¢’°¢÷&ræ'V¶¶—Båv÷&ÆBv÷&ÆBÒF&vWBævWEv÷&ÆB‚“°¢÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²7W÷'BÒF&vWBævWE&VÆF—fR€¢÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6´f6RäDõtà¢“°¢&ööÆVâ&Vv–öäFVæ–VBÒfÇ6S°¢–b‡v÷&ÆDwV&BÒçVÆÂ’°¢G'’°¢&Vv–öäFVæ–VBÒv÷&ÆDwV&BæFVæ–VB€¢Æ–W"À¢F&vWBævWDÆö6F–öâ‚¢“°¢Ò6F6‚…'VçF–ÖTW†6WF–öâf–ÇW&R’°¢&Vv–öäFVæ–VBÒG'VS°¢Ğ¢Ğ¢&WGW&âæWrÆVæ6‡EÆ6VÖVçEöÆ–7’å6æ6†÷B€¢v÷&ÆBævWDæÖR‚’À¢v÷&ÆBæ—46‡Væ´ÆöFVB‡F&vWBævWE‚‚’ãâBÂF&vWBævWE¢‚’ãâB’À¢F&vWD—"À¢7W÷'BævWEG—R‚’æ—56öÆ–B‚’À¢F&vWBæ—4Æ—V–B‚’ÇÂ7W÷'Bæ—4Æ—V–B‚’À¢v÷&ÆBævWEv÷&ÆD&÷&FW"‚’æ—4–ç6–FR‡F&vWBævWDÆö6F–öâ‚’’À¢&÷FV7FVE7—7FVÔ&V‡F&vWB’À¢&Vv–öäFVæ–VBÀ¢fÇ6RÀ¢7F—fTÆVæ6‡G2æ6öçF–ç2†Æö6F–öâ‡F&vWB’¢“°¢Ğ ¢&—fFR7FF–2&ööÆVâ&÷FV7FVE7—7FVÔ&V€¢÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²F&vW@¢’°¢÷&ræ'V¶¶—BäÆö6F–öâ7vâÒF&vWBævWEv÷&ÆB‚’ævWE7väÆö6F–öâ‚“°¢F÷V&ÆRG‚ÒF&vWBævWE‚‚’²ãTBÒ7vâævWE‚‚“°¢F÷V&ÆRG¢ÒF&vWBævWE¢‚’²ãTBÒ7vâævWE¢‚“°¢–b†G‚¢G‚²G¢¢G¢ÃÒ#SdB¢#SdB’°¢&WGW&âG'VS°¢Ğ¢f÷"†–çB‚ÒÓC²‚ÃÒC²‚²²’°¢f÷"†–çB’ÒÓC²’ÃÒC²’²²’°¢f÷"†–çB¢ÒÓC²¢ÃÒC²¢²²’°¢ÖFW&–ÂÖFW&–ÂÒF&vWBævWE&VÆF—fR‡‚Â’Â¢’ævWEG—R‚“°¢–b†ÖFW&–ÂÓÒÖFW&–ÂääUD„U%õõ%DÀ¢ÇÂÖFW&–ÂÓÒÖFW&–ÂäTäEõõ%DÀ¢ÇÂÖFW&–ÂÓÒÖFW&–ÂäTäEôtDUt’’°¢&WGW&âG'VS°¢Ğ¢Ğ¢Ğ¢Ğ¢&WGW&âfÇ6S°¢Ğ ¢&—fFR7FF–27G&–ærFW‡B„—FVÕ7F6²—FVÒÂæÖW76VD¶W’¶W’’°¢–b†—FVÒÓÒçVÆÂÇÂ—FVÒæ†4—FVÔÖWF‚’’°¢&WGW&âçVÆÃ°¢Ğ¢&WGW&â—FVÒævWD—FVÔÖWF‚’ævWEW'6—7FVçDFF6öçF–æW"‚’ævWB€¢¶W’À¢W'6—7FVçDFFG—Rå5E$”äp¢“°¢Ğ ¢&—fFR7FF–2ÆöærçVÖ&W"„—FVÕ7F6²—FVÒÂæÖW76VD¶W’¶W’’°¢–b†—FVÒÓÒçVÆÂÇÂ—FVÒæ†4—FVÔÖWF‚’’°¢&WGW&âÆöæräÔ”åõdÅTS°¢Ğ¢W'6—7FVçDFF6öçF–æW"F2Ğ¢—FVÒævWD—FVÔÖWF‚’ævWEW'6—7FVçDFF6öçF–æW"‚“°¢ÆöærÆöæufÇVRÒF2ævWB†¶W’ÂW'6—7FVçDFFG—RäÄôär“°¢–b†ÆöæufÇVRÒçVÆÂ’°¢&WGW&âÆöæufÇVS°¢Ğ¢–çFVvW"–çFVvW"ÒF2ævWB†¶W’ÂW'6—7FVçDFFG—Rä”åDTtU"“°¢&WGW&â–çFVvW"ÓÒçVÆÂòÆöæräÔ”åõdÅTR¢–çFVvW"æÆöæufÇVR‚“°¢Ğ ¢&—fFR&ööÆVâ—5&÷FV7FVDÆVæ6‡DÖFW&–Â†÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²&Æö6²’°¢&WGW&â6öæf–ræW†7Ev÷&ÆDæÖR‚’æWVÇ2†&Æö6²ævWEv÷&ÆB‚’ævWDæÖR‚’¢bb‚‚ÆVæ6‡D–æFW…&VG’ævWB‚¢bb&Æö6²ævWEG—R‚¢ÓÒÖFW&–ÂçfÇVTöb†6öæf–ræÆVæ6‡B‚’æÖFW&–Â‚’’¢ÇÂ7F—fTÆVæ6‡G2æ6öçF–ç2†Æö6F–öâ†&Æö6²’’“°¢Ğ ¢&—fFR7FF–2ÆVæ6‡BäÆö6F–öâÆö6F–öâ†÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²&Æö6²’°¢&WGW&âæWrÆVæ6‡BäÆö6F–öâ€¢&Æö6²ævWEv÷&ÆB‚’ævWDæÖR‚’À¢&Æö6²ævWE‚‚’À¢&Æö6²ævWE’‚’À¢&Æö6²ævWE¢‚¢“°¢Ğ ¢&—fFRf–æÂ6Æ72ÆVæ6‡DvFWv¢–×ÆVÖVçG2ÆVæ6‡EW6T6ö÷&F–æF÷"äÆVæ6„vFWv’°¢÷fW'&–FP¢V&Æ–2&ööÆVâ6fUFôÆVæ6‚…UT”BÆ–W%WV–BÂÆVæ6‡BÆVæ6‡B’°¢–b‚66WF–ærævWB‚’’°¢&WGW&âfÇ6S°¢Ğ¢Æ–W"Æ–W"ÒÇVv–âævWE6W'fW"‚’ævWEÆ–W"‡Æ–W%WV–B“°¢–b‡Æ–W"ÓÒçVÆÂÇÂÆ–W"æ—4öæÆ–æR‚¢ÇÂ6öæf–ræW†7Ev÷&ÆDæÖR‚’æWVÇ2€¢Æ–W"ævWEv÷&ÆB‚’ævWDæÖR‚¢’’°¢&WGW&âfÇ6S°¢Ğ¢f"v÷&ÆBÒÆ–W"ævWEv÷&ÆB‚“°¢f"&Æö6²Òv÷&ÆBævWD&Æö6´B€¢ÆVæ6‡BæÆö6F–öâ‚’ç‚‚’À¢ÆVæ6‡BæÆö6F–öâ‚’ç’‚’À¢ÆVæ6‡BæÆö6F–öâ‚’ç¢‚¢“°¢&WGW&â&Æö6²ævWEG—R‚¢ÓÒÖFW&–ÂçfÇVTöb†6öæf–ræÆVæ6‡B‚’æÖFW&–Â‚’¢bb&Æö6²ævWE&VÆF—fRƒÂÂ’æ—576&ÆR‚¢bb&Æö6²ævWE&VÆF—fRƒÂ"Â’æ—576&ÆR‚“°¢Ğ ¢÷fW'&–FP¢V&Æ–2fö–BÆVæ6‚…UT”BÆ–W%WV–BÂÆVæ6‡BÆVæ6‡B’°¢–b‚66WF–ærævWB‚’’°¢&WGW&ã°¢Ğ¢Æ–W"Æ–W"Ò¦fçWF–Âäö&¦V7G2ç&WV—&TæöäçVÆÂ€¢ÇVv–âævWE6W'fW"‚’ævWEÆ–W"‡Æ–W%WV–B’À¢&öæÆ–æRÆ–W" ¢“°¢fV7F÷"F—&V7F–öâÒÆ–W"ævWDÆö6F–öâ‚’ævWDF—&V7F–öâ‚¢ç6WE’ƒ¢ææ÷&ÖÆ—¦R‚¢æ×VÇF—Ç’†6öæf–ræÆVæ6‡B‚’æ†÷&—¦öçFÅfVÆö6—G’‚’“°¢F—&V7F–öâç6WE’†6öæf–ræÆVæ6‡B‚’çfW'F–6ÅfVÆö6—G’‚’“°¢Æ–W"ç6WEfVÆö6—G’†F—&V7F–öâ“°¢–b†6öæf–ræÆVæ6‡B‚’æWFôFWÆ÷”VÇ—G&‚¢bbÆ–W"ævWD–çfVçF÷'’‚’ævWD6†W7GÆFR‚’ÒçVÆÀ¢bbÆ–W"ævWD–çfVçF÷'’‚’ævWD6†W7GÆFR‚’ævWEG—R‚¢ÓÒÖFW&–ÂäTÅ•E$’°¢Æ–W"ç6WDvÆ–F–ær‡G'VR“°¢Ğ¢Ğ ¢÷fW'&–FP¢V&Æ–2fö–BgFW%W'6—7FVB„ÆVæ6‡BÆVæ6‡B’°¢–b‚66WF–ærævWB‚¢ÇÂÆVæ6‡Bç7FFR‚’ÒÆVæ6‡Bå7FFRäU„„U5DTB’°¢&WGW&ã°¢Ğ¢÷&ræ'V¶¶—Båv÷&ÆBv÷&ÆBÒÇVv–âævWE6W'fW"‚’ævWEv÷&ÆB€¢ÆVæ6‡BæÆö6F–öâ‚’çv÷&ÆD–B‚¢“°¢–b‡v÷&ÆBÓÒçVÆÂ’°¢&WGW&ã°¢Ğ¢÷&ræ'V¶¶—Bæ&Æö6²ä&Æö6²&Æö6²Òv÷&ÆBævWD&Æö6´B€¢ÆVæ6‡BæÆö6F–öâ‚’ç‚‚’À¢ÆVæ6‡BæÆö6F–öâ‚’ç’‚’À¢ÆVæ6‡BæÆö6F–öâ‚’ç¢‚¢“°¢–b†&Æö6²ævWEG—R‚¢ÓÒÖFW&–ÂçfÇVTöb†6öæf–ræÆVæ6‡B‚’æÖFW&–Â‚’’’°¢&Æö6²ç6WEG—R„ÖFW&–Âä•"ÂfÇ6R“°¢Ğ¢7F—fTÆVæ6‡G2æFV7F—fFR€¢ÆVæ6‡BæÆö6F–öâ‚’À¢ÆVæ6‡BæÆVæ6‡D–B‚¢“°¢&V6÷&DÆVæ6‡B€¢ÆVæ6‡BÀ¢çVÆÂÀ¢$ÄTä4…EôU„„U5DTB"À¢%$TÔõdTB ¢’æW†6WF–öæÆÇ’†–væ÷&VBÓâçVÆÂ“°¢Ğ¢Ğ ¢V&Æ–2&V6÷&BÆVæ6‡D–ç7V7F–öâ€¢ÆVæ6‡Bå7FFR7FFRÀ¢&V6öæ6–ÆT6Æ76–f–6F–öâ6Æ76–f–6F–öà¢’·Ğ ¢V&Æ–2VçVÒ&V6öæ6–ÆT6Æ76–f–6F–öâ°¢D%ôäEô$Äô4µôÔD4‚À¢D%ôôäÅ’À¢$Äô4µôôäÅ’À¢4ôädÄ”5BÀ¢Tä´äõtâÀ¢äõEôdõTä@¢Ğ ¢V&Æ–2VçVÒFÖ–äÆVæ6‡E&W7VÇB°¢Ä”TBÀ¢äõô4„ätRÀ¢äõEôdõTäBÀ¢4ôädÄ”5BÀ¢ÔåTÅõ$Ud”Uuõ$UT•$TBÀ¢Täd”Ä$ÄP¢Ğ ¢V&Æ–2VçVÒFÖ–äÆöF÷WE&W7VÇB°¢Ä”TBÀ¢äõEôdõTäEôõ%ô4ôädÄ”5BÀ¢Täd”Ä$ÄP¢Ğ ¢&—fFR7FF–2f–æÂ6Æ72–ç7V7F–öä6GW&R°¢&—fFR&V6öæ6–ÆT6Æ76–f–6F–öâ6Æ76–f–6F–öâĞ¢&V6öæ6–ÆT6Æ76–f–6F–öâåTä´äõtã°¢Ğ ¢&—fFR&V6÷&B&VÖ÷fÄ6GW&R€¢ÆVæ6‡BÆVæ6‡BÀ¢&ööÆVâ&VÖ÷fV@¢’·Ğ ¢&—fFR&V6÷&BÆ6VÖVçE&WVW7B€¢UT”BÆ–W%WV–BÀ¢ÆVæ6‡BäÆö6F–öâÆö6F–öâÀ¢WV—ÖVçE6Æ÷B†æBÀ¢ÆVæ6‡D—FVÔ6Æ–Ò6Æ–ÒÀ¢UT”BFVÆ—fW'”–BÀ¢ÆVæ6‡BÆVæ6‡@¢’·Ğ ¢&—fFRVçVÒæf–vF–öäÖöFR°¢Ô”âÀ¢4„õÀ¢ÄôDõUBÀ¢U$4„4Uô4ôäd•$ÒÀ¢U$4„4Uõ$U5TÅ@¢Ğ ¢&—fFR7FF–2f–æÂ6Æ72æf–vF–öä†öÆFW"–×ÆVÖVçG2–çfVçF÷'”†öÆFW"°¢&—fFRf–æÂæf–vF–öäÖöFRÖöFS°¢&—fFRf–æÂUT”BFö¶Vã°¢&—fFRf–æÂ7G&–æröffW$–C°¢&—fFR–çfVçF÷'’–çfVçF÷'“° ¢&—fFRæf–vF–öä†öÆFW"€¢æf–vF–öäÖöFRÖöFRÀ¢UT”BFö¶VâÀ¢7G&–æröffW$–@¢’°¢F†—2æÖöFRÒÖöFS°¢F†—2çFö¶VâÒFö¶Vã°¢F†—2æöffW$–BÒöffW$–C°¢Ğ ¢&—fFRæf–vF–öäÖöFRÖöFR‚’°¢&WGW&âÖöFS°¢Ğ ¢&—fFRUT”BFö¶Vâ‚’°¢&WGW&âFö¶Vã°¢Ğ ¢&—fFR7G&–æröffW$–B‚’°¢&WGW&âöffW$–C°¢Ğ ¢÷fW'&–FP¢V&Æ–2–çfVçF÷'’vWD–çfVçF÷'’‚’°¢&WGW&â–çfVçF÷'“°¢Ğ¢Ğ ¢&—fFR&V6÷&Bæf–vF–öå6W76–öâ€¢UT”BFö¶VâÀ¢7G&–æröffW$–BÀ¢6–ævÆUW6TvFR66WFV@¢’·Ğ ¢&—fFR7FF–2f–æÂ6Æ72&WG'”6GW&R°¢&—fFR7G&–ærv÷&ÆDæÖS°¢&—fFRFVÆ—fW'•&WG'”6Æ76–f–6F–öâä¶–æB¶–æBĞ¢FVÆ—fW'•&WG'”6Æ76–f–6F–öâä¶–æBå4…UDDõtåõTäd”Ä$ÄS°¢Ğ§Ğ