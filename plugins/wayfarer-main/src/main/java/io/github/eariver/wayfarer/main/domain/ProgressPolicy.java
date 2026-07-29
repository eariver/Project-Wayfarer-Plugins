package io.github.eariver.wayfarer.main.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ProgressPolicy {
    private final Set<String> allowedWorlds;
    private final int scale;
    private final BigDecimal fallbackWeight;
    private final Map<String, BigDecimal> blockWeights;
    private final Map<String, BigDecimal> oreMultipliers;

    public ProgressPolicy(
        Set<String> allowedWorlds,
        int scale,
        BigDecimal fallbackWeight,
        Map<String, BigDecimal> blockWeights,
        Map<String, BigDecimal> oreMultipliers
    ) {
        this.allowedWorlds = Set.copyOf(allowedWorlds);
        this.scale = scale;
        this.fallbackWeight = positive(fallbackWeight, "fallbackWeight");
        this.blockWeights = positiveValues(blockWeights);
        this.oreMultipliers = positiveValues(oreMultipliers);
        if (scale <= 0 || this.allowedWorlds.isEmpty()) {
            throw new IllegalArgumentException("Progress scale and world allowlist are required");
        }
    }

    public boolean allowsWorld(String exactWorldName) {
        return allowedWorlds.contains(exactWorldName);
    }

    public long unitsFor(String material, String oreGroup) {
        BigDecimal base = blockWeights.getOrDefault(material, fallbackWeight);
        BigDecimal multiplier = oreGroup == null
            ? BigDecimal.ONE
            : oreMultipliers.getOrDefault(oreGroup, BigDecimal.ONE);
        return base.multiply(multiplier)
            .multiply(BigDecimal.valueOf(scale))
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact();
    }

    private static Map<String, BigDecimal> positiveValues(Map<String, BigDecimal> values) {
        Objects.requireNonNull(values, "values");
        values.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Progress map key is required");
            }
            positive(value, key);
        });
        return Map.copyOf(values);
    }

    private static BigDecimal positive(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }
}
