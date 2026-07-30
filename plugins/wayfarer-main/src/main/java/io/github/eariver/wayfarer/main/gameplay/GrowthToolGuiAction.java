package io.github.eariver.wayfarer.main.gameplay;

final class GrowthToolGuiAction {
    enum Action {
        NONE,
        OPEN_REPAIR_PREVIEW,
        HELP,
        CONFIRM_REPAIR,
        CANCEL
    }

    private GrowthToolGuiAction() {}

    static Action main(int rawSlot) {
        return switch (rawSlot) {
            case 14 -> Action.OPEN_REPAIR_PREVIEW;
            case 16 -> Action.HELP;
            default -> Action.NONE;
        };
    }

    static Action repairPreview(int rawSlot) {
        return switch (rawSlot) {
            case 11 -> Action.CONFIRM_REPAIR;
            case 15 -> Action.CANCEL;
            default -> Action.NONE;
        };
    }
}
