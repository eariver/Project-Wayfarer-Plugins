package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.common.SingleUseGate;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Short-lived, process-local quote bound to the current tool authority. */
public record ReissueQuote(
    UUID quoteId,
    UUID playerUuid,
    UUID toolId,
    int evolutionCount,
    String configRevision,
    UUID currentItemInstanceId,
    long currentInstanceEpoch,
    GrowthTool.DeliveryStatus currentDeliveryStatus,
    long amountWaymark,
    Instant expiresAt,
    SingleUseGate singleUseGate
) {
    public ReissueQuote {
        Objects.requireNonNull(quoteId, "quoteId");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(toolId, "toolId");
        Objects.requireNonNull(configRevision, "configRevision");
        Objects.requireNonNull(currentItemInstanceId, "currentItemInstanceId");
        Objects.requireNonNull(currentDeliveryStatus, "currentDeliveryStatus");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(singleUseGate, "singleUseGate");
        if (evolutionCount < 0 || currentInstanceEpoch < 1 || amountWaymark <= 0
            || configRevision.isBlank()) {
            throw new IllegalArgumentException("Reissue quote is invalid");
        }
    }

    public boolean matches(
        GrowthTool tool,
        int currentEvolutionCount,
        String currentConfigRevision,
        Instant now
    ) {
        return matchesSnapshot(
            tool,
            currentEvolutionCount,
            currentConfigRevision,
            amountWaymark,
            now
        );
    }

    public boolean matchesSnapshot(
        GrowthTool tool,
        int currentEvolutionCount,
        String currentConfigRevision,
        long currentAmountWaymark,
        Instant now
    ) {
        Objects.requireNonNull(tool, "tool");
        Objects.requireNonNull(currentConfigRevision, "currentConfigRevision");
        Objects.requireNonNull(now, "now");
        return now.isBefore(expiresAt)
            && playerUuid.equals(tool.ownerUuid())
            && toolId.equals(tool.toolId())
            && evolutionCount == currentEvolutionCount
            && configRevision.equals(currentConfigRevision)
            && currentItemInstanceId.equals(tool.itemInstanceId())
            && currentInstanceEpoch == tool.instanceEpoch()
            && currentDeliveryStatus == tool.deliveryStatus()
            && amountWaymark == currentAmountWaymark;
    }
}
