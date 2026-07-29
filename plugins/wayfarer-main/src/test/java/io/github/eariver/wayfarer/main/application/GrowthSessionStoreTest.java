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

    private static GrowthTool tool() {
        return new GrowthTool(
            UUID.fromString("00000000-0000-0000-0000-000000000021"),
            OWNER,
            1,
            0,
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
