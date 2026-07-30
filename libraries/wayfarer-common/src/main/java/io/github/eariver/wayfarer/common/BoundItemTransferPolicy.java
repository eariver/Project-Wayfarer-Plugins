package io.github.eariver.wayfarer.common;

public final class BoundItemTransferPolicy {
    private BoundItemTransferPolicy() {}

    public static boolean denyContainerClick(
        boolean externalContainer,
        boolean currentBound,
        boolean cursorBound,
        boolean hotbarBound,
        boolean shiftClick,
        boolean clickedTop
    ) {
        return externalContainer
            && (currentBound || cursorBound || hotbarBound)
            && (clickedTop || shiftClick || cursorBound || hotbarBound);
    }

    public static boolean denyContainerDrag(
        boolean externalContainer,
        boolean cursorBound,
        boolean touchesTop
    ) {
        return externalContainer && cursorBound && touchesTop;
    }
}
