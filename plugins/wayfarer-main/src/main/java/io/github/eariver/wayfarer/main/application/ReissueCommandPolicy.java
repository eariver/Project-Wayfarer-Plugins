package io.github.eariver.wayfarer.main.application;

/** Pure permission boundary for Main paid reissue commands. */
public final class ReissueCommandPolicy {
    private ReissueCommandPolicy() {
    }

    public static boolean mayUsePlayerReissue(
        boolean isPlayer,
        boolean hasUsePermission
    ) {
        return isPlayer && hasUsePermission;
    }

    public static boolean mayUseAdminRecovery(boolean hasAdminPermission) {
        return hasAdminPermission;
    }
}
