package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HealthCommandHandlerTest {
    @Test
    void authorizedHealthCommandShowsSummary() {
        FakeAudience audience = new FakeAudience(true, false);
        handler(services(), false, ignored -> {}).execute(
            audience,
            new String[] {"admin", "health"}
        );
        assertTrue(audience.messages.getFirst().contains("overall=DEGRADED"));
        assertTrue(audience.messages.getFirst().contains("server=test-server"));
    }

    @Test
    void permissionDenialIsStableAndObserved() {
        List<String> events = new ArrayList<>();
        FakeAudience audience = new FakeAudience(false, false);
        boolean handled = handler(services(), false, events::add).execute(
            audience,
            new String[] {"admin", "health"}
        );
        assertTrue(handled);
        assertTrue(audience.messages.getFirst().contains("permission"));
        assertTrue(events.contains("ADMIN_HEALTH_PERMISSION_DENIED"));
    }

    @Test
    void consoleReceivesSanitizedComponentDetails() {
        FakeAudience audience = new FakeAudience(true, true);
        handler(services(), false, ignored -> {}).execute(
            audience,
            new String[] {"admin", "health"}
        );
        assertTrue(audience.messages.stream().anyMatch(
            message -> message.contains("[REDACTED]")
        ));
        assertFalse(audience.messages.stream().anyMatch(
            message -> message.contains("raw-secret")
        ));
    }

    @Test
    void playerDoesNotReceiveInternalDetailsByDefault() {
        FakeAudience audience = new FakeAudience(true, false);
        handler(services(), false, ignored -> {}).execute(
            audience,
            new String[] {"admin", "health"}
        );
        assertFalse(audience.messages.stream().skip(1).anyMatch(
            message -> message.contains("(")
        ));
    }

    @Test
    void unknownSubcommandReturnsUsage() {
        FakeAudience audience = new FakeAudience(true, true);
        boolean handled = handler(services(), false, ignored -> {}).execute(
            audience,
            new String[] {"admin", "unknown"}
        );
        assertFalse(handled);
        assertTrue(audience.messages.getFirst().startsWith("Usage:"));
    }

    @Test
    void commandFailureDoesNotEscape() {
        List<String> events = new ArrayList<>();
        FakeAudience audience = new FakeAudience(true, true);
        boolean handled = handler(
            () -> {
                throw new IllegalStateException("raw-secret");
            },
            false,
            events::add
        ).execute(audience, new String[] {"admin", "health"});
        assertTrue(handled);
        assertTrue(audience.messages.getFirst().contains("unavailable"));
        assertFalse(audience.messages.getFirst().contains("raw-secret"));
        assertTrue(events.contains("ADMIN_HEALTH_COMMAND_FAILED"));
    }

    @Test
    void consoleIsStillSubjectToPermissionContract() {
        FakeAudience audience = new FakeAudience(false, true);
        handler(services(), false, ignored -> {}).execute(
            audience,
            new String[] {"admin", "health"}
        );
        assertTrue(audience.messages.getFirst().contains("permission"));
    }

    private static HealthCommandHandler handler(
        WayfarerServices services,
        boolean playerDetails,
        OperationalEventSink events
    ) {
        return handler(() -> services, playerDetails, events);
    }

    private static HealthCommandHandler handler(
        java.util.function.Supplier<WayfarerServices> services,
        boolean playerDetails,
        OperationalEventSink events
    ) {
        return new HealthCommandHandler(services, "0.0.1-alpha.1", playerDetails, events);
    }

    private static WayfarerServices services() {
        Instant now = Instant.parse("2026-07-26T00:00:00Z");
        Map<String, WayfarerHealth.ComponentHealth> components = new LinkedHashMap<>();
        components.put(
            "Config",
            new WayfarerHealth.ComponentHealth(
                "Config",
                WayfarerHealth.Status.UP,
                now,
                "validated"
            )
        );
        components.put(
            "MariaDB",
            new WayfarerHealth.ComponentHealth(
                "MariaDB",
                WayfarerHealth.Status.UNKNOWN,
                now,
                "credential=[REDACTED]"
            )
        );
        WayfarerHealth health = () -> new WayfarerHealth.HealthSnapshot(
            WayfarerHealth.Status.DEGRADED,
            now,
            WayfarerLifecycleState.ENABLED,
            components
        );
        WayfarerServices services = mock(WayfarerServices.class);
        when(services.serverId()).thenReturn("test-server");
        when(services.configVersion()).thenReturn(1);
        when(services.health()).thenReturn(health);
        return services;
    }

    private static final class FakeAudience implements CommandAudience {
        private final boolean permitted;
        private final boolean console;
        private final List<String> messages = new ArrayList<>();

        private FakeAudience(boolean permitted, boolean console) {
            this.permitted = permitted;
            this.console = console;
        }

        @Override
        public boolean hasPermission(String permission) {
            return permitted && HealthCommandHandler.PERMISSION.equals(permission);
        }

        @Override
        public boolean console() {
            return console;
        }

        @Override
        public void sendMessage(String message) {
            messages.add(message);
        }
    }
}
