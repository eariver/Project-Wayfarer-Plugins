package io.github.eariver.wayfarer.core.task;

public record ShutdownResult(
    ShutdownStatus status,
    boolean terminated,
    int droppedTaskCount
) {
    public ShutdownResult {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        if (droppedTaskCount < 0) {
            throw new IllegalArgumentException("droppedTaskCount must not be negative");
        }
        if ((status == ShutdownStatus.GRACEFUL
            || status == ShutdownStatus.FORCED_TERMINATED) && !terminated) {
            throw new IllegalArgumentException(status + " requires confirmed termination");
        }
    }
}
