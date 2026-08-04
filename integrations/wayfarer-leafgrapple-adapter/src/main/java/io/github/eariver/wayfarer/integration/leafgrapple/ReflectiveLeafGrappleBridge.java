package io.github.eariver.wayfarer.integration.leafgrapple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Version-isolated adapter for the observed public LeafGrapple 1.0.2 surface.
 *
 * <p>No LeafGrapple type appears in the compile-time signature. A hook tier is usable only when
 * its public capability reports durability and entity hooking disabled.</p>
 */
public final class ReflectiveLeafGrappleBridge implements LeafGrappleBridge {
    private static final String EXPECTED_VERSION = "1.0.2";

    private final Plugin ownerPlugin;
    private final Plugin leafGrapple;
    private final Capability capability;
    private final Object hookItemService;
    private final Object safeTier;
    private final Method createHookItem;

    public ReflectiveLeafGrappleBridge(Plugin ownerPlugin, Plugin leafGrapple) {
        this.ownerPlugin = Objects.requireNonNull(ownerPlugin, "ownerPlugin");
        this.leafGrapple = leafGrapple;
        Probe probe = probe(leafGrapple);
        capability = probe.capability;
        hookItemService = probe.hookItemService;
        safeTier = probe.safeTier;
        createHookItem = probe.createHookItem;
    }

    @Override
    public String expectedVersion() {
        return EXPECTED_VERSION;
    }

    @Override
    public Capability capability() {
        return capability;
    }

    @Override
    public ItemStack createHook(UUID ownerUuid, long instanceEpoch) {
        Objects.requireNonNull(ownerUuid, "ownerUuid");
        if (instanceEpoch < 1) {
            throw new IllegalArgumentException("Instance epoch must be positive");
        }
        if (capability != Capability.AVAILABLE) {
            throw new IllegalStateException("LeafGrapple capability is unavailable");
        }
        try {
            Object result = createHookItem.invoke(hookItemService, safeTier);
            if (!(result instanceof ItemStack item)) {
                throw new IllegalStateException("LeafGrapple hook factory returned no item");
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                throw new IllegalStateException("LeafGrapple hook item has no metadata");
            }
            meta.setUnbreakable(true);
            set(meta, "item_type", "GRAPPLING_HOOK");
            set(meta, "item_instance_id", UUID.randomUUID().toString());
            set(meta, "owner_uuid", ownerUuid.toString());
            set(meta, "theme_id", "worlds_beyond");
            set(meta, "instance_epoch", Long.toString(instanceEpoch));
            set(meta, "schema_version", "1");
            item.setItemMeta(meta);
            return item;
        } catch (IllegalAccessException | InvocationTargetException failure) {
            throw new IllegalStateException("LeafGrapple hook creation failed");
        }
    }

    private void set(ItemMeta meta, String key, String value) {
        NamespacedKey namespacedKey = new NamespacedKey(ownerPlugin, key);
        meta.getPersistentDataContainer().set(
            namespacedKey,
            PersistentDataType.STRING,
            value
        );
    }

    private static Probe probe(Plugin plugin) {
        if (plugin == null || !plugin.isEnabled()) {
            return Probe.unavailable(Capability.MISSING_PLUGIN);
        }
        if (!EXPECTED_VERSION.equals(plugin.getPluginMeta().getVersion())) {
            return Probe.unavailable(Capability.UNSUPPORTED_VERSION);
        }
        try {
            Class<?> pluginClass = plugin.getClass();
            if (!"me.LeafPixel.leafgrapple.LeafGrapplePlugin".equals(pluginClass.getName())) {
                return Probe.unavailable(Capability.MISSING_API);
            }
            Method serviceMethod = pluginClass.getMethod("hookItemService");
            Object service = serviceMethod.invoke(plugin);
            if (service == null) {
                return Probe.unavailable(Capability.MISSING_API);
            }
            Method getTiers = service.getClass().getMethod("getTiers");
            Object values = getTiers.invoke(service);
            if (!(values instanceof Collection<?> tiers)) {
                return Probe.unavailable(Capability.MISSING_API);
            }
            Object safeTier = tiers.stream()
                .filter(Objects::nonNull)
                .filter(ReflectiveLeafGrappleBridge::safeTier)
                .findFirst()
                .orElse(null);
            if (safeTier == null) {
                return Probe.unavailable(Capability.UNSAFE_CONFIGURATION);
            }
            Method create = service.getClass().getMethod("createHookItem", safeTier.getClass());
            return new Probe(Capability.AVAILABLE, service, safeTier, create);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException failure) {
            return Probe.unavailable(Capability.MISSING_API);
        }
    }

    private static boolean safeTier(Object tier) {
        try {
            Method durabilityEnabled = tier.getClass().getMethod("durabilityEnabled");
            Method entityHookSettings = tier.getClass().getMethod("entityHookSettings");
            if (Boolean.TRUE.equals(durabilityEnabled.invoke(tier))) {
                return false;
            }
            Object settings = entityHookSettings.invoke(tier);
            if (settings == null) {
                return false;
            }
            Method enabled = settings.getClass().getMethod("enabled");
            return Boolean.FALSE.equals(enabled.invoke(settings));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException failure) {
            return false;
        }
    }

    private record Probe(
        Capability capability,
        Object hookItemService,
        Object safeTier,
        Method createHookItem
    ) {
        private static Probe unavailable(Capability capability) {
            return new Probe(capability, null, null, null);
        }
    }
}
