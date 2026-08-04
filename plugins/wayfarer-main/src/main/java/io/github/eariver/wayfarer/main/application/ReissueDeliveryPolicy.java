package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.util.Objects;
import java.util.UUID;

/** Pure delivery classification and rotated-authority identity policy. */
public final class ReissueDeliveryPolicy {
    private ReissueDeliveryPolicy() {
    }

    public static DeliveryOutcome classify(
        boolean accepting,
        boolean playerOnline,
        boolean worldAllowed,
        int currentItemCount,
        boolean inventoryHasSpace
    ) {
        if (!accepting) {
            return DeliveryOutcome.UNAVAILABLE;
        }
        if (!playerOnline) {
            return DeliveryOutcome.PLAYER_OFFLINE;
        }
        if (!worldAllowed) {
            return DeliveryOutcome.WORLD_NOT_ALLOWED;
        }
        if (currentItemCount < 0) {
            throw new IllegalArgumentException("Current item count cannot be negative");
        }
        if (currentItemCount == 1) {
            return DeliveryOutcome.ALREADY_PRESENT;
        }
        if (currentItemCount > 1) {
            return DeliveryOutcome.UNAVAILABLE;
        }
        return inventoryHasSpace
            ? DeliveryOutcome.DELIVERED
            : DeliveryOutcome.INVENTORY_FULL;
    }

    public static boolean createsOneItem(DeliveryOutcome outcome) {
        return outcome == DeliveryOutcome.DELIVERED;
    }

    public static ReissueDeliveryIdentity identity(GrowthTool rotatedTool) {
        Objects.requireNonNull(rotatedTool, "rotatedTool");
        return new ReissueDeliveryIdentity(
            rotatedTool.itemInstanceId(),
            rotatedTool.instanceEpoch(),
            rotatedTool.cumulativeProgressUnits(),
            rotatedTool.branch(),
            0
        );
    }

    public record ReissueDeliveryIdentity(
        UUID itemInstanceId,
        long instanceEpoch,
        long cumulativeProgressUnits,
        GrowthTool.Branch branch,
        int damage
    ) {
        public ReissueDeliveryIdentity {
            Objects.requireNonNull(itemInstanceId, "itemInstanceId");
            Objects.requireNonNull(branch, "branch");
            if (instanceEpoch < 1 || cumulativeProgressUnits < 0 || damage != 0) {
                throw new IllegalArgumentException("Reissue delivery identity is invalid");
            }
        }
    }
}
