package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.GrowthTool;

/** Implemented by Phase 05; no Bukkit type crosses this boundary. */
public interface ReissueDeliveryGateway {
    DeliveryOutcome deliverReissued(GrowthTool rotatedTool);
}
