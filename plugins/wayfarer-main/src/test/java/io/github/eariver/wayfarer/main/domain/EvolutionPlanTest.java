package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class EvolutionPlanTest {
    private final EvolutionPlan plan = EvolutionPlan.defaults();
    private final EvolutionPlan.EnchantmentCaps caps = EvolutionPlan.EnchantmentCaps.defaults();

    @Test
    void resolvesMaterialThresholdsWithIntegerUnits() {
        assertEquals(EvolutionPlan.MaterialTier.WOOD, evaluate(99_999).material());
        assertEquals(EvolutionPlan.MaterialTier.STONE, evaluate(100_000).material());
        assertEquals(EvolutionPlan.MaterialTier.IRON, evaluate(400_000).material());
        assertEquals(EvolutionPlan.MaterialTier.DIAMOND, evaluate(1_200_000).material());
    }

    @Test
    void generatesEnchantThresholdsIterativelyAndUsesCycle() {
        EvolutionPlan.EvolutionSnapshot beforeFirst = evaluate(2_000_000);
        assertEquals(3, beforeFirst.evolutionCount());

        EvolutionPlan.EvolutionSnapshot first = evaluate(2_240_000);
        assertEquals(4, first.evolutionCount());
        assertEquals(1, first.efficiency());

        EvolutionPlan.EvolutionSnapshot cycle = evaluate(10_400_000);
        assertEquals(8, cycle.evolutionCount());
        assertEquals(2, cycle.efficiency());
        assertEquals(2, cycle.unbreaking());
        assertEquals(1, cycle.fortune());
    }

    @Test
    void usesOneBasedEnchantFormulaAndReusesRevisionLocalCache() {
        assertEquals(
            java.util.List.of(100_000L, 400_000L, 1_200_000L, 2_240_000L),
            plan.thresholdsThrough(1_200_000)
        );
        assertEquals(4, plan.cachedThresholdCount());
        plan.evaluate(1_200_001, GrowthTool.Branch.FORTUNE, caps);
        assertEquals(4, plan.cachedThresholdCount());

        java.util.List<Long> throughTen = plan.thresholdsThrough(10_400_000);
        assertEquals(2_800_000L, throughTen.get(7) - throughTen.get(6));
        assertEquals(6_800_000L,
            plan.thresholdsThrough(40_000_000).get(12)
                - plan.thresholdsThrough(40_000_000).get(11));

        EvolutionPlan otherRevision = new EvolutionPlan(
            "revision-2",
            100_000,
            400_000,
            1_200_000,
            800_000,
            200_000,
            40_000
        );
        assertEquals(3, otherRevision.cachedThresholdCount());
        assertEquals("revision-2", otherRevision.configRevision());
    }

    @Test
    void appliesSilkBranchImmediatelyAndHonorsZeroCap() {
        EvolutionPlan.EvolutionSnapshot silk = plan.evaluate(
            0,
            GrowthTool.Branch.SILK_TOUCH,
            caps
        );
        assertEquals(1, silk.silkTouch());
        assertEquals(0, silk.fortune());
        assertEquals(0, plan.evaluate(
            0,
            GrowthTool.Branch.SILK_TOUCH,
            new EvolutionPlan.EnchantmentCaps(10, 10, 5, 0)
        ).silkTouch());
    }

    @Test
    void appliesBranchAndEffectiveCapsWithoutStoppingConceptualEvolution() {
        EvolutionPlan.EnchantmentCaps lowCaps =
            new EvolutionPlan.EnchantmentCaps(1, 1, 1, 1);
        EvolutionPlan.EvolutionSnapshot fortune =
            plan.evaluate(100_000_000, GrowthTool.Branch.FORTUNE, lowCaps);
        EvolutionPlan.EvolutionSnapshot silk =
            plan.evaluate(100_000_000, GrowthTool.Branch.SILK_TOUCH, lowCaps);

        assertEquals(1, fortune.efficiency());
        assertEquals(1, fortune.unbreaking());
        assertEquals(1, fortune.fortune());
        assertEquals(0, fortune.silkTouch());
        assertEquals(0, silk.fortune());
        assertEquals(1, silk.silkTouch());
        assertEquals(fortune.evolutionCount(), silk.evolutionCount());
    }

    @Test
    void rejectsInvalidAndOverflowingPlans() {
        assertThrows(IllegalArgumentException.class,
            () -> new EvolutionPlan(100, 100, 200, 1, 1, 1));
        EvolutionPlan overflowing =
            new EvolutionPlan(1, 2, Long.MAX_VALUE - 1, 10, 0, 0);
        assertThrows(ArithmeticException.class,
            () -> overflowing.evaluate(Long.MAX_VALUE, GrowthTool.Branch.FORTUNE, caps));
    }

    private EvolutionPlan.EvolutionSnapshot evaluate(long units) {
        return plan.evaluate(units, GrowthTool.Branch.FORTUNE, caps);
    }
}
