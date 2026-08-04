package io.github.eariver.wayfarer.main.application;

import java.util.UUID;

/** Implemented by Phase 05 runtime code on the Bukkit Main Thread. */
public interface ReissueEligibilityPort {
    ReissueEligibilitySnapshot snapshot(UUID playerUuid);
}
