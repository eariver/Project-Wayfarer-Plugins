package io.github.eariver.wayfarer.frontier.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class FrontierCommandPermissionPolicyTest {
    private static final String PLAYER = "00000000-0000-0000-0000-000000000201";
    private static final String LAUNCHPAD = "00000000-0000-0000-0000-000000000202";
    private static final String PURCHASE = "00000000-0000-0000-0000-000000000203";

    @Test
    void statusAndAllInspectionRoutesUseAdminRead() {
        String[][] routes = {
            {},
            {"status"},
            {"loadout", "inspect", PLAYER},
            {"delivery", "inspect", PLAYER},
            {"launchpad", "inspect", LAUNCHPAD},
            {"transaction", "inspect", PURCHASE},
            {"audit", "inspect", PURCHASE}
        };

        for (String[] route : routes) {
            assertRoute(FrontierCommandPermissionPolicy.ADMIN_READ, route);
        }
    }

    @Test
    void deliveryLaunchpadAndPlayerRoutesUseTheirOwnLeaves() {
        assertRoute(
            FrontierCommandPermissionPolicy.ADMIN_DELIVERY,
            new String[] {
                "loadout", "reissue", PLAYER, "elytra", "confirm"
            }
        );
        assertRoute(
            FrontierCommandPermissionPolicy.ADMIN_DELIVERY,
            new String[] {"delivery", "retry", PLAYER}
        );
        assertRoute(
            FrontierCommandPermissionPolicy.ADMIN_LAUNCHPAD,
            new String[] {"launchpad", "remove", LAUNCHPAD, "confirm"}
        );
        assertRoute(
            FrontierCommandPermissionPolicy.ADMIN_RECONCILE,
            new String[] {"launchpad", "reconcile", LAUNCHPAD}
        );
        assertRoute(
            FrontierCommandPermissionPolicy.ADMIN_RECONCILE,
            new String[] {"launchpad", "reconcile", LAUNCHPAD, "confirm"}
        );
        assertRoute(
            FrontierCommandPermissionPolicy.USE,
            new String[] {"open"}
        );
        assertRoute(
            FrontierCommandPermissionPolicy.USE,
            new String[] {"shop", "launchpad"}
        );
    }

    @Test
    void unrelatedLeavesCannotAuthorizeFrontierRoutes() {
        String[][] routes = {
            {"loadout", "inspect", PLAYER},
            {"delivery", "retry", PLAYER},
            {"launchpad", "remove", LAUNCHPAD, "confirm"},
            {"launchpad", "reconcile", LAUNCHPAD},
            {"transaction", "inspect", PURCHASE}
        };
        String[] leaves = {
            FrontierCommandPermissionPolicy.ADMIN_READ,
            FrontierCommandPermissionPolicy.ADMIN_DELIVERY,
            FrontierCommandPermissionPolicy.ADMIN_LAUNCHPAD,
            FrontierCommandPermissionPolicy.ADMIN_RECONCILE,
            FrontierCommandPermissionPolicy.USE,
            "wayfarer.frontier.admin"
        };

        for (String[] route : routes) {
            String required = FrontierCommandPermissionPolicy.requiredPermission(route)
                .orElseThrow();
            for (String leaf : leaves) {
                assertEquals(
                    required.equals(leaf),
                    FrontierCommandPermissionPolicy.isAuthorized(route, leaf::equals),
                    route[0] + " with " + leaf
                );
            }
        }
    }

    @Test
    void malformedRoutesHaveNoPermissionDecisionOrSinkAuthorization() {
        String[][] malformed = {
            {"status", "extra"},
            {"loadout", "unknown", PLAYER},
            {"delivery", "retry"},
            {"launchpad", "remove", LAUNCHPAD},
            {"launchpad", "remove", LAUNCHPAD, "no"},
            {"launchpad", "reconcile", LAUNCHPAD, "extra"},
            {"transaction", "reconcile", PURCHASE},
            {"open", "extra"},
            {"shop"}
        };

        for (String[] route : malformed) {
            AtomicInteger permissionChecks = new AtomicInteger();
            assertTrue(
                FrontierCommandPermissionPolicy.requiredPermission(route).isEmpty(),
                route[0]
            );
            assertFalse(
                FrontierCommandPermissionPolicy.isAuthorized(
                    route,
                    ignored -> {
                        permissionChecks.incrementAndGet();
                        return true;
                    }
                )
            );
            assertEquals(0, permissionChecks.get(), route[0]);
        }
    }

    @Test
    void deniedMutationCannotAuthorizeAFrontierSink() {
        AtomicInteger sinkCalls = new AtomicInteger();
        boolean allowed = FrontierCommandPermissionPolicy.isAuthorized(
            new String[] {"launchpad", "remove", LAUNCHPAD, "confirm"},
            ignored -> false
        );
        if (allowed) {
            sinkCalls.incrementAndGet();
        }

        assertFalse(allowed);
        assertEquals(0, sinkCalls.get());
    }

    private static void assertRoute(String expected, String[] route) {
        assertEquals(Optional.of(expected),
            FrontierCommandPermissionPolicy.requiredPermission(route));
    }
}
