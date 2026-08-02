­r‡^Ñf¥–Ø¦{OlyÊ'vÃ®¶›­package io.github.eariver.wayfarer.frontier.gameplay;

import io.github.eariver.wayfarer.frontier.domain.TraversalIdentity.ItemType;
import net.kyori.adventure.text.Component;

/** Stable Frontier loadout presentation, independent of PDC identity. */
public final class TraversalItemPresentation {
    private TraversalItemPresentation() {
    }

    public static Component displayName(ItemType itemType) {
        if (itemType == null) {
            return null;
        }
        return switch (itemType) {
            case ELYTRA -> Component.text("Beyond Wayfarer Elytra");
            case GRAPPLING_HOOK -> Component.text("Beyond Wayfarer Grappling Hook");
            case NAVIGATION -> Component.text("Beyond Wayfarer Navigation");
        };
    }

    public static Component launchpadName() {
        return Component.text("Beyond Wayfarer Launchpad");
    }

    public static Component rocketName() {
        return Component.text("Beyond Wayfarer Rocket");
    }
}
