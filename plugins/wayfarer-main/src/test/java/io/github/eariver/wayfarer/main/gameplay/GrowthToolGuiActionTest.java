package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class GrowthToolGuiActionTest {
    @Test
    void mapsMainAndRepairPreviewActionsWithoutAcceptingInventorySlots() {
        assertEquals(
            GrowthToolGuiAction.Action.OPEN_REPAIR_PREVIEW,
            GrowthToolGuiAction.main(14)
        );
        assertEquals(
            GrowthToolGuiAction.Action.HELP,
            GrowthToolGuiAction.main(16)
        );
        assertEquals(
            GrowthToolGuiAction.Action.CONFIRM_REPAIR,
            GrowthToolGuiAction.repairPreview(11)
        );
        assertEquals(
            GrowthToolGuiAction.Action.CANCEL,
            GrowthToolGuiAction.repairPreview(15)
        );
        assertEquals(
            GrowthToolGuiAction.Action.NONE,
            GrowthToolGuiAction.main(27)
        );
        assertEquals(
            GrowthToolGuiAction.Action.NONE,
            GrowthToolGuiAction.repairPreview(-1)
        );
    }
}
