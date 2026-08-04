package io.github.eariver.wayfarer.main.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.eariver.wayfarer.main.identity.GrowthToolPhysicalClaim.ItemType;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

final class GrowthToolDeliveryPresentationTest {
    @Test
    void usesExactNonItalicGrowthToolNames() {
        assertEquals(
            Component.text("Wayfarer Growth Pickaxe"),
            GrowthToolDeliveryPresentation.displayName(ItemType.GROWTH_TOOL)
        );
        assertEquals(
            Component.text("Broken Wayfarer Growth Pickaxe"),
            GrowthToolDeliveryPresentation.displayName(ItemType.BROKEN_GROWTH_TOOL)
        );
    }

    @Test
    void usesExactDurableDeliverySuccessMessage() {
        assertEquals(
            "[Wayfarer] Growth Tool「Wayfarer Growth Pickaxe」を受け取りました。",
            GrowthToolDeliveryPresentation.SUCCESS_MESSAGE
        );
    }

    @Test
    void unknownItemTypesHaveNoPresentationFallback() {
        assertEquals(null, GrowthToolDeliveryPresentation.displayName(null));
    }
}
