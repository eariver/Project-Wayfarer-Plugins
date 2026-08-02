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
        heldAuthorizations.remove(playerId);
        checkpoints.checkpoint(playerId).whenComplete((ignored, failure) ->
            sessions.close(playerId)
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
            && !authorization(player.getUniqueId()).allowsBlockBreak()) {
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
        if (tool == null
            || authorization(player.getUniqueId()).state()
                != HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER
            || !authorization(player.getUniqueId()).allowsProgress()) {
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
            && authorization(player.getUniqueId()).state()
                != HeldGrowthToolAuthorization.State.VALID_ACTIVE_OWNER) {
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
        scheduleAuthorization(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        scheduleAuthorization(player);
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
            handleGuiClick(player, event.getRawSlot(), ×]üîÚ$z{-®éÜj×’’°¢öæÆ–æRç6VæDÖW76vR€¢f–ÇW&RÓÒçVÆÀ¢ò$w&÷wF‚FööÂ&W—#¢"²&W7VÇBç7FGW2‚¢¢$w&÷wF‚FööÂ&W—"—2Væf–Æ&ÆRâ ¢“°¢Ğ¢Ò¢“°¢Ğ ¢&—fFR7FF–2—FVÕ7F6²æÖVB€¢ÖFW&–ÂÖFW&–ÂÀ¢7G&–æræÖRÀ¢Æ—7CÅ7G&–æsâÆ÷&P¢’°¢—FVÕ7F6²—FVÒÒæWr—FVÕ7F6²†ÖFW&–Â“°¢—FVÔÖWFÖWFÒ—FVÒævWD—FVÔÖWF‚“°¢ÖWFæF—7Æ”æÖR†æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçBçFW‡B†æÖR’“°¢ÖWFæÆ÷&R†Æ÷&Rç7G&VÒ‚¢æÖ†æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçC£§FW‡B¢çFôÆ—7B‚’“°¢—FVÒç6WD—FVÔÖWF†ÖWF“°¢&WGW&â—FVÓ°¢Ğ ¢&—fFR6ö×ÆWF–öå7FvSÅfö–Câ&V6÷&DFÖ–â€¢w&÷wF…FööÂFööÂÀ¢7G&–ærWfVçEG—P¢’°¢&WGW&â6W'f–6W2æVF—B‚’ç&V6÷&B†æWrv–f&W$VF—BäVF—DWfVçB€¢UT”Bç&æFöÕUT”B‚’À¢WfVçEG—RÀ¢FööÂæ÷væW%WV–B‚’À¢$u$õuD…õDôôÂ"À¢FööÂçFööÄ–B‚’çFõ7G&–ær‚’À¢6W'f–6W2ç6W'fW$–B‚’À¢'µÂ'&W7VÇEÂ#¥Â$Ä”TEÂ'Ò"À¢6Æö6²æ–ç7FçB‚¢’“°¢Ğ ¢&—fFRw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRFVÆ—fW"„w&÷wF…FööÂFööÂ’°¢–b‚66WF–ær’°¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRåTäd”Ä$ÄS°¢Ğ¢Æ–W"Æ–W"ÒÇVv–âævWE6W'fW"‚’ævWEÆ–W"‡FööÂæ÷væW%WV–B‚’“°¢–b‡Æ–W"ÓÒçVÆÂÇÂÆ–W"æ—4öæÆ–æR‚’’°¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRåÄ”U%ôôddÄ”äS°¢Ğ¢–b†6÷VçD7W'&VçB‡Æ–W"ÂFööÂæ÷væW%WV–B‚’ÂFööÂ’â’°¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRäÅ$TE•õ$U4TåC°¢Ğ¢–b‡Æ–W"ævWD–çfVçF÷'’‚’æf—'7DV×G’‚’Â’°¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRä”ådTåDõ%•ôeTÄÃ°¢Ğ¢—FVÕ7F6²—FVÓ°¢G'’°¢—FVÒÒæWr—FVÕ7F6²„ÖFW&–ÂåtôôDTåõ”4´„R“°¢w&—FT–FVçF—G’†—FVÒÂFööÂÂ$u$õuD…õDôôÂ"“°¢Ç”WföÇWF–öâ†—FVÒÂFööÂÂfÇ6R“°¢Ò6F6‚…'VçF–ÖTW†6WF–öâf–ÇW&R’°¢F‡&÷ræWrw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"äFVÆ—fW'•7FvTW†6WF–öâ€¢w&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"äF–væ÷7F–57FvRä5$TDUôäEôääõDDUô•DTÒÀ¢f–ÇW&P¢“°¢Ğ¢G'’°¢–b‚Æ–W"ævWD–çfVçF÷'’‚’æFD—FVÒ†—FVÒ’æ—4V×G’‚’’°¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRä”ådTåDõ%•ôeTÄÃ°¢Ğ¢Ò6F6‚…'VçF–ÖTW†6WF–öâf–ÇW&R’°¢F‡&÷ræWrw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"äFVÆ—fW'•7FvTW†6WF–öâ€¢w&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"äF–væ÷7F–57FvRä”å4U%Eõ…•4”4Åô•DTÒÀ¢f–ÇW&P¢“°¢Ğ¢&WGW&âw&÷wF…FööÄFVÆ—fW'”6ö÷&F–æF÷"ä÷WF6öÖRäDTÄ•dU$TC°¢Ğ ¢7W&W75v&æ–æw2‚&FW&V6F–öâ"¢&—fFRfö–BÇ”WföÇWF–öâ„—FVÕ7F6²—FVÒÂw&÷wF…FööÂFööÂ’°¢Ç”WföÇWF–öâ†—FVÒÂFööÂÂfÇ6R“°¢Ğ ¢7W&W75v&æ–æw2‚&FW&V6F–öâ"¢&—fFRfö–BÇ”WföÇWF–öâ€¢—FVÕ7F6²—FVÒÀ¢w&÷wF…FööÂFööÂÀ¢&ööÆVâWföÇWF–öä6÷VçD–æ7&V6V@¢’°¢WföÇWF–öåÆâäWföÇWF–öå6æ6†÷B6æ6†÷BÒ6öæf–ræWföÇWF–öåÆâ‚’æWfÇVFR€¢FööÂæ7V×VÆF—fU&öw&W75Væ—G2‚’À¢FööÂæ'&æ6‚‚’À¢6öæf–ræVæ6†çFÖVçD62‚¢“°¢ÖFW&–ÂW‡V7FVDÖFW&–ÂÒ7v—F6‚‡6æ6†÷BæÖFW&–Â‚’’°¢66RtôôBÓâÖFW&–ÂåtôôDTåõ”4´„S°¢66R5DôäRÓâÖFW&–Âå5DôäUõ”4´„S°¢66R•$ôâÓâÖFW&–Âä•$ôåõ”4´„S°¢66RD”ÔôäBÓâÖFW&–ÂäD”ÔôäEõ”4´„S°¢Ó°¢–çBöÆDÖ†–×VÒÒÖF‚æÖ‚ƒÂ—FVÒævWEG—R‚’ævWDÖ„GW&&–Æ—G’‚’“°¢–çBöÆDFÖvRÒ—FVÒævWD—FVÔÖWF‚’–ç7Fæ6VöbFÖvV&ÆRFÖvV&ÆP¢òFÖvV&ÆRævWDFÖvR‚¢¢°¢&ööÆVâÖFW&–Ä6†ævVBÒ—FVÒævWEG—R‚’ÒW‡V7FVDÖFW&–Ã°¢&ööÆVâVæ6†çFÖVçG46†ævVBĞ¢—FVÒævWDVæ6†çFÖVçDÆWfVÂ„Væ6†çFÖVçBäTdd”4”Tä5’¢Ò6æ6†÷BæVff–6–Væ7’‚¢ÇÂ—FVÒævWDVæ6†çFÖVçDÆWfVÂ„Væ6†çFÖVçBåTä%$T´”är¢Ò6æ6†÷BçVæ'&V¶–ær‚¢ÇÂ—FVÒævWDVæ6†çFÖVçDÆWfVÂ„Væ6†çFÖVçBädõ%ETäR¢Ò6æ6†÷Bæf÷'GVæR‚¢ÇÂ—FVÒævWDVæ6†çFÖVçDÆWfVÂ„Væ6†çFÖVçBå4”ÄµõDõT4‚¢Ò6æ6†÷Bç6–ÆµF÷V6‚‚“°¢&ööÆVâ&W6VçFF–öä6†ævVBÒ†4W‡V7FVDF—7Æ”æÖR€¢—FVÒÀ¢w&÷wF…FööÄFVÆ—fW'•&W6VçFF–öâæF—7Æ”æÖR€¢w&÷wF…FööÅ‡—6–6Ä6Æ–Òä—FVÕG—Räu$õuD…õDôôÀ¢¢“°¢–b‚ÖFW&–Ä6†ævVBbbVæ6†çFÖVçG46†ævV@¢bbWföÇWF–öä6÷VçD–æ7&V6V@¢bb&W6VçFF–öä6†ævV@¢bb6æöæ–6Ä–FVçF—G’†—FVÒÂFööÂ’’°¢&WGW&ã°¢Ğ¢–b†ÖFW&–Ä6†ævVB’°¢—FVÒç6WEG—R†W‡V7FVDÖFW&–Â“°¢Ğ¢—FVÒç&VÖ÷fTVæ6†çFÖVçB„Væ6†çFÖVçBäTdd”4”Tä5’“°¢—FVÒç&VÖ÷fTVæ6†çFÖVçB„Væ6†çFÖVçBåTä%$T´”är“°¢—FVÒç&VÖ÷fTVæ6†çFÖVçB„Væ6†çFÖVçBädõ%ETäR“°¢—FVÒç&VÖ÷fTVæ6†çFÖVçB„Væ6†çFÖVçBå4”ÄµõDõT4‚“°¢FB†—FVÒÂVæ6†çFÖVçBäTdd”4”Tä5’Â6æ6†÷BæVff–6–Væ7’‚’“°¢FB†—FVÒÂVæ6†çFÖVçBåTä%$T´”ärÂ6æ6†÷BçVæ'&V¶–ær‚’“°¢FB†—FVÒÂVæ6†çFÖVçBädõ%ETäRÂ6æ6†÷Bæf÷'GVæR‚’“°¢FB†—FVÒÂVæ6†çFÖVçBå4”ÄµõDõT4‚Â6æ6†÷Bç6–ÆµF÷V6‚‚’“°¢–b†—FVÒævWD—FVÔÖWF‚’–ç7Fæ6VöbFÖvV&ÆRFÖvV&ÆR’°¢–çBÖ†–×VÒÒÖF‚æÖ‚ƒÂ—FVÒævWEG—R‚’ævWDÖ„GW&&–Æ—G’‚’“°¢–çBFÖvS°¢–b†WföÇWF–öä6÷VçD–æ7&V6VB’°¢FÖvRÒGW&&–Æ—G•6VÖçF–72ægFW$WföÇWF–öâ€¢öÆDFÖvRÀ¢G'VP¢“°¢ÒVÇ6R–b†ÖFW&–Ä6†ævVBbböÆDFÖvRÂöÆDÖ†–×VÒ’°¢FÖvRÒGW&&–Æ—G•6VÖçF–72ç&V6öæ6–ÆT7F—fR€¢öÆDÖ†–×VÒÀ¢öÆDFÖvRÀ¢Ö†–×VĞ¢“°¢ÒVÇ6R°¢FÖvRÒÖF‚æÖ–â†öÆDFÖvRÂÖ†–×VÒÒ“°¢Ğ¢FÖvV&ÆRç6WDFÖvR†FÖvR“°¢—FVÒç6WD—FVÔÖWF†FÖvV&ÆR“°¢Ğ¢w&—FT–FVçF—G’†—FVÒÂFööÂÂ$u$õuD…õDôôÂ"“°¢Ğ ¢&—fFR7FF–2&ööÆVâ6æöæ–6Ä–FVçF—G’„—FVÕ7F6²—FVÒÂw&÷wF…FööÂFööÂ’°¢w&÷wF…FööÅ‡—6–6Ä6Æ–Ò'6VBÒ6Æ–Ò†—FVÒ’æ÷$VÇ6R†çVÆÂ“°¢&WGW&â'6VBÒçVÆÀ¢bb'6VBçfÆ–FFR‡FööÂæ÷væW%WV–B‚’ÂFööÂ¢ÓÒw&÷wF…FööÅ‡—6–6Ä6Æ–ÒåfÆ–FF–öâådÄ”C°¢Ğ ¢&—fFR7FF–2&ööÆVâ7W'&VçE‡—6–6Â€¢—FVÕ7F6²—FVÒÀ¢UT”B÷væW%WV–BÀ¢w&÷wF…FööÂWF†÷&—G¢’°¢&WGW&â—FVÒÒçVÆÀ¢bb6æöæ–6Ä–FVçF—G’†—FVÒÂWF†÷&—G’¢bbWF†÷&—G’æ÷væW%WV–B‚’æWVÇ2†÷væW%WV–B“°¢Ğ ¢&—fFR7FF–2–çB6÷VçD7W'&VçB€¢Æ–W"Æ–W"À¢UT”B÷væW%WV–BÀ¢w&÷wF…FööÂWF†÷&—G¢’°¢–çB6÷VçBÒ°¢f÷"„—FVÕ7F6²—FVÒ¢Æ–W"ævWD–çfVçF÷'’‚’ævWE7F÷&vT6öçFVçG2‚’’°¢–b†7W'&VçE‡—6–6Â†—FVÒÂ÷væW%WV–BÂWF†÷&—G’’’°¢6÷VçB²³°¢Ğ¢Ğ¢f÷"„—FVÕ7F6²—FVÒ¢Æ–W"ævWD–çfVçF÷'’‚’ævWD&Ö÷$6öçFVçG2‚’’°¢–b†7W'&VçE‡—6–6Â†—FVÒÂ÷væW%WV–BÂWF†÷&—G’’’°¢6÷VçB²³°¢Ğ¢Ğ¢–b†7W'&VçE‡—6–6Â€¢Æ–W"ævWD–çfVçF÷'’‚’ævWD—FVÔ–äöfd†æB‚’À¢÷væW%WV–BÀ¢WF†÷&—G¢’’°¢6÷VçB²³°¢Ğ¢–b†7W'&VçE‡—6–6Â€¢Æ–W"ævWD÷Vä–çfVçF÷'’‚’ævWD7W'6÷"‚’À¢÷væW%WV–BÀ¢WF†÷&—G¢’’°¢6÷VçB²³°¢Ğ¢&WGW&â6÷VçC°¢Ğ ¢&—fFR7FF–2fö–BFB„—FVÕ7F6²—FVÒÂVæ6†çFÖVçBVæ6†çFÖVçBÂ–çBÆWfVÂ’°¢–b†ÆWfVÂâ’°¢—FVÒæFEVç6fTVæ6†çFÖVçB†Væ6†çFÖVçBÂÆWfVÂ“°¢Ğ¢Ğ ¢&—fFR7FF–2&ööÆVâ6öçF–ç5v–f&W%FööÂ„—FVÕ7F6µµÒ6öçFVçG2’°¢f÷"„—FVÕ7F6²—FVÒ¢6öçFVçG2’°¢–b‡v–f&W%FööÂ†—FVÒ’’°¢&WGW&âG'VS°¢Ğ¢Ğ¢&WGW&âfÇ6S°¢Ğ ¢&—fFR7FF–2&ööÆVâv–f&W%FööÂ„—FVÕ7F6²—FVÒ’°¢–b†—FVÒÓÒçVÆÂ’°¢&WGW&âfÇ6S°¢Ğ¢&WGW&â—4ÖævVDFVF…G—R‡FW‡B†—FVÒÂ•DTÕõE•R’“°¢Ğ ¢7FF–2&ööÆVâ—4ÖævVDFVF…G—R…7G&–ær—FVÕG—R’°¢&WGW&â$u$õuD…õDôôÂ"æWVÇ2†—FVÕG—R¢ÇÂ$%$ô´Tåôu$õuD…õDôôÂ"æWVÇ2†—FVÕG—R“°¢Ğ ¢&—fFR7FF–2fö–Bw&—FT–FVçF—G’€¢—FVÕ7F6²—FVÒÀ¢w&÷wF…FööÂFööÂÀ¢7G&–ær—FVÕG—P¢’°¢w&—FT–FVçF—G’€¢—FVÒÀ¢&V—77VTFVÆ—fW'•öÆ–7’æ–FVçF—G’‡FööÂ’À¢FööÂÀ¢—FVÕG—P¢“°¢Ğ ¢&—fFR7FF–2fö–Bw&—FT–FVçF—G’€¢—FVÕ7F6²—FVÒÀ¢&V—77VTFVÆ—fW'•öÆ–7’å&V—77VTFVÆ—fW'”–FVçF—G’–FVçF—G’À¢w&÷wF…FööÂFööÂÀ¢7G&–ær—FVÕG—P¢’°¢—FVÔÖWFÖWFÒ—FVÒævWD—FVÔÖWF‚“°¢W'6—7FVçDFF6öçF–æW"F2ÒÖWFævWEW'6—7FVçDFF6öçF–æW"‚“°¢F2ç6WB„•DTÕõE•RÂW'6—7FVçDFFG—Rå5E$”ärÂ—FVÕG—R“°¢F2ç6WB€¢•DTÕô”å5Dä4Uô”BÀ¢W'6—7FVçDFFG—Rå5E$”ärÀ¢–FVçF—G’æ—FVÔ–ç7Fæ6T–B‚’çFõ7G&–ær‚¢“°¢F2ç6WB…DôôÅô”BÂW'6—7FVçDFFG—Rå5E$”ärÂFööÂçFööÄ–B‚’çFõ7G&–ær‚’“°¢F2ç6WB„õtäU%ô”BÂW'6—7FVçDFFG—Rå5E$”ärÂFööÂæ÷væW%WV–B‚’çFõ7G&–ær‚’“°¢F2ç6WB…DôôÅõE•RÂW'6—7FVçDFFG—Rå5E$”ärÂw&÷wF…FööÂåDôôÅõE•R“°¢F2ç6WB„Uô4‚ÂW'6—7FVçDFFG—RäÄôärÂ–FVçF—G’æ–ç7Fæ6TWö6‚‚’“°¢F2ç6WB…44„TÔÂW'6—7FVçDFFG—Rä”åDTtU"ÂFööÂç66†VÖfW'6–öâ‚’“°¢F2ç6WB…$Ud•4”ôâÂW'6—7FVçDFFG—RäÄôärÂFööÂæF—7Æ•&Wf—6–öâ‚’“°¢w&÷wF…FööÅ‡—6–6Ä6Æ–Òä—FVÕG—R&W6VçFF–öåG—RĞ¢$%$ô´Tåôu$õuD…õDôôÂ"æWVÇ2†—FVÕG—R¢òw&÷wF…FööÅ‡—6–6Ä6Æ–Òä—FVÕG—Rä%$ô´Tåôu$õuD…õDôôÀ¢¢$u$õuD…õDôôÂ"æWVÇ2†—FVÕG—R¢òw&÷wF…FööÅ‡—6–6Ä6Æ–Òä—FVÕG—Räu$õuD…õDôôÀ¢¢çVÆÃ°¢–b‡&W6VçFF–öåG—RÒçVÆÂ’°¢ÖWFæF—7Æ”æÖR€¢w&÷wF…FööÄFVÆ—fW'•&W6VçFF–öâæF—7Æ”æÖR‡&W6VçFF–öåG—R¢“°¢Ğ¢—FVÒç6WD—FVÔÖWF†ÖWF“°¢Ğ ¢&—fFR7FF–2÷F–öæÃÄw&÷wF…FööÅ‡—6–6Ä6Æ–Óâ6Æ–Ò„—FVÕ7F6²—FVÒ’°¢&WGW&â6Æ–Õ&W7VÇB†—FVÒ’æ6Æ–Ò‚“°¢Ğ ¢&—fFR7FF–2w&÷wF…FööÅ‡—6–6Ä6Æ–Òå'6U&W7VÇB6Æ–Õ&W7VÇB€¢—FVÕ7F6²—FVĞ¢’°¢¦fçWF–ÂäÖÅ7G&–ærÂ7G&–æsâ&rÒæWr¦fçWF–Âä†6„ÖÃâ‚“°¢WB‡&rÂ&—FVÕ÷G—R"ÂFW‡B†—FVÒÂ•DTÕõE•R’“°¢WB‡&rÂ&—FVÕö–ç7Fæ6Uö–B"ÂFW‡B†—FVÒÂ•DTÕô”å5Dä4Uô”B’“°¢WB‡&rÂ'FööÅö–B"ÂFW‡B†—FVÒÂDôôÅô”B’“°¢WB‡&rÂ&÷væW%÷WV–B"ÂFW‡B†—FVÒÂõtäU%ô”B’“°¢WB‡&rÂ'FööÅ÷G—R"ÂFW‡B†—FVÒÂDôôÅõE•R’“°¢WB‡&rÂ&–ç7Fæ6UöWö6‚"ÂçVÖW&–5FW‡B†—FVÒÂUô4‚’“°¢WB‡&rÂ'66†VÖ÷fW'6–öâ"ÂçVÖW&–5FW‡B†—FVÒÂ44„TÔ’“°¢WB‡&rÂ&F—7Æ•÷&Wf—6–öâ"ÂçVÖW&–5FW‡B†—FVÒÂ$Ud•4”ôâ’“°¢&WGW&âw&÷wF…FööÅ‡—6–6Ä6Æ–Òç'6R‡&r“°¢Ğ ¢&—fFR7FF–2&ööÆVâ†4W‡V7FVDF—7Æ”æÖR€¢—FVÕ7F6²—FVÒÀ¢æWBæ·–÷&’æGfVçGW&RçFW‡Bä6ö×öæVçBW‡V7FV@¢’°¢&WGW&âW‡V7FVBÒçVÆÀ¢bb—FVÒÒçVÆÀ¢bb—FVÒæ†4—FVÔÖWF‚¢bb¦fçWF–Âäö&¦V7G2æWVÇ2†—FVÒævWD—FVÔÖWF‚’æF—7Æ”æÖR‚’ÂW‡V7FVB“°¢Ğ ¢&—fFR7FF–2fö–BWB€¢¦fçWF–ÂäÖÅ7G&–ærÂ7G&–æsâfÇVW2À¢7G&–ær¶W’À¢7G&–ærfÇVP¢’°¢–b‡fÇVRÒçVÆÂ’°¢fÇVW2çWB†¶W’ÂfÇVR“°¢Ğ¢Ğ ¢&—fFR7FF–27G&–ærçVÖW&–5FW‡B„—FVÕ7F6²—FVÒÂæÖW76VD¶W’¶W’’°¢ÆöærfÇVRÒçVÖ&W"†—FVÒÂ¶W’“°¢&WGW&âfÇVRÓÒÆöæräÔ”åõdÅTRòçVÆÂ¢ÆöærçFõ7G&–ær‡fÇVR“°¢Ğ ¢&—fFR7FF–27G&–ærFW‡B„—FVÕ7F6²—FVÒÂæÖW76VD¶W’¶W’’°¢–b†—FVÒÓÒçVÆÂÇÂ—FVÒæ†4—FVÔÖWF‚’’°¢&WGW&âçVÆÃ°¢Ğ¢&WGW&â—FVÒævWD—FVÔÖWF‚’ævWEW'6—7FVçDFF6öçF–æW"‚’ævWB€¢¶W’À¢W'6—7FVçDFFG—Rå5E$”äp¢“°¢Ğ ¢&—fFR7FF–2ÆöærçVÖ&W"„—FVÕ7F6²—FVÒÂæÖW76VD¶W’¶W’’°¢–b†—FVÒÓÒçVÆÂÇÂ—FVÒæ†4—FVÔÖWF‚’’°¢&WGW&âÆöæräÔ”åõdÅTS°¢Ğ¢W'6—7FVçDFF6öçF–æW"F2Ğ¢—FVÒævWD—FVÔÖWF‚’ævWEW'6—7FVçDFF6öçF–æW"‚“°¢&WGW&âçVÖ&W"‡F2Â¶W’“°¢Ğ ¢7FF–2ÆöærçVÖ&W"…W'6—7FVçDFF6öçF–æW"F2ÂæÖW76VD¶W’¶W’’°¢–b‡F2æ†2†¶W’ÂW'6—7FVçDFFG—RäÄôär’’°¢ÆöærÆöæufÇVRÒF2ævWB†¶W’ÂW'6—7FVçDFFG—RäÄôär“°¢–b†ÆöæufÇVRÒçVÆÂ’°¢&WGW&âÆöæufÇVS°¢Ğ¢Ğ¢–b‡F2æ†2†¶W’ÂW'6—7FVçDFFG—Rä”åDTtU"’’°¢–çFVvW"–çFVvW"ÒF2ævWB†¶W’ÂW'6—7FVçDFFG—Rä”åDTtU"“°¢–b†–çFVvW"ÒçVÆÂ’°¢&WGW&â–çFVvW"æÆöæufÇVR‚“°¢Ğ¢Ğ¢&WGW&âÆöæräÔ”åõdÅTS°¢Ğ ¢&—fFR7FF–27G&–ær÷&Tw&÷W„ÖFW&–ÂÖFW&–Â’°¢7G&–æræÖRÒÖFW&–ÂææÖR‚’çFõWW$66R„Æö6ÆRå$ôõB“°¢–b†æÖRæWVÇ2‚$äUD„U%õT%E¥ôõ$R"’’°¢&WGW&â$äUD„U%õT%E¢#°¢Ğ¢–b†æÖRæWVÇ2‚$äUD„U%ôtôÄEôõ$R"’’°¢&WGW&â$äUD„U%ôtôÄB#°¢Ğ¢–b†æÖRæWVÇ2‚$ä4”TåEôDT%$•2"’’°¢&WGW&â$ä4”TåEôDT%$•2#°¢Ğ¢7G&–ær6–×Æ–f–VBÒæÖP¢ç&WÆ6R‚$DTU4ÄDUò"Â""¢ç&WÆ6R‚%ôõ$R"Â""“°¢&WGW&â7v—F6‚‡6–×Æ–f–VB’°¢66R$4ôÂ"Â$4õU""Â%$TE5DôäR"Â$•$ôâ"Â$Ä•2"Â$tôÄB"À¢$D”ÔôäB"Â$TÔU$ÄB"Óâ6–×Æ–f–VC°¢FVfVÇBÓâçVÆÃ°¢Ó°¢Ğ ¢&—fFRfö–B6†V6·ö–çDÆÂ‚’°¢f÷"…UT”BÆ–W$–B¢6W76–öç2æ÷væW%WV–G2‚’’°¢6†V6·ö–çG2æ6†V6·ö–çB‡Æ–W$–B“°¢Ğ¢Ğ ¢V&Æ–2&V6÷&B&W—%6æ6†÷B†–çBFÖvRÂ–çBÖ†–×VÔGW&&–Æ—G’’·Ğ ¢V&Æ–2VçVÒFÖ–ä×WFF–öâ°¢Ä”TBÀ¢äõô4„ätRÀ¢äõEôdõTäBÀ¢4ôädÄ”5BÀ¢Täd”Ä$ÄP¢Ğ ¢&—fFR&V6÷&BWF†÷&—G”×WFF–öâ€¢w&÷wF…FööÂFööÂÀ¢FÖ–ä×WFF–öâ&W7VÇ@¢’·Ğ ¢&—fFRVçVÒwV”ÖöFR°¢Ô”âÀ¢$U•%õ$Ud”Up¢Ğ ¢&—fFR7FF–2f–æÂ6Æ72w&÷wF…FööÄwV”†öÆFW ¢–×ÆVÖVçG2–çfVçF÷'”†öÆFW"°¢&—fFRf–æÂwV”ÖöFRÖöFS°¢&—fFRf–æÂUT”BFö¶Vã°¢&—fFR–çfVçF÷'’–çfVçF÷'“° ¢&—fFRw&÷wF…FööÄwV”†öÆFW"„wV”ÖöFRÖöFRÂUT”BFö¶Vâ’°¢F†—2æÖöFRÒÖöFS°¢F†—2çFö¶VâÒFö¶Vã°¢Ğ ¢&—fFRwV”ÖöFRÖöFR‚’°¢&WGW&âÖöFS°¢Ğ ¢&—fFRUT”BFö¶Vâ‚’°¢&WGW&âFö¶Vã°¢Ğ ¢÷fW'&–FP¢V&Æ–2–çfVçF÷'’vWD–çfVçF÷'’‚’°¢&WGW&â–çfVçF÷'“°¢Ğ¢Ğ ¢&—fFR&V6÷&B&W—$wV•6W76–öâ€¢UT”BFö¶VâÀ¢UT”BFööÄ–BÀ¢UT”B—FVÔ–ç7Fæ6T–BÀ¢Æöær–ç7Fæ6TWö6‚À¢ÆöærF—7Æ•&Wf—6–öâÀ¢–çBFÖvRÀ¢–çBÖ†–×VÔGW&&–Æ—G’À¢ÆöærÖ÷VçEv–Ö&²À¢6–ævÆUW6TvFR66WFV@¢’·Ğ§Ğ 