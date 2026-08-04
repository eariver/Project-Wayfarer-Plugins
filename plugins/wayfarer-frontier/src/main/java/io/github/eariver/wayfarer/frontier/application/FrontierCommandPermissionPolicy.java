package io.github.eariver.wayfarer.frontier.application;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/** Pure route-to-permission policy for the Frontier command surface. */
public final class FrontierCommandPermissionPolicy {
    public static final String USE = "wayfarer.frontier.use";
    public static final String ADMIN_READ = "wayfarer.frontier.admin.read";
    public static final String ADMIN_DELIVERY = "wayfarer.frontier.admin.delivery";
    public static final String ADMIN_LAUNCHPAD = "wayfarer.frontier.admin.launchpad";
    public static final String ADMIN_RECONCILE = "wayfarer.frontier.admin.reconcile";

    private FrontierCommandPermissionPolicy() {
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
            case "open" -> arguments.length == 1
                ? Optional.of(USE)
                : Optional.empty();
            case "shop" -> arguments.length == 2
                ? Optional.of(USE)
                : Optional.empty();
            case "loadout" -> loadoutPermission(arguments);
            case "delivery" -> deliveryPermission(arguments);
            case "launchpad" -> launchpadPermission(arguments);
            case "transaction", "audit" -> arguments.length == 3
                && "inspect".equalsIgnoreCase(arguments[1])
                ? Optional.of(ADMIN_READ)
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

    private static Optional<String> loadoutPermission(String[] arguments) {
        if (arguments.length == 3
            && "inspect".equalsIgnoreCase(arguments[1])) {
            return Optional.of(ADMIN_READ);
        }
        if (arguments.length == 5
            && "reissue".equalsIgnoreCase(arguments[1])
            && "confirm".equalsIgnoreCase(arguments[4])) {
            return Optional.of(ADMIN_DELIVERY);
        }
        return Optional.empty();
    }

    private static Optional<String> deliveryPermission(String[] arguments) {
        if (arguments.length != 3) {
            return Optional.empty();
        }
        return switch (arguments[1].toLowerCase(Locale.ROOT)) {
            case "inspect" -> Optional.of(ADMIN_READ);
            case "retry" -> Optional.of(ADMIN_DELIVERY);
            default -> Optional.empty();
        };
    }

    private static Optional<String> launchpadPermission(String[] arguments) {
        if (arguments.length == 3
            && "inspect".equalsIgnoreCase(arguments[1])) {
            return Optional.of(ADMIN_READ);
        }
        if (arguments.length == 4
            && "remove".equalsIgnoreCase(arguments[1])
            && "confirm".equalsIgnoreCase(arguments[3])) {
            return Optional.of(ADMIN_LAUNCHPAD);
        }
        if ((arguments.length == 3
                || arguments.length == 4
                    && "confirm".equalsIgnoreCase(arguments[3]))
            && "reconcile".equalsIgnoreCase(arguments[1])) {
            return Optional.of(ADMIN_RECONCILE);
        }
        return Optional.empty();
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
