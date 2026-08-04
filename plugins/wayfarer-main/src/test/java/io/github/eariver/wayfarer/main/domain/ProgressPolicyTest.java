package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

final class ProgressPolicyTest {
    private final ProgressPolicy policy = new ProgressPolicy(
        Set.of("resource", "resource_nether", "resource_end"),
        1000,
        BigDecimal.ONE,
        Map.of(
            "COBBLESTONE", new BigDecimal("0.25"),
            "DEEPSLATE", new BigDecimal("1.25")
        ),
        Map.of("DIAMOND", new BigDecimal("3.50"))
    );

    @Test
    void worldGateUsesExactNamesOnly() {
        assertTrue(policy.allowsWorld("resource"));
        assertTrue(policy.allowsWorld("resource_nether"));
        assertFalse(policy.allowsWorld("resource_backup"));
        assertFalse(policy.allowsWorld("RESOURCE"));
        assertFalse(policy.allowsWorld("main"));
    }

    @Test
    void calculatesDeterministicIntegerUnits() {
        assertEquals(250, policy.unitsFor("COBBLESTONE", null));
        assertEquals(1250, policy.unitsFor("DEEPSLATE", null));
        assertEquals(3500, policy.unitsFor("DIAMOND_ORE", "DIAMOND"));
        assertEquals(1000, policy.unitsFor("UNLISTED_PICKAXE_BLOCK", null));
    }
}
