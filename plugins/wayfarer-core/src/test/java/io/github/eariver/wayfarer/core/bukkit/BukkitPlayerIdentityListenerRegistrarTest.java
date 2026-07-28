package io.github.eariver.wayfarer.core.bukkit;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BukkitPlayerIdentityListenerRegistrarTest {
    @Test
    void snapshotsOnlyImmutableJdkIdentityValues() {
        UUID playerUuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Instant observedAt = Instant.parse("2026-07-27T12:00:00Z");

        var observation = BukkitPlayerIdentityListenerRegistrar.snapshot(
            playerUuid,
            "PlayerOne",
            "test-server",
            observedAt
        );

        assertEquals(playerUuid, observation.playerUuid());
        assertEquals("PlayerOne", observation.lastKnownName());
        assertEquals("test-server", observation.serverId());
        assertEquals(observedAt, observation.observedAt());
    }
}
