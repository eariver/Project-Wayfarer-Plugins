package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class GrowthSessionStoreTest {
    private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final Instant NOW = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void accumulatesProgressInMemoryAndExposesDirtyCheckpointOnce() {
        GrowthSessionStore store = new GrowthSessionStore();
        store.open(tool());

        store.addProgress(OWNER, 250, NOW.plusSeconds(1));
        store.addProgress(OWNER, 750, NOW.plusSeconds(2));

        assertEquals(1000, store.current(OWNER).orElseThrow().cumulativeProgressUnits());
        assertEquals(1000, store.takeDirty(OWNER).orElseThrow().cumulativeProgressUnits());
        assertTrue(store.takeDirty(OWNER).isEmpty());
    }

    @Test
    void restoresDirtyFlagAfterFailedPersistence() {
        GrowthSessionStore store = new GrowthSessionStore();
        store.open(tool());
        GrowthTool changed = store.addProgress(OWNER, 1, NOW);
        store.takeDirty(OWNER).orElseThrow();
        store.restoreDirty(changed);
        assertTrue(store.takeDirty(OWNER).isPresent());
    }

    @Test
    void saturatesProgressAtLongMaxWithinSession() {
        GrowthSessionStore store = new GrowthSessionStore();
        store.open(tool(Long.MAX_VALUE - 100));

        store.addProgress(OWNER, 50, NOW.plusSeconds(1));
        assertEquals(
            Long.MAX_VALUE - 50,
            store.current(OWNER).orElseThrow().cumulativeProgressUnits()
        );

        store.addProgress(OWNER, 200, NOW.plusSeconds(2));
        assertEquals(
            Long.MAX_VALUE,
            store.current(OWNER).orElseThrow().cumulativeProgressUnits()
        );

        store.addProgress(OWNER, 1, NOW.plusSeconds(3));
        assertEquals(
            Long.MAX_VALUE,
            store.takeDirty(OWNER).orElseThrow().cumulativeProgressUnits()
        );
    }

    private static GrowthTool tool() {
        return tool(0);
    }

    private static GrowthTool tool(long progress) {
        return new GrowthTool(
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            OWNER,
            1,
            progress,
            GrowthTool.Branch.FORTUNE,
            GrowthTool.Status.ACTIVE,
            GrowthTool.DeliveryStatus.DELIVERED,
            0,
            1,
            1,
            0,
            NOW
        );
    }
}
