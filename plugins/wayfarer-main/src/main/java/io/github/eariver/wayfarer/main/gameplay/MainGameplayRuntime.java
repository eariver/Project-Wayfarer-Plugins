package io.github.eariver.wayfarer.main.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerAudit;
import io.github.eariver.wayfarer.main.application.GrowthCheckpointService;
import io.github.eariver.wayfarer.main.application.GrowthSessionStore;
import io.github.eariver.wayfarer.main.application.GrowthToolDeliveryCoordinator;
import io.github.eariver.wayfarer.main.application.GrowthToolRepository;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import io.github.eariver.wayfarer.main.domain.EvolutionPlan;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
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

public final class MainGameplayRuntime implements Listener, AutoCloseable {
    private static final NamespacedKey ITEM_TYPE =
        new NamespacedKey("wayfarer", "item_type");
    private static final NamespacedKey TOOL_ID =
        new NamespacedKey("wayfarer", "tool_id");
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
            GrowthTool updated = sessions.addProgress(
                player.getUniqueId(),
                units,
                clock.instant()
            );
            applyEvolution(item, updated);
        } catch (RuntimeException failure) {
            plugin.getLogger().severe("Growth progress update failed closed.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation")
    public void onDamage(PlayerItemDamageEvent event) {
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
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        boolean carriesTool = wayfarerTool(current) || wayfarerTool(cursor);
        InventoryType type = event.getView().getTopInventory().getType();
        boolean targetsRestrictedInventory =
            event.getClickedInventory() == event.getView().getTopInventory()
                || (event.isShiftClick() && wayfarerTool(current));
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
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            || event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) {
            return;
        }
        GrowthTool tool = sessions.current(event.getPlayer().getUniqueId()).orElse(null);
        ItemStack held = event.getPlayer().getInventory().getItemInMainHand();
        if (tool == null || (!canonical(held, event.getPlayer().getUniqueId(), tool)
            && !"BROKEN_GROWTH_TOOL".equals(text(held, ITEM_TYPE)))) {
            return;
        }
        event.setCancelled(true);
        var inventory = org.bukkit.Bukkit.createInventory(
            null,
            27,
            net.kyori.adventure.text.Component.text("Growth Tool")
        );
        inventory.setItem(11, held.clone());
        inventory.setItem(13, new ItemStack(Material.EXPERIENCE_BOTTLE));
        inventory.setItem(15, new ItemStack(Material.ANVIL));
        event.getPlayer().openInventory(inventory);
    }

    public CompletionStage<Integer> stopAndFlush() {
        checkpointTask.cancel();
        return checkpoints.stopAndFlush();
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
            refreshSession(playerUuid).thenApply(ignored -> outcome)
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
        delivery.onJoin(player.getUniqueId());
        services.tasks().database(() -> repository.findOrCreate(
            player.getUniqueId(),
            clock.instant()
        )).thenAccept(sessions::open);
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
                found.ifPresent(sessions::open);
            }
        }));
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
            if (item != null && tool.toolId().toString().equals(text(item, TOOL_ID))) {
                return GrowthToolDeliveryCoordinator.Outcome.ALREADY_PRESENT;
            }
        }
        if (player.getInventory().firstEmpty() < 0) {
            return GrowthToolDeliveryCoordinator.Outcome.INVENTORY_FULL;
        }
        ItemStack item = new ItemStack(Material.WOODEN_PICKAXE);
        writeIdentity(item, tool, "GROWTH_TOOL");
        applyEvolution(item, tool);
        player.getInventory().addItem(item);
        return GrowthToolDeliveryCoordinator.Outcome.DELIVERED;
    }

    @SuppressWarnings("deprecation")
    private void applyEvolution(ItemStack item, GrowthTool tool) {
        EvolutionPlan.EvolutionSnapshot snapshot = config.evolutionPlan().evaluate(
            tool.cumulativeProgressUnits(),
            tool.branch(),
            config.enchantmentCaps()
        );
        item.setType(switch (snapshot.material()) {
            case WOOD -> Material.WOODEN_PICKAXE;
            case STONE -> Material.STONE_PICKAXE;
            case IRON -> Material.IRON_PICKAXE;
            case DIAMOND -> Material.DIAMOND_PICKAXE;
        });
        item.removeEnchantment(Enchantment.EFFICIENCY);
        item.removeEnchantment(Enchantment.UNBREAKING);
        item.removeEnchantment(Enchantment.FORTUNE);
        item.removeEnchantment(Enchantment.SILK_TOUCH);
        add(item, Enchantment.EFFICIENCY, snapshot.efficiency());
        add(item, Enchantment.UNBREAKING, snapshot.unbreaking());
        add(item, Enchantment.FORTUNE, snapshot.fortune());
        add(item, Enchantment.SILK_TOUCH, snapshot.silkTouch());
        writeIdentity(item, tool, "GROWTH_TOOL");
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
        return "GROWTH_TOOL".equals(text(item, ITEM_TYPE))
            && authority.toolId().toString().equals(text(item, TOOL_ID))
            && actor.toString().equals(text(item, OWNER_ID))
            && authority.instanceEpoch() == number(item, EPOCH)
            && authority.schemaVersion() == number(item, SCHEMA)
            && authority.status() == GrowthTool.Status.ACTIVE;
    }

    private boolean validAuthority(Player player, ItemStack item) {
        GrowthTool authority = sessions.current(player.getUniqueId()).orElse(null);
        if (authority == null
            || !authority.toolId().toString().equals(text(item, TOOL_ID))
            || !authority.ownerUuid().toString().equals(text(item, OWNER_ID))
            || authority.instanceEpoch() != number(item, EPOCH)
            || authority.schemaVersion() != number(item, SCHEMA)
            || authority.status() == GrowthTool.Status.REVOKED) {
            return false;
        }
        String type = text(item, ITEM_TYPE);
        return authority.status() == GrowthTool.Status.BROKEN
            ? "BROKEN_GROWTH_TOOL".equals(type)
            : "GROWTH_TOOL".equals(type);
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
        pdc.set(TOOL_ID, PersistentDataType.STRING, tool.toolId().toString());
        pdc.set(OWNER_ID, PersistentDataType.STRING, tool.ownerUuid().toString());
        pdc.set(EPOCH, PersistentDataType.LONG, tool.instanceEpoch());
        pdc.set(SCHEMA, PersistentDataType.INTEGER, tool.schemaVersion());
        pdc.set(REVISION, PersistentDataType.LONG, tool.displayRevision());
        item.setItemMeta(meta);
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
}
