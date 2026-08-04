package io.github.eariver.wayfarer.main.application;

/** Pure session-refresh decision for paid reissue command completion. */
public final class ReissueSessionPolicy {
    private ReissueSessionPolicy() {
    }

    public static boolean refreshAfterPaidResult(
        ReissueCoordinator.Status status
    ) {
        return status == ReissueCoordinator.Status.DELIVERED
            || status == ReissueCoordinator.Status.PENDING;
    }
}
