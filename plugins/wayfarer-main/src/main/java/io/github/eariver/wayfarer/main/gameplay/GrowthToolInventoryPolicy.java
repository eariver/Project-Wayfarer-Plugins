package io.github.eariver.wayfarer.main.gameplay;

import org.bukkit.event.inventory.InventoryType;

/** Identity-changing inventory boundaries for the managed Growth Tool. */
public final class GrowthToolInventoryPolicy {
    private GrowthToolInventoryPolicy() {
    }

    public enum InventoryBoundary {
        ANVIL,
        GRINDSTONE,
        SMITHING,
        CRAFTING,
        WORKBENCH,
        STONECUTTER,
        CARTOGRAPHY,
        LOOM,
        ENCHANTING,
        BEACON,
        CHEST,
        BARREL,
        SHULKER_BOX,
        HOPPER,
        DISPENSER,
        DROPPER,
        ENDER_CHEST,
        OTHER
    }

    public static boolean isProcessingInventory(InventoryBoundary boundary) {
        if (boundary == null) {
            return false;
        }
        return switch (boundary) {
            case ANVIL, GRINDSTONE, SMITHING, CRAFTING, WORKBENCH,
                STONECUTTER, CARTOGRAPHY, LOOM, ENCHANTING, BEACON -> true;
            default -> false;
        };
    }

    public static boolean isProcessingInventory(InventoryType type) {
        if (type == null) {
            return false;
        }
        return isProcessingInventory(switch (type) {
            case ANVIL -> InventoryBoundary.ANVIL;
            case GRINDSTONE -> InventoryBoundary.GRINDSTONE;
            case SMITHING -> InventoryBoundary.SMITHING;
            case CRAFTING -> InventoryBoundary.CRAFTING;
            case WORKBENCH -> InventoryBoundary.WORKBENCH;
            case STONECUTTER -> InventoryBoundary.STONECUTTER;
            case CARTOGRAPHY -> InventoryBoundary.CARTOGRAPHY;
            case LOOM -> InventoryBoundary.LOOM;
            case ENCHANTING -> InventoryBoundary.ENCHANTING;
            case BEACON -> InventoryBoundary.BEACON;
            case CHEST -> InventoryBoundary.CHEST;
            case BARREL -> InventoryBoundary.BARREL;
            case SHULKER_BOX -> InventoryBoundary.SHULKER_BOX;
            case HOPPER -> InventoryBoundary.HOPPER;
            case DISPENSER -> InventoryBoundary.DISPENSER;
            case DROPPER -> InventoryBoundary.DROPPER;
            case ENDER_CHEST -> InventoryBoundary.ENDER_CHEST;
            default -> InventoryBoundary.OTHER;
        });
    }

    public static boolean shouldCancel(
        InventoryBoundary type,
        boolean currentManaged,
        boolean cursorManaged,
        boolean hotbarManaged,
        boolean shiftClick,
        boolean clickedTop
    ) {
        // The event-shape flags are part of the policy contract. Any managed
        // item crossing a processing boundary is denied, independent of the
        // particular click mode.
        return isProcessingInventory(type)
            && (currentManaged || cursorManaged || hotbarManaged);
    }

    public static boolean shouldCancel(
        InventoryType type,
        boolean currentManaged,
        boolean cursorManaged,
        boolean hotbarManaged,
        boolean shiftClick,
        boolean clickedTop
    ) {
        return shouldCancel(
            type == null ? InventoryBoundary.OTHER : switch (type) {
                case ANVIL -> InventoryBoundary.ANVIL;
                case GRINDSTONE -> InventoryBoundary.GRINDSTONE;
                case SMITHING -> InventoryBoundary.SMITHING;
                case CRAFTING -> InventoryBoundary.CRAFTING;
                case WORKBENCH -> InventoryBoundary.WORKBENCH;
                case STONECUTTER -> InventoryBoundary.STONECUTTER;
                case CARTOGRAPHY -> InventoryBoundary.CARTOGRAPHY;
                case LOOM -> InventoryBoundary.LOOM;
                case ENCHANTING -> InventoryBoundary.ENCHANTING;
                case BEACON -> InventoryBoundary.BEACON;
                default -> InventoryBoundary.OTHER;
            },
            currentManaged,
            cursorManaged,
            hotbarManaged,
            shiftClick,
            clickedTop
        );
    }
}
