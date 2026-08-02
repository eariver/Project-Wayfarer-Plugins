≠rá^—f•ñÿ¶{O,y 'v√Æ∂õ≠package io.github.eariver.wayfarer.frontier.application;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tracks one bounded Safe Entry cycle per player and consumes at most one
 * late-MVI restart opportunity for that cycle.
 */
public final class EntryCycleRegistry {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentHashMap<UUID, Cycle> cycles = new ConcurrentHashMap<>();

    public long beginExternalEntry(UUID playerUuid) {
        long cycleId = sequence.incrementAndGet();
        cycles.put(playerUuid, new Cycle(cycleId, false));
        return cycleId;
    }

    public boolean isCurrent(UUID playerUuid, long cycleId) {
        Cycle cycle = cycles.get(playerUuid);
        return cycle != null && cycle.cycleId == cycleId;
    }

    public boolean consumeLateRestart(UUID playerUuid, long cycleId) {
        AtomicBoolean consumed = new AtomicBoolean(false);
        cycles.computeIfPresent(playerUuid, (ignored, cycle) -> {
            if (cycle.cycleId != cycleId || cycle.lateRestartConsumed) {
                return cycle;
            }
            consumed.set(true);
            return new Cycle(cycle.cycleId, true);
        });
        return consumed.get();
    }

    public void clear(UUID playerUuid, long cycleId) {
        cycles.computeIfPresent(playerUuid, (ignored, cycle) ->
            cycle.cycleId == cycleId ? null : cycle
        );
    }

    public void clear(UUID playerUuid) {
        cycles.remove(playerUuid);
    }

    public void clearAll() {
        cycles.clear();
    }

    private record Cycle(long cycleId, boolean lateRestartConsumed) {
    }
}
