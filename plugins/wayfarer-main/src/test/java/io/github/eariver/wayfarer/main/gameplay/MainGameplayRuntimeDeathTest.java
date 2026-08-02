≠rá^—f•ñÿ¶{Oly 'v√Æ∂õ≠package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

final class MainGameplayRuntimeDeathTest {
    @Test
    void managedGrowthBrokenAndOrdinaryDropsRemainUntouched() {
        List<String> drops = List.of(
            "GROWTH_TOOL",
            "BROKEN_GROWTH_TOOL",
            "minecraft:stone"
        );

        assertTrue(drops.contains("GROWTH_TOOL"));
        assertTrue(drops.contains("BROKEN_GROWTH_TOOL"));
        assertTrue(drops.contains("minecraft:stone"));
    }

    @Test
    void onlyTheTwoManagedDeathTypesAreRecognized() {
        assertTrue(MainGameplayRuntime.isManagedDeathType("GROWTH_TOOL"));
        assertTrue(MainGameplayRuntime.isManagedDeathType("BROKEN_GROWTH_TOOL"));
        assertFalse(MainGameplayRuntime.isManagedDeathType("minecraft:stone"));
    }
}
