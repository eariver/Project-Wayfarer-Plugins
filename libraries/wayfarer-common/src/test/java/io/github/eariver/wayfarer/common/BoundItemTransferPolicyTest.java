package io.github.eariver.wayfarer.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class BoundItemTransferPolicyTest {
    @Test
    void deniesOwnerContainerShiftHotbarCursorAndDragPaths() {
        assertTrue(BoundItemTransferPolicy.denyContainerClick(
            true, true, false, false, true, false
        ));
        assertTrue(BoundItemTransferPolicy.denyContainerClick(
            true, false, true, false, false, true
        ));
        assertTrue(BoundItemTransferPolicy.denyContainerClick(
            true, false, false, true, false, true
        ));
        assertTrue(BoundItemTransferPolicy.denyContainerDrag(
            true, true, true
        ));
    }

    @Test
    void doesNotBlockOrdinaryItemsOrPlayerInventoryReordering() {
        assertFalse(BoundItemTransferPolicy.denyContainerClick(
            true, false, false, false, true, true
        ));
        assertFalse(BoundItemTransferPolicy.denyContainerClick(
            false, true, true, true, true, true
        ));
        assertFalse(BoundItemTransferPolicy.denyContainerDrag(
            false, true, true
        ));
    }
}
