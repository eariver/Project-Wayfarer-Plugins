package io.github.eariver.wayfarer.main.gameplay;

import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim.ItemType;
import net.kyori.adventure.text.Component;

/** Stable presentation strings kept separate from physical identity. */
public final class GrowthToolDeliveryPresentation {
    public static final String SUCCESS_MESSAGE =
        "[Wayfarer] Growth Tool「Wayfarer Growth Pickaxe」を受け取りました。";

    private GrowthToolDeliveryPresentation() {
    }

    public static Component displayName(ItemType itemType) {
        if (itemType == null) {
            return null;
        }
        return switch (itemType) {
            case GROWTH_TOOL -> Component.text("Wayfarer Growth Pickaxe");
            case BROKEN_GROWTH_TOOL -> Component.text("Broken Wayfarer Growth Pickaxe");
        };
    }
}
