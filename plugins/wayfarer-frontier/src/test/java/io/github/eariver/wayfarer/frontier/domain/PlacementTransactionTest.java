package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

final class PlacementTransactionTest {
    @Test
    void requiresCompensatableOrderedPlacement() {
        PlacementTransaction prepared = prepared();
        UUID launchpadId = UUID.randomUUID();
        PlacementTransaction committed = prepared
            .physicalBlockPlaced()
            .recordCommitted(launchpadId)
            .itemConsumed();

        assertEquals(PlacementTransaction.State.COMMITTED, committed.state());
        assertEquals(launchpadId, committed.launchpadId());
        assertThrows(IllegalStateException.class, () -> committed.compensated("DB_FAILURE"));
    }

    @Test
    void sanitizesFailureCodesAndPreservesUnknown() {
        PlacementTransaction unknown = prepared()
            .physicalBlockPlaced()
            .unknown("raw provider message!");
        assertEquals(PlacementTransaction.State.UNKNOWN, unknown.state());
        assertEquals("PLACEMENT_FAILURE", unknown.failureCode());
    }

    private static PlacementTransaction prepared() {
        return new PlacementTransaction(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new Launchpad.Location("frontier_iris", 1, 64, 2),
            PlacementTransaction.State.PREPARED,
            null,
            null
        );
    }
}
