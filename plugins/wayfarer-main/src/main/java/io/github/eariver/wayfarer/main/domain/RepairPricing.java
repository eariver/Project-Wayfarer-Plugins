package io.github.eariver.wayfarer.main.domain;

public final class RepairPricing {
    private final long baseCost;
    private final double evolutionMultiplier;
    private final double minimumChargeRatio;
    private final long brokenSurcharge;
    private final long brokenPerEvolution;

    public RepairPricing(
        long baseCost,
        double evolutionMultiplier,
        double minimumChargeRatio,
        long brokenSurcharge,
        long brokenPerEvolution
    ) {
        if (baseCost < 0 || evolutionMultiplier < 0 || minimumChargeRatio < 0
            || minimumChargeRatio > 1 || brokenSurcharge < 0 || brokenPerEvolution < 0) {
            throw new IllegalArgumentException("Repair pricing is invalid");
        }
        this.baseCost = baseCost;
        this.evolutionMultiplier = evolutionMultiplier;
        this.minimumChargeRatio = minimumChargeRatio;
        this.brokenSurcharge = brokenSurcharge;
        this.brokenPerEvolution = brokenPerEvolution;
    }

    public static RepairPricing defaults() {
        return new RepairPricing(100, 0.08, 0.25, 100, 5);
    }

    public Quote quote(
        GrowthTool.Status status,
        int evolutionCount,
        int currentDamage,
        int maximumDurability
    ) {
        if (evolutionCount < 0 || currentDamage < 0 || maximumDurability <= 0
            || currentDamage > maximumDurability) {
            throw new IllegalArgumentException("Repair state is invalid");
        }
        if (status == GrowthTool.Status.REVOKED || currentDamage == 0) {
            return new Quote(false, 0, "NOT_REPAIRABLE");
        }
        long fullCost = ceil(baseCost * (1.0 + evolutionCount * evolutionMultiplier));
        if (status == GrowthTool.Status.BROKEN) {
            return new Quote(
                true,
                Math.addExact(fullCost, Math.addExact(
                    brokenSurcharge,
                    Math.multiplyExact(brokenPerEvolution, evolutionCount)
                )),
                null
            );
        }
        double missingRatio = (double) currentDamage / maximumDurability;
        return new Quote(true, ceil(fullCost * Math.max(minimumChargeRatio, missingRatio)), null);
    }

    private static long ceil(double value) {
        if (!Double.isFinite(value) || value > Long.MAX_VALUE) {
            throw new ArithmeticException("Repair cost overflow");
        }
        return (long) Math.ceil(value);
    }

    public record Quote(boolean available, long amountWaymark, String failureCode) {}
}
