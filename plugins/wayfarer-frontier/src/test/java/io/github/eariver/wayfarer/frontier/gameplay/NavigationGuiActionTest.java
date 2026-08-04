package io.github.eariver.wayfarer.frontier.gameplay;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class NavigationGuiActionTest {
    @Test
    void mapsNavigationShopAndConfirmationSlots() {
        assertEquals(
            NavigationGuiAction.Action.OPEN_SHOP,
            NavigationGuiAction.main(10)
        );
        assertEquals(
            NavigationGuiAction.Action.OPEN_LOADOUT,
            NavigationGuiAction.main(13)
        );
        assertEquals(
            NavigationGuiAction.Action.WAYSTONE_UNAVAILABLE,
            NavigationGuiAction.main(22)
        );
        assertEquals(
            NavigationGuiAction.Action.SELECT_LAUNCHPAD,
            NavigationGuiAction.shop(11)
        );
        assertEquals(
            NavigationGuiAction.Action.SELECT_ROCKET,
            NavigationGuiAction.shop(15)
        );
        assertEquals(
            NavigationGuiAction.Action.CONFIRM_PURCHASE,
            NavigationGuiAction.purchaseConfirm(11)
        );
        assertEquals(
            NavigationGuiAction.Action.CANCEL,
            NavigationGuiAction.purchaseConfirm(15)
        );
        assertEquals(
            NavigationGuiAction.Action.NONE,
            NavigationGuiAction.main(27)
        );
    }
}
