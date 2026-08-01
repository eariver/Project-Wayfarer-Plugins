package io.github.eariver.wayfarer.main.application;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/** Pure parser for the narrow Main reissue command surface. */
public final class ReissueCommandParser {
    private ReissueCommandParser() {
    }

    public static PlayerAction playerAction(String[] arguments) {
        if (arguments == null) {
            return PlayerAction.INVALID;
        }
        if (arguments.length == 2
            && "tool".equalsIgnoreCase(arguments[0])
            && "reissue".equalsIgnoreCase(arguments[1])) {
            return PlayerAction.QUOTE;
        }
        if (arguments.length == 3
            && "tool".equalsIgnoreCase(arguments[0])
            && "reissue".equalsIgnoreCase(arguments[1])
            && "confirm".equalsIgnoreCase(arguments[2])) {
            return PlayerAction.CONFIRM;
        }
        return PlayerAction.INVALID;
    }

    public static Optional<UUID> inspectReissue(String[] arguments) {
        if (arguments == null || arguments.length != 3
            || !"inspect".equalsIgnoreCase(arguments[0])
            || !"reissue".equalsIgnoreCase(arguments[1])) {
            return Optional.empty();
        }
        return parseUuid(arguments[2]);
    }

    public static Optional<RecoveryRoute> recoveryRoute(String[] arguments) {
        if (arguments == null || arguments.length < 2
            || !"reconcile".equalsIgnoreCase(arguments[0])) {
            return Optional.empty();
        }
        Optional<UUID> reissueId = parseUuid(arguments[1]);
        if (reissueId.isEmpty() || arguments.length < 4) {
            return Optional.empty();
        }
        String action = arguments[2].toLowerCase(Locale.ROOT);
        if (!"confirm".equalsIgnoreCase(arguments[arguments.length - 1])) {
            return Optional.empty();
        }
        return switch (action) {
            case "confirm-payment" -> arguments.length == 4
                ? Optional.of(new RecoveryRoute(
                    reissueId.orElseThrow(),
                    RecoveryAction.CONFIRM_PAYMENT,
                    null
                ))
                : Optional.empty();
            case "resume-payment" -> arguments.length == 4
                ? Optional.of(new RecoveryRoute(
                    reissueId.orElseThrow(),
                    RecoveryAction.RESUME_PAYMENT,
                    null
                ))
                : Optional.empty();
            case "resume-rotation" -> arguments.length == 4
                ? Optional.of(new RecoveryRoute(
                    reissueId.orElseThrow(),
                    RecoveryAction.RESUME_ROTATION,
                    null
                ))
                : Optional.empty();
            case "mark-failed" -> arguments.length == 5
                ? Optional.of(new RecoveryRoute(
                    reissueId.orElseThrow(),
                    RecoveryAction.MARK_FAILED,
                    sanitizeFailureCode(arguments[3])
                ))
                : Optional.empty();
            default -> Optional.empty();
        };
    }

    public static String sanitizeFailureCode(String failureCode) {
        return failureCode != null && failureCode.matches("[A-Z0-9_]{3,96}")
            ? failureCode
            : "ADMIN_FAILED";
    }

    private static Optional<UUID> parseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException failure) {
            return Optional.empty();
        }
    }

    public enum PlayerAction {
        QUOTE,
        CONFIRM,
        INVALID
    }

    public enum RecoveryAction {
        CONFIRM_PAYMENT,
        RESUME_PAYMENT,
        RESUME_ROTATION,
        MARK_FAILED
    }

    public record RecoveryRoute(
        UUID reissueId,
        RecoveryAction action,
        String sanitizedFailureCode
    ) {
    }
}
