package io.github.eariver.wayfarer.main.domain;

import java.util.Objects;

/**
 * Derives the reissue price from the existing repair policy.
 *
 * <p>The repair policy remains the only owner of rounding, clamping, and
 * repair formula details.  This class only composes the two existing quotes
 * and applies the reissue ordering/overflow guard.</p>
 */
public final class ReissuePricing {
    private static final int NOMINAL_DAMAGE = 1;
    private static final int NOMINAL_DURABILITY = 1;

    private final RepairPricing repairPricing;

    public ReissuePricing(RepairPricing repairPricing) {
        this.repairPricing = Objects.requireNonNull(repairPricing, "repairPricing");
    }

    public RepairPricing.Quote quote(int evolutionCount) {
        RepairPricing.Quote full = repairPricing.quote(
            GrowthTool.Status.ACTIVE,
            evolutionCount,
            NOMINAL_DAMAGE,
            NOMINAL_DURABILITY
        );
        RepairPricing.Quote broken = repairPricing.quote(
            GrowthTool.Status.BROKEN,
            evolutionCount,
            NOMINAL_DAMAGE,
            NOMINAL_DURABILITY
        );
        if (!full.available() || !broken.available()
            || full.amountWaymark() < 1
            || broken.amountWaymark() <= full.amountWaymark()) {
            throw new ArithmeticException("Reissue pricing ordering is invalid");
        }
        return new RepairPricing.Quote(
            true,
            Math.addExact(broken.amountWaymark(), full.amountWaymark()),
            null
        );
    }
}
