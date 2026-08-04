package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class ReissuePricingTest {
    private final ReissuePricing pricing = new ReissuePricing(RepairPricing.defaults());

    @Test
    void usesBrokenPlusFullRepairPrice() {
        assertEquals(300, pricing.quote(0).amountWaymark());
        assertEquals(363, pricing.quote(3).amountWaymark());
        assertEquals(510, pricing.quote(10).amountWaymark());
        assertEquals(888, pricing.quote(28).amountWaymark());
    }

    @Test
    void orderingIsStrictForRepresentativeEvolutionCounts() {
        for (int evolutionCount : new int[] {0, 3, 10, 28}) {
            RepairPricing.Quote full = RepairPricing.defaults().quote(
                GrowthTool.Status.ACTIVE,
                evolutionCount,
                1,
                1
            );
            RepairPricing.Quote broken = RepairPricing.defaults().quote(
                GrowthTool.Status.BROKEN,
                evolutionCount,
                1,
                1
            );
            long reissue = pricing.quote(evolutionCount).amountWaymark();
            org.junit.jupiter.api.Assertions.assertTrue(
                reissue > broken.amountWaymark()
                    && broken.amountWaymark() > full.amountWaymark()
            );
        }
    }

    @Test
    void overflowAndInvalidOrderingFailClosed() {
        assertThrows(
            ArithmeticException.class,
            () -> new ReissuePricing(new RepairPricing(
                Long.MAX_VALUE,
                0,
                0,
                1,
                0
            )).quote(0)
        );
        assertThrows(
            ArithmeticException.class,
            () -> new ReissuePricing(new RepairPricing(0, 0, 0, 0, 0)).quote(0)
        );
    }
}
