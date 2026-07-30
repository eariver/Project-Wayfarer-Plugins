package io.github.eariver.wayfarer.main.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EvolutionPlan {
    private final String configRevision;
    private final long stoneThreshold;
    private final long ironThreshold;
    private final long diamondThreshold;
    private final long enchantBase;
    private final long enchantLinear;
    private final long enchantQuadratic;
    private volatile List<Long> thresholdCache;

    public EvolutionPlan(
        long stoneThreshold,
        long ironThreshold,
        long diamondThreshold,
        long enchantBase,
        long enchantLinear,
        long enchantQuadratic
    ) {
        this(
            "legacy",
            stoneThreshold,
            ironThreshold,
            diamondThreshold,
            enchantBase,
            enchantLinear,
            enchantQuadratic
        );
    }

    public EvolutionPlan(
        String configRevision,
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
        this.configRevision = Objects.requireNonNull(configRevision, "configRevision");
        if (configRevision.isBlank()) {
            throw new IllegalArgumentException("Config revision is required");
        }
        this.stoneThreshold = stoneThreshold;
        this.ironThreshold = ironThreshold;
        this.diamondThreshold = diamondThreshold;
        this.enchantBase = enchantBase;
        this.enchantLinear = enchantLinear;
        this.enchantQuadratic = enchantQuadratic;
        this.thresholdCache = List.of(
            stoneThreshold,
            ironThreshold,
            diamondThreshold
        );
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
        int silkTouch = branch == GrowthTool.Branch.SILK_TOUCH
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

    synchronized List<Long> thresholdsThrough(long progress) {
        List<Long> current = thresholdCache;
        if (current.get(current.size() - 1) > progress) {
            return current;
        }
        List<Long> values = new ArrayList<>(current);
        long cursor = values.get(values.size() - 1);
        long enchantIndex = values.size() - 2L;
        while (cursor <= progress) {
            long increment = Math.addExact(
                enchantBase,
                Math.addExact(
                    Math.multiplyExact(enchantLinear, enchantIndex),
                    Math.multiplyExact(enchantQuadratic, Math.multiplyExact(enchantIndex, enchantIndex))
                )
            );
            cursor = Math.addExact(cursor, increment);
            values.add(cursor);
            enchantIndex++;
        }
        thresholdCache = List.copyOf(values);
        return thresholdCache;
    }

    String configRevision() {
        return configRevision;
    }

    int cachedThresholdCount() {
        return thresholdCache.size();
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
