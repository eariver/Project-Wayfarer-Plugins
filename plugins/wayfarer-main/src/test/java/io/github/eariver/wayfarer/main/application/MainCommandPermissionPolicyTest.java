package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class MainCommandPermissionPolicyTest {
    private static final String UUID = "00000000-0000-0000-0000-000000000201";

    @Test
    void readRoutesUseAdminRead() {
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_READ,
            new String[] {}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_READ,
            new String[] {"status"}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_READ,
            new String[] {"inspect", "tool", UUID}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_READ,
            new String[] {"inspect", "reissue", UUID}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_READ,
            new String[] {"inspect", "repair", UUID}
        );
    }

    @Test
    void deliveryAndModifyRoutesUseTheirOwnLeaves() {
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_DELIVERY,
            new String[] {"grant", UUID}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_DELIVERY,
            new String[] {"delivery", UUID}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_DELIVERY,
            new String[] {"reissue", UUID, "confirm"}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_MODIFY,
            new String[] {"revoke", UUID, "confirm"}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_MODIFY,
            new String[] {"branch", "FORTUNE"}
        );
    }

    @Test
    void reconcilePlayerAndDebugRoutesRemainSeparated() {
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_RECONCILE,
            new String[] {"reconcile", UUID}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_RECONCILE,
            new String[] {"reconcile", UUID, "resume-payment", "confirm"}
        );
        assertRoute(
            MainCommandPermissionPolicy.ADMIN_RECONCILE,
            new String[] {
                "reconcile", UUID, "mark-failed", "CORE_FAILED", "confirm"
            }
        );
        assertRoute(
            MainCommandPermissionPolicy.USE,
            new String[] {"repair"}
        );
        assertRoute(
            MainCommandPermissionPolicy.USE,
            new String[] {"tool", "reissue"}
        );
        assertRoute(
            MainCommandPermissionPolicy.USE,
            new String[] {"tool", "reissue", "confirm"}
        );
        assertRoute(
            MainCommandPermissionPolicy.DEBUG,
            new String[] {"debug", "progress-next"}
        );
    }

    @Test
    void unrelatedLeavesCannotAuthorizeAnyAdminRoute() {
        String[][] routes = {
            {"inspect", "tool", UUID},
            {"delivery", UUID},
            {"revoke", UUID, "confirm"},
            {"reconcile", UUID, "resume-rotation", "confirm"}
        };
        String[] leaves = {
            MainCommandPermissionPolicy.ADMIN_READ,
            MainCommandPermissionPolicy.ADMIN_DELIVERY,
            MainCommandPermissionPolicy.ADMIN_MODIFY,
            MainCommandPermissionPolicy.ADMIN_RECONCILE,
            MainCommandPermissionPolicy.USE,
            MainCommandPermissionPolicy.DEBUG,
            "wayfarer.main.admin"
        };

        for (String[] route : routes) {
            String required = MainCommandPermissionPolicy.requiredPermission(route)
                .orElseThrow();
            for (String leaf : leaves) {
                assertEquals(
                    required.equals(leaf),
                    MainCommandPermissionPolicy.isAuthorized(route, leaf::equals),
                    route[0] + " with " + leaf
                );
            }
        }
    }

    @Test
    void malformedRoutesHaveNoPermissionDecisionOrSinkAuthorization() {
        String[][] malformed = {
            {"status", "extra"},
            {"inspect", "unknown", UUID},
            {"reissue", UUID},
            {"revoke", UUID, "no"},
            {"reconcile", UUID, "resume-payment", "confirm", "extra"},
            {"reconcile", UUID, "unknown", "confirm"},
            {"branch"},
            {"repair", "extra"},
            {"debug"}
        };

        for (String[] route : malformed) {
            AtomicInteger permissionChecks = new AtomicInteger();
            assertTrue(
                MainCommandPermissionPolicy.requiredPermission(route).isEmpty(),
                route[0]
            );
            assertFalse(
                MainCommandPermissionPolicy.isAuthorized(
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
    void deniedRoutesCannotAuthorizeACoordinatorSink() {
        AtomicInteger coordinatorCalls = new AtomicInteger();
        boolean allowed = MainCommandPermissionPolicy.isAuthorized(
            new String[] {"reconcile", UUID, "resume-payment", "confirm"},
            ignored -> false
        );
        if (allowed) {
            coordinatorCalls.incrementAndGet();
        }

        assertFalse(allowed);
        assertEquals(0, coordinatorCalls.get());
    }

    private static void assertRoute(String expected, String[] route) {
        assertEquals(Optional.of(expected),
            MainCommandPermissionPolicy.requiredPermission(route));
    }
}
