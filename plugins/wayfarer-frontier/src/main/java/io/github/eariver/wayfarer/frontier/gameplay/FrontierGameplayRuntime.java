package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.frontier.application.TraversalDeliveryCoordinator;
import io.github.eariver.wayfarer.frontier.application.TraversalLoadoutRepository;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Clock;
import java.util.UUID;

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

    public FrontierGameplayRuntime(
        JavaPlugin plugin,
        FrontierModuleConfig config,
        WayfarerServices services,
        TraversalLoadoutRepository repository,
        LeafGrappleBridge leafGrapple,
        Clock clock
    ) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.config = java.util.Objects.requireNonNull(config, "config");
        this.leafGrapple = java.util.Objects.requireNonNull(
            leafGrapple,
            "leafGrapple"
        );
        delivery = new TraversalDeliveryCoordinator(
            new FrontierWorldGate(java.util.Set.of(config.exactWorldName())),
            repository,
            services.tasks(),
            this::deliver,
            clock
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
}
