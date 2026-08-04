package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class GrowthToolInventoryPolicyTest {
    @Test
    void ordinaryContainersAreAllowedStorageBoundaries() {
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.CHEST));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.BARREL));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.SHULKER_BOX));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.HOPPER));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.DISPENSER));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.DROPPER));
        assertFalse(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.ENDER_CHEST));
    }

    @Test
    void identityChangingProcessingInventoriesAreDenied() {
        assertTrue(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.ANVIL));
        assertTrue(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.GRINDSTONE));
        assertTrue(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.SMITHING));
        assertTrue(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.CRAFTING));
        assertTrue(GrowthToolInventoryPolicy.isProcessingInventory(
            GrowthToolInventoryPolicy.InventoryBoundary.WORKBENCH));
    }

    @Test
    void onlyManagedItemTouchesInProcessingInventoryAreCancelled() {
        assertTrue(GrowthToolInventoryPolicy.shouldCancel(
            GrowthToolInventoryPolicy.InventoryBoundary.ANVIL, true, false, false, false, true
        ));
        assertTrue(GrowthToolInventoryPolicy.shouldCancel(
            GrowthToolInventoryPolicy.InventoryBoundary.GRINDSTONE, false, true, false, false, true
        ));
        assertTrue(GrowthToolInventoryPolicy.shouldCancel(
            GrowthToolInventoryPolicy.InventoryBoundary.SMITHING, false, false, true, true, false
        ));
        assertFalse(GrowthToolInventoryPolicy.shouldCancel(
            GrowthToolInventoryPolicy.InventoryBoundary.CHEST, true, false, false, false, true
        ));
        assertFalse(GrowthToolInventoryPolicy.shouldCancel(
            GrowthToolInventoryPolicy.InventoryBoundary.ANVIL, false, false, false, false, true
        ));
    }
}
