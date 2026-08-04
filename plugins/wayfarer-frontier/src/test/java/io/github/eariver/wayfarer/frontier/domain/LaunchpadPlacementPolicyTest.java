package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class LaunchpadPlacementPolicyTest {
    private final LaunchpadPlacementPolicy policy =
        new LaunchpadPlacementPolicy(FrontierWorldGate.worldsBeyondDefault());

    @Test
    void permitsOnlyFullyValidatedSnapshot() {
        assertEquals(LaunchpadPlacementPolicy.Result.ALLOWED, policy.validate(valid()));
        assertEquals(
            LaunchpadPlacementPolicy.Result.WORLD_UNAVAILABLE,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris_nether", true, true, true, false, true,
                false, false, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.TARGET_NOT_AIR,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, false, true, false, true,
                false, false, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.REGION_DENIED,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, true, true, false, true,
                false, true, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.INVALID_SUPPORT,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, true, false, false, true,
                false, false, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.LIQUID,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, true, true, true, true,
                false, false, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.OUTSIDE_BORDER,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, true, true, false, false,
                false, false, false, false
            ))
        );
        assertEquals(
            LaunchpadPlacementPolicy.Result.LAUNCHPAD_OVERLAP,
            policy.validate(new LaunchpadPlacementPolicy.Snapshot(
                "frontier_iris", true, true, true, false, true,
                false, false, false, true
            ))
        );
    }

    private static LaunchpadPlacementPolicy.Snapshot valid() {
        return new LaunchpadPlacementPolicy.Snapshot(
            "frontier_iris",
            true,
            true,
            true,
            false,
            true,
            false,
            false,
            false,
            false
        );
    }
}
