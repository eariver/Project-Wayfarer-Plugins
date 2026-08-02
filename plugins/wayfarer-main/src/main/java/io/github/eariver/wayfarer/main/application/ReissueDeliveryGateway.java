≠rá^—f•ñÿ¶{Oly 'v√Æ∂õ≠package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.GrowthTool;

/** Implemented by Phase 05; no Bukkit type crosses this boundary. */
public interface ReissueDeliveryGateway {
    DeliveryOutcome deliverReissued(GrowthTool rotatedTool);

    /** Called on Main after the durable reissue operation is DELIVERED. */
    default void notifyReissueDelivered(java.util.UUID playerUuid) {
        // Existing gateways may opt into the post-commit presentation boundary.
    }
}
