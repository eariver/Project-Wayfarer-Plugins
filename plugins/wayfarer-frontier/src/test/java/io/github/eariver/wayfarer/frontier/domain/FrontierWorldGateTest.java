package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class FrontierWorldGateTest {
    private final FrontierWorldGate gate = FrontierWorldGate.worldsBeyondDefault();

    @Test
    void acceptsOnlyExactOverworldName() {
        assertTrue(gate.allows("frontier_iris"));
        assertFalse(gate.allows("frontier_iris_nether"));
        assertFalse(gate.allows("frontier_iris_the_end"));
        assertFalse(gate.allows("frontier_iris_copy"));
        assertFalse(gate.allows("FRONTIER_IRIS"));
        assertFalse(gate.allows(null));
        assertThrows(IllegalStateException.class, () -> gate.requireAllowed("unknown"));
    }
}
