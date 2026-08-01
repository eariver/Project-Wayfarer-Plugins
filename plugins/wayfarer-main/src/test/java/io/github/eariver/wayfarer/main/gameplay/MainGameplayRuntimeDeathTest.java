package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

final class MainGameplayRuntimeDeathTest {
    @Test
    void managedGrowthAndBrokenDropsAreRemovedWithoutTouchingOtherDrops() {
        List<String> drops = new ArrayList<>(List.of(
            "GROWTH_TOOL",
            "BROKEN_GROWTH_TOOL",
            "minecraft:stone"
        ));

        MainGameplayRuntime.removeManagedDeathDrops(drops, value -> value);

        assertEquals(1, drops.size());
        assertEquals("minecraft:stone", drops.get(0));
    }

    @Test
    void dropsWithoutManagedToolsAreNoOp() {
        List<String> drops = new ArrayList<>(List.of("minecraft:stone"));

        MainGameplayRuntime.removeManagedDeathDrops(drops, value -> value);

        assertEquals(List.of("minecraft:stone"), drops);
    }

    @Test
    void onlyTheTwoManagedDeathTypesAreRecognized() {
        assertTrue(MainGameplayRuntime.isManagedDeathType("GROWTH_TOOL"));
        assertTrue(MainGameplayRuntime.isManagedDeathType("BROKEN_GROWTH_TOOL"));
        assertFalse(MainGameplayRuntime.isManagedDeathType("minecraft:stone"));
    }
}
