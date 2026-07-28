package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerServices;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class HealthCommandHandler {
    public static final String PERMISSION = "wayfarer.admin.health";
    private static final String USAGE = "Usage: /wayfarer admin health";

    private final Supplier<WayfarerServices> services;
    private final String version;
    private final boolean playerDetails;
    private final OperationalEventSink events;
    private final Consumer<String> warningSink;

    public HealthCommandHandler(
        Supplier<WayfarerServices> services,
        String version,
        boolean playerDetails,
        OperationalEventSink events
    ) {
        this(services, version, playerDetails, events, ignored -> {});
    }

    public HealthCommandHandler(
        Supplier<WayfarerServices> services,
        String version,
        boolean playerDetails,
        OperationalEventSink events,
        Consumer<String> warningSink
    ) {
        this.services = Objects.requireNonNull(services, "services");
        this.version = Objects.requireNonNull(version, "version");
        this.playerDetails = playerDetails;
        this.events = Objects.requireNonNull(events, "events");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
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
            audience.sendMessage("You do not have permission to view Wayfarer health.");
            safeEvent("ADMIN_HEALTH_PERMISSION_DENIED", audience);
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
            audience.sendMessage("Wayfarer health is currently unavailable.");
            safeEvent("ADMIN_HEALTH_COMMAND_FAILED", audience);
            return true;
        }
    }

    private void safeEvent(String eventType, CommandAudience audience) {
        try {
            OperationalEvent event = new OperationalEvent(
                eventType,
                audience.actorUuid().orElse(null),
                audience.audienceKind(),
                "ADMIN_COMMAND",
                "health",
                null
            );
            events.record(event).whenComplete((ignored, failure) -> {
                if (failure != null) {
                    warn("Wayfarer operational audit failed");
                }
            });
        } catch (RuntimeException failure) {
            warn("Wayfarer operational audit failed");
        }
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Command response must remain available.
        }
    }

    private static String format(WayfarerHealth.ComponentHealth component, boolean details) {
        String base = component.component() + ": " + component.status();
        return details && !component.detail().isBlank()
            ? base + " (" + component.detail() + ")"
            : base;
    }
}
