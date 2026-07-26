package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class HealthCommandHandler {
    public static final String PERMISSION = "wayfarer.admin.health";
    private static final String USAGE = "Usage: /wayfarer admin health";

    private final Supplier<WayfarerServices> services;
    private final String version;
    private final boolean playerDetails;
    private final OperationalEventSink events;

    public HealthCommandHandler(
        Supplier<WayfarerServices> services,
        String version,
        boolean playerDetails,
        OperationalEventSink events
    ) {
        this.services = Objects.requireNonNull(services, "services");
        this.version = Objects.requireNonNull(version, "version");
        this.playerDetails = playerDetails;
        this.events = Objects.requireNonNull(events, "events");
    }

    public boolean execute(CommandAudience audience, String[] arguments) {
        Objects.requireNonNull(audience, "audience");
        Objects.requireNonNull(arguments, "arguments");
        if (arguments.length != 2
            || !"admin".equalsIgnoreCase(arguments[0])
            || !"health".equalsIgnoreCase(arguments[1])) {
            audience.sendMessage(USAGE);
            return false;
        }
        if (!audience.hasPermission(PERMISSION)) {
            safeEvent("ADMIN_HEALTH_PERMISSION_DENIED");
            audience.sendMessage("You do not have permission to view Wayfarer health.");
            return true;
        }

        try {
            WayfarerServices current = services.get();
            WayfarerHealth.HealthSnapshot snapshot = current.health().snapshot();
            audience.sendMessage(
                "Wayfarer_Core " + version
                    + " | server=" + current.serverId()
                    + " | config=" + current.configVersion()
                    + " | lifecycle=" + snapshot.lifecycleState()
                    + " | overall=" + snapshot.overall()
            );
            boolean showDetails = audience.console() || playerDetails;
            snapshot.components().entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> audience.sendMessage(format(entry.getValue(), showDetails)));
            return true;
        } catch (RuntimeException failure) {
            safeEvent("ADMIN_HEALTH_COMMAND_FAILED");
            audience.sendMessage("Wayfarer health is currently unavailable.");
            return true;
        }
    }

    private void safeEvent(String eventType) {
        try {
            events.record(eventType);
        } catch (RuntimeException ignored) {
            // Command handling remains stable even before persistent audit exists.
        }
    }

    private static String format(WayfarerHealth.ComponentHealth component, boolean details) {
        String base = component.component() + ": " + component.status();
        return details && !component.detail().isBlank()
            ? base + " (" + component.detail() + ")"
            : base;
    }
}
