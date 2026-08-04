package io.github.eariver.wayfarer.main.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class RepairPricingTest {
    private final RepairPricing pricing = RepairPricing.defaults();

    @Test
    void pricesActiveRepairWithMinimumAndMissingRatios() {
        RepairPricing.Quote minimum =
            pricing.quote(GrowthTool.Status.ACTIVE, 5, 1, 100);
        RepairPricing.Quote half =
            pricing.quote(GrowthTool.Status.ACTIVE, 5, 50, 100);

        assertEquals(35, minimum.amountWaymark());
        assertEquals(70, half.amountWaymark());
    }

    @Test
    void pricesBrokenRepairWithSurcharges() {
        RepairPricing.Quote quote =
            pricing.quote(GrowthTool.Status.BROKEN, 5, 100, 100);
        assertTrue(quote.available());
        assertEquals(265, quote.amountWaymark());
    }

    @Test
    void rejectsFullDurabilityAndRevokedTools() {
        RepairPricing.Quote full =
            pricing.quote(GrowthTool.Status.ACTIVE, 0, 0, 100);
        RepairPricing.Quote revoked =
            pricing.quote(GrowthTool.Status.REVOKED, 0, 50, 100);
        assertFalse(full.available());
        assertFalse(revoked.available());
        assertEquals(0, full.amountWaymark());
    }
}
