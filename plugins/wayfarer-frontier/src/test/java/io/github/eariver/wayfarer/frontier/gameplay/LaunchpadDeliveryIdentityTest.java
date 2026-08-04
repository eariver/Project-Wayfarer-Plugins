package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class LaunchpadDeliveryIdentityTest {
    @Test
    void unstackedLaunchpadsHaveStableDistinctPhysicalIdentities() {
        UUID deliveryId = UUID.randomUUID();

        UUID first = FrontierGameplayRuntime.launchpadItemInstance(
            deliveryId,
            0
        );
        UUID second = FrontierGameplayRuntime.launchpadItemInstance(
            deliveryId,
            1
        );

        assertEquals(
            first,
            FrontierGameplayRuntime.launchpadItemInstance(deliveryId, 0)
        );
        assertNotEquals(first, second);
    }
}
