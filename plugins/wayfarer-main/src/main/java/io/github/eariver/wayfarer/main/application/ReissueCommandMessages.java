package io.github.eariver.wayfarer.main.application;

/** Pure player-facing result mapper for paid reissue commands. */
public final class ReissueCommandMessages {
    private ReissueCommandMessages() {
    }

    public static String quote(ReissueCoordinator.QuoteResult result) {
        if (result == null) {
            return unavailable();
        }
        if (result.status() == ReissueCoordinator.QuoteStatus.ISSUED
            && result.quote() != null) {
            return "Growth Tool reissue quote: "
                + result.quote().amountWaymark()
                + " WM; Evolution Count: "
                + result.quote().evolutionCount()
                + "; replacement is fully repaired. Confirm with "
                + "/wayfarer-main tool reissue confirm";
        }
        String failureCode = result.failureCode() == null
            ? ""
            : result.failureCode();
        return switch (failureCode) {
            case "PLAYER_OFFLINE" ->
                "You must be online to request a Growth Tool reissue quote.";
            case "WORLD_NOT_ALLOWED" ->
                "Growth Tool reissue is available only in a resource world.";
            case "CURRENT_ITEM_PRESENT" ->
                "Your current Growth Tool is present; paid reissue is unavailable.";
            case "DELIVERY_PENDING" ->
                "A free Growth Tool delivery is pending; free inventory space and rejoin.";
            default -> "Growth Tool reissue quote is unavailable.";
        };
    }

    public static String confirm(ReissueCoordinator.Result result) {
        if (result == null) {
            return unavailable();
        }
        return switch (result.status()) {
            case DELIVERED ->
                "Growth Tool reissue completed; the replacement is fully repaired.";
            case PENDING ->
                "Growth Tool reissue payment completed; free delivery is pending. "
                    + "Free inventory space and rejoin to retry delivery.";
            case UNKNOWN ->
                "Growth Tool reissue requires administrator review; no automatic retry will occur.";
            case FAILED ->
                "Growth Tool reissue did not complete; no further automatic payment will occur.";
            case REJECTED -> rejected(result.failureCode());
            case UNAVAILABLE -> unavailable();
        };
    }

    private static String rejected(String failureCode) {
        String safeFailureCode = failureCode == null ? "" : failureCode;
        return switch (safeFailureCode) {
            case "QUOTE_EXPIRED", "QUOTE_CHANGED" ->
                "The reissue quote changed or expired; request a new quote before confirming.";
            case "CURRENT_ITEM_PRESENT" ->
                "Your current Growth Tool is present; no WM was charged.";
            case "DELIVERY_PENDING" ->
                "A free Growth Tool delivery is pending; no WM was charged. Free inventory space and rejoin.";
            case "PLAYER_OFFLINE" ->
                "You must be online to confirm a Growth Tool reissue.";
            case "WORLD_NOT_ALLOWED" ->
                "Growth Tool reissue is available only in a resource world.";
            default -> "Growth Tool reissue was rejected; no WM was charged.";
        };
    }

    private static String unavailable() {
        return "Wayfarer Main reissue is unavailable.";
    }
}
