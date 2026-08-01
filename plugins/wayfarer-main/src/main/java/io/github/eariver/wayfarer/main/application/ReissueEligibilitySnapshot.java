package io.github.eariver.wayfarer.main.application;

import java.util.Objects;
import java.util.UUID;

/** Immutable Main Thread snapshot passed into the Bukkit-independent saga. */
public record ReissueEligibilitySnapshot(
    UUID playerUuid,
    boolean playerOnline,
    String exactWorldName,
    boolean worldAllowed,
    PhysicalItemPresence physicalPresence
) {
    public ReissueEligibilitySnapshot {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(exactWorldName, "exactWorldName");
        Objects.requireNonNull(physicalPresence, "physicalPresence");
    }

    public boolean currentItemPresent() {
        return physicalPresence.anyPresent();
    }
}
