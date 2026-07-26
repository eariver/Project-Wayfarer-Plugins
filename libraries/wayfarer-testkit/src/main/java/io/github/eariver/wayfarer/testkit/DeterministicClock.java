package io.github.eariver.wayfarer.testkit;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public final class DeterministicClock extends Clock {
    private Instant instant;
    private final ZoneId zone;

    public DeterministicClock(Instant instant) {
        this(instant, ZoneId.of("UTC"));
    }

    public DeterministicClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public void set(Instant instant) {
        this.instant = instant;
    }

    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId zone) { return new DeterministicClock(instant, zone); }
    @Override public Instant instant() { return instant; }
}
