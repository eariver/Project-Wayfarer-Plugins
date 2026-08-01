package io.github.eariver.wayfarer.frontier.application;

/**
 * Pure retry classification for admin delivery retry. Does not touch Bukkit.
 */
public final class DeliveryRetryClassification {
    private DeliveryRetryClassification() {}

    public enum Kind {
        SHUTDOWN_UNAVAILABLE,
        PLAYER_OFFLINE,
        PROCEED_SAFE_ENTRY
    }

    public static Kind classify(boolean accepting, boolean playerOnline) {
        if (!accepting) {
            return Kind.SHUTDOWN_UNAVAILABLE;
        }
        if (!playerOnline) {
            return Kind.PLAYER_OFFLINE;
        }
        return Kind.PROCEED_SAFE_ENTRY;
    }

    public static TraversalDeliveryCoordinator.Result terminalResult(Kind kind) {
        return switch (kind) {
            case SHUTDOWN_UNAVAILABLE ->
                TraversalDeliveryCoordinator.Result.unavailable();
            case PLAYER_OFFLINE ->
                TraversalDeliveryCoordinator.Result.offlineOnly();
            case PROCEED_SAFE_ENTRY ->
                throw new IllegalArgumentException(
                    "PROCEED_SAFE_ENTRY is not a terminal result"
                );
        };
    }

    public static boolean playerNotificationProhibited(Kind kind) {
        return kind == Kind.PLAYER_OFFLINE || kind == Kind.SHUTDOWN_UNAVAILABLE;
    }
}
