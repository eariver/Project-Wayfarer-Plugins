package io.github.eariver.wayfarer.frontier.domain;

import java.util.Objects;
import java.util.Set;

public final class FrontierWorldGate {
    private final Set<String> allowedWorldNames;

    public FrontierWorldGate(Set<String> allowedWorldNames) {
        this.allowedWorldNames = Set.copyOf(allowedWorldNames);
        if (this.allowedWorldNames.isEmpty()
            || this.allowedWorldNames.stream().anyMatch(name -> name == null || name.isBlank())) {
            throw new IllegalArgumentException("An exact Frontier world allowlist is required");
        }
    }

    public static FrontierWorldGate worldsBeyondDefault() {
        return new FrontierWorldGate(Set.of("frontier_iris"));
    }

    public boolean allows(String exactWorldName) {
        return exactWorldName != null && allowedWorldNames.contains(exactWorldName);
    }

    public void requireAllowed(String exactWorldName) {
        Objects.requireNonNull(exactWorldName, "exactWorldName");
        if (!allows(exactWorldName)) {
            throw new IllegalStateException("FRONTIER_WORLD_UNAVAILABLE");
        }
    }
}
