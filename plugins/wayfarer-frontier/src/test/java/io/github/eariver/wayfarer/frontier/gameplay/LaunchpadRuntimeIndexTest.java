package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.frontier.domain.Launchpad;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class LaunchpadRuntimeIndexTest {
    @Test
    void protectsOnlyActiveLaunchpadCoordinates() {
        LaunchpadRuntimeIndex index = new LaunchpadRuntimeIndex();
        Launchpad launchpad = launchpad(10, 64, 20);
        Launchpad.Location ordinaryPlate = new Launchpad.Location(
            "frontier_iris",
            11,
            64,
            20
        );

        index.activate(launchpad);

        assertTrue(index.contains(launchpad.location()));
        assertFalse(index.contains(ordinaryPlate));
        assertTrue(index.deactivate(
            launchpad.location(),
            launchpad.launchpadId()
        ));
        assertFalse(index.contains(launchpad.location()));
    }

    private static Launchpad launchpad(int x, int y, int z) {
        Instant now = Instant.parse("2026-07-30T00:00:00Z");
        return new Launchpad(
            UUID.randomUUID(),
            new Launchpad.Location("frontier_iris", x, y, z),
            0,
            UUID.randomUUID(),
            0,
            3,
            now,
            null,
            now.plusSeconds(60),
            "frontier-v1",
            Launchpad.State.ACTIVE,
            1,
            0
        );
    }
}
