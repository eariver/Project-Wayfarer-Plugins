package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.frontier.application.TraversalDeliveryCoordinator;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
import io.github.eariver.wayfarer.frontier.application.FrontierPurchaseRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadRepository;
import io.github.eariver.wayfarer.frontier.application.LaunchpadUseCoordinator;
import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import io.github.eariver.wayfarer.frontier.domain.FrontierWorldGate;
import io.github.eariver.wayfarer.frontier.domain.PendingDelivery;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
    private final JavaPlugin plugin;
    private final FrontierModuleConfig config;
    private final LeafGrappleBridge leafGrapple;
    private final TraversalDeliveryCoordinator delivery;
    private final WayfarerServices services;
    private final LaunchpadRepository launchpads;
    private final LaunchpadUseCoordinator launchpadUse;
    private final ConcurrentHashMap<UUID, Instant> cooldowns =
        new ConcurrentHashMap<>();

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
        this.launchpads = java.util.Objects.requireNonNull(launchpads, "launchpads");
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
            config.launchpad().expiration()
        );
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            enter(player);
        }
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
            launchpads.create(launchpad, Instant.now())
        ).thenAccept(created -> services.tasks().mainThread(() -> {
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
            .thenAccept(found -> found.ifPresent(launchpad ->
                launchpadUse.use(new LaunchpadUseCoordinator.Request(
                    launchpad.launchpadId(),
                    event.getPlayer().getUniqueId(),
                    event.getPlayer().getWorld().getName(),
                    event.getPlayer().isSneaking(),
                    cooldowns.get(event.getPlayer().getUniqueId())
                )).thenAccept(result -> {
                    if (result.outcome() == Launchpad.Outcome.LAUNCHED) {
                        cooldowns.put(
                            event.getPlayer().getUniqueId(),
                            Instant.now().plus(config.launchpad().cooldown())
                        );
                    }
                })
            ));
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

    private void enter(Player player) {
        delivery.onSafeEntry(
            player.getUniqueId(),
            player.getWorld().getName()
        );
    }

    private TraversalDeliveryCoordinator.DeliveryOutcome deliver(
        UUID playerUuid,
        PendingDelivery pending
    ) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        if (player == null || !player.isOnline()) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.PLAYER_OFFLINE;
        }
        if (!config.exactWorldName().equals(player.getWorld().getName())) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.LEFT_THEME;
        }
        if (isPermanent(pending.itemType())
            && containsType(player, pending.itemType().name())) {
            return TraversalDeliveryCoordinator.DeliveryOutcome.ALREADY_PRESENT;
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

    public boolean deliverPurchase(
        FrontierPurchaseRepository.Purchase purchase,
        UUID deliveryId
    ) {
        PendingDelivery pending = new PendingDelivery(
            deliveryId,
            purchase.playerUuid(),
            "worlds-beyond",
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
        item.setItemMeta(meta);
    }

    private static boolean containsType(Player player, String itemType) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && itemType.equals(text(item, ITEM_TYPE))) {
                return true;
            }
        }
        return false;
    }

    private static boolean permanent(ItemStack item) {
        String type = text(item, ITEM_TYPE);
        return type != null && switch (type) {
            case "ELYTRA", "GRAPPLING_HOOK", "NAVIGATION" -> true;
            default -> false;
        };
    }

    private static boolean isPermanent(PendingDelivery.ItemType type) {
        return switch (type) {
            case ELYTRA, GRAPPLING_HOOK, NAVIGATION -> true;
            case LAUNCHPAD, FIREWORK_ROCKET -> false;
        };
    }

    private static String text(ItemStack item, NamespacedKey key) {
        return item.getItemMeta().getPersistentDataContainer().get(
            key,
            PersistentDataType.STRING
        );
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
    }
}
