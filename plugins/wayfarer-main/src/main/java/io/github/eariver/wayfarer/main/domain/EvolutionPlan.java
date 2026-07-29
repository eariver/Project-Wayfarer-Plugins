package io.github.eariver.wayfarer.main.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class EvolutionPlan {
    private final long stoneThreshold;
    private final long ironThreshold;
    private final long diamondThreshold;
    private final long enchantBase;
    private final long enchantLinear;
    private final long enchantQuadratic;

    public EvolutionPlan(
        long stoneThreshold,
        long ironThreshold,
        long diamondThreshold,
        long enchantBase,
        long enchantLinear,
        long enchantQuadratic
    ) {
        if (stoneThreshold <= 0 || ironThreshold <= stoneThreshold
            || diamondThreshold <= ironThreshold || enchantBase <= 0
            || enchantLinear < 0 || enchantQuadratic < 0) {
            throw new IllegalArgumentException("Evolution thresholds are invalid");
        }
        this.stoneThreshold = stoneThreshold;
        this.ironThreshold = ironThreshold;
        this.diamondThreshold = diamondThreshold;
        this.enchantBase = enchantBase;
        this.enchantLinear = enchantLinear;
        this.enchantQuadratic = enchantQuadratic;
    }

    public static EvolutionPlan defaults() {
        return new EvolutionPlan(100_000, 400_000, 1_200_000, 800_000, 200_000, 40_000);
    }

    public EvolutionSnapshot evaluate(
        long cumulativeProgressUnits,
        GrowthTool.Branch branch,
        EnchantmentCaps caps
    ) {
        if (cumulativeProgressUnits < 0) {
            throw new IllegalArgumentException("Progress cannot be negative");
        }
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(caps, "caps");

        List<Long> thresholds = thresholdsThrough(cumulativeProgressUnits);
        int evolutionCount = upperBound(thresholds, cumulativeProgressUnits);
        MaterialTier tier = switch (Math.min(evolutionCount, 3)) {
            case 0 -> MaterialTier.WOOD;
            case 1 -> MaterialTier.STONE;
            case 2 -> MaterialTier.IRON;
            default -> MaterialTier.DIAMOND;
        };
        int enchantEvolutionCount = Math.max(0, evolutionCount - 3);
        int efficiency = 0;
        int unbreaking = 0;
        int fortune = 0;
        for (int index = 0; index < enchantEvolutionCount; index++) {
            switch (index % 5) {
                case 0, 2 -> efficiency++;
                case 1, 3 -> unbreaking++;
                case 4 -> fortune++;
                default -> throw new AssertionError("Unreachable enchant cycle");
            }
        }
        efficiency = Math.min(efficiency, caps.efficiency());
        unbreaking = Math.min(unbreaking, caps.unbreaking());
        fortune = branch == GrowthTool.Branch.FORTUNE
            ? Math.min(fortune, caps.fortune())
            : 0;
        int silkTouch = branch == GrowthTool.Branch.SILK_TOUCH && enchantEvolutionCount > 0
            ? Math.min(1, caps.silkTouch())
            : 0;
        Long next = evolutionCount < thresholds.size() ? thresholds.get(evolutionCount) : null;
        return new EvolutionSnapshot(
            tier,
            evolutionCount,
            enchantEvolutionCount,
            efficiency,
            unbreaking,
            fortune,
            silkTouch,
            next
        );
    }

    List<Long> thresholdsThrough(long progress) {
        List<Long> values = new ArrayList<>();
        values.add(stoneThreshold);
        values.add(ironThreshold);
        values.add(diamondThreshold);
        long cursor = diamondThreshold;
        for (long enchantIndex = 0; cursor <= progress; enchantIndex++) {
            long increment = Math.addExact(
                enchantBase,
                Math.addExact(
                    Math.multiplyExact(enchantLinear, enchantIndex),
                    Math.multiplyExact(enchantQuadratic, Math.multiplyExact(enchantIndex, enchantIndex))
                )
            );
            cursor = Math.addExact(cursor, increment);
            values.add(cursor);
        }
        return Collections.unmodifiableList(values);
    }

    private static int upperBound(List<Long> sorted, long value) {
        int low = 0;
        int high = sorted.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (sorted.get(middle) <= value) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    public enum MaterialTier {
        WOOD,
        STONE,
        IRON,
        DIAMOND
    }

    public record EnchantmentCaps(int efficiency, int unbreaking, int fortune, int silkTouch) {
        public EnchantmentCaps {
            if (efficiency < 0 || unbreaking < 0 || fortune < 0 || silkTouch < 0) {
                throw new IllegalArgumentException("Enchantment caps cannot be negative");
            }
        }

        public static EnchantmentCaps defaults() {
            return new EnchantmentCaps(10, 10, 5, 1);
        }
    }

    public record EvolutionSnapshot(
        MaterialTier material,
        int evolutionCount,
        int enchantEvolutionCount,
        int efficiency,
        int unbreaking,
        int fortune,
        int silkTouch,
        Long nextThresholdUnits
    ) {}
}
