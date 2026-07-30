package io.github.eariver.wayfarer.frontier.gameplay;

final class NavigationGuiAction {
    enum Action {
        NONE,
        OPEN_SHOP,
        OPEN_LOADOUT,
        HELP,
        WAYSTONE_UNAVAILABLE,
        SELECT_LAUNCHPAD,
        SELECT_ROCKET,
        CONFIRM_PURCHASE,
        CANCEL
    }

    private NavigationGuiAction() {}

    static Action main(int rawSlot) {
        return switch (rawSlot) {
            case 10 -> Action.OPEN_SHOP;
            case 13 -> Action.OPEN_LOADOUT;
            case 16 -> Action.HELP;
            case 22 -> Action.WAYSTONE_UNAVAILABLE;
            default -> Action.NONE;
        };
    }

    static Action shop(int rawSlot) {
        return switch (rawSlot) {
            case 11 -> Action.SELECT_LAUNCHPAD;
            case 15 -> Action.SELECT_ROCKET;
            default -> Action.NONE;
        };
    }

    static Action purchaseConfirm(int rawSlot) {
        return switch (rawSlot) {
            case 11 -> Action.CONFIRM_PURCHASE;
            case 15 -> Action.CANCEL;
            default -> Action.NONE;
        };
    }
}
