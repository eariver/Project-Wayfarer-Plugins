package io.github.eariver.wayfarer.main.domain;

public final class DurabilitySemantics {
    private DurabilitySemantics() {}

    public static int afterEvolution(
        int currentDamage,
        boolean evolutionCountIncreased
    ) {
        if (currentDamage < 0) {
            throw new IllegalArgumentException("Damage cannot be negative");
        }
        return evolutionCountIncreased ? 0 : currentDamage;
    }

    public static int reconcileActive(
        int oldMaximum,
        int oldDamage,
        int newMaximum
    ) {
        if (oldMaximum < 1 || newMaximum < 1
            || oldDamage < 0 || oldDamage >= oldMaximum) {
            throw new IllegalArgumentException(
                "Active durability snapshot is invalid"
            );
        }
        long oldRemaining = oldMaximum - oldDamage;
        long scaledRemaining = Math.addExact(
            Math.multiplyExact(oldRemaining, (long) newMaximum),
            oldMaximum - 1L
        ) / oldMaximum;
        int newRemaining = (int) Math.max(
            1L,
            Math.min(newMaximum, scaledRemaining)
        );
        return newMaximum - newRemaining;
    }
}
