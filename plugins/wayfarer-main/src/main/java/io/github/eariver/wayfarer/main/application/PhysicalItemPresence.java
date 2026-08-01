package io.github.eariver.wayfarer.main.application;

public record PhysicalItemPresence(
    boolean storage,
    boolean armor,
    boolean offhand,
    boolean cursor
) {
    public boolean anyPresent() {
        return storage || armor || offhand || cursor;
    }
}
