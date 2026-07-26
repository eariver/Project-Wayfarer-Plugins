package io.github.eariver.wayfarer.integration.leafgrapple;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface LeafGrappleBridge {
    String expectedVersion();
    Capability capability();
    ItemStack createHook(UUID ownerUuid, long instanceEpoch);

    enum Capability {
        AVAILABLE, MISSING_PLUGIN, UNSUPPORTED_VERSION, MISSING_API
    }
}
