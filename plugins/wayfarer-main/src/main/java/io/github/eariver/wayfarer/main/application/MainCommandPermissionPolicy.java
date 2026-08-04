package io.github.eariver.wayfarer.main.application;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Pure route-to-permission policy for the Main command surface. */
public final class MainCommandPermissionPolicy {
    public static final String USE = "wayfarer.main.use";
    public static final String ADMIN_READ = "wayfarer.main.admin.read";
    public static final String ADMIN_DELIVERY = "wayfarer.main.admin.delivery";
    public static final String ADMIN_MODIFY = "wayfarer.main.admin.modify";
    public static final String ADMIN_RECONCILE = "wayfarer.main.admin.reconcile";
    public static final String DEBUG = "wayfarer.main.debug";

    private MainCommandPermissionPolicy() {
    }

    public static Optional<String> requiredPermission(String[] arguments) {
        if (arguments == null || containsNull(arguments)) {
            return Optional.empty();
        }
        if (arguments.length == 0
            || (arguments.length == 1 && "status".equalsIgnoreCase(arguments[0]))) {
            return Optional.of(ADMIN_READ);
        }

        String command = arguments[0].toLowerCase(Locale.ROOT);
        return switch (command) {
            case "debug" -> arguments.length == 2
                ? Optional.of(DEBUG)
                : Optional.empty();
            case "tool" -> ReissueCommandParser.playerAction(arguments)
                    != ReissueCommandParser.PlayerAction.INVALID
                ? Optional.of(USE)
                : Optional.empty();
            case "repair" -> arguments.length == 1
                ? Optional.of(USE)
                : Optional.empty();
            case "branch" -> arguments.length == 2
                ? Optional.of(ADMIN_MODIFY)
                : Optional.empty();
            case "inspect" -> isInspectRoute(arguments)
                ? Optional.of(ADMIN_READ)
                : Optional.empty();
            case "grant", "delivery" -> arguments.length == 2
                ? Optional.of(ADMIN_DELIVERY)
                : Optional.empty();
            case "reissue" -> isConfirmed(arguments)
                ? Optional.of(ADMIN_DELIVERY)
                : Optional.empty();
            case "revoke" -> isConfirmed(arguments)
                ? Optional.of(ADMIN_MODIFY)
                : Optional.empty();
            case "reconcile" -> arguments.length == 2
                || ReissueCommandParser.recoveryRoute(arguments).isPresent()
                ? Optional.of(ADMIN_RECONCILE)
                : Optional.empty();
            default -> Optional.empty();
        };
    }

    public static boolean isAuthorized(
        String[] arguments,
        Predicate<String> permissionChecker
    ) {
        Objects.requireNonNull(permissionChecker, "permissionChecker");
        return requiredPermission(arguments)
            .map(permissionChecker::test)
            .orElse(false);
    }

    private static boolean isInspectRoute(String[] arguments) {
        return arguments.length == 3
            && ("tool".equalsIgnoreCase(arguments[1])
                || "reissue".equalsIgnoreCase(arguments[1])
                || "repair".equalsIgnoreCase(arguments[1]));
    }

    private static boolean isConfirmed(String[] arguments) {
        return arguments.length == 3
            && "confirm".equalsIgnoreCase(arguments[2]);
    }

    private static boolean containsNull(String[] arguments) {
        for (String argument : arguments) {
            if (argument == null) {
                return true;
            }
        }
        return false;
    }
}
