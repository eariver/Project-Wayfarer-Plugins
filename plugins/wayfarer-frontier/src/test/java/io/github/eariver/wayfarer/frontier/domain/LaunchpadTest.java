package io.github.eariver.wayfarer.frontier.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class LaunchpadTest {
    private static final Instant START = Instant.parse("2026-07-30T00:00:00Z");

    @Test
    void countsOnlySuccessfulLaunchAndExtendsExpiry() {
        Launchpad initial = launchpad(0, 3, START.plus(Duration.ofDays(30)));

        Launchpad.UseResult sneaking =
            initial.use(START.plusSeconds(1), null, true, true, Duration.ofDays(30));
        Launchpad.UseResult unsafe =
            initial.use(START.plusSeconds(1), null, false, false, Duration.ofDays(30));
        Launchpad.UseResult launched =
            initial.use(START.plusSeconds(1), null, false, true, Duration.ofDays(30));

        assertEquals(Launchpad.Outcome.SNEAKING, sneaking.outcome());
        assertEquals(Launchpad.Outcome.UNSAFE, unsafe.outcome());
        assertEquals(0, sneaking.launchpad().successfulUseCount());
        assertEquals(Launchpad.Outcome.LAUNCHED, launched.outcome());
        assertEquals(1, launched.launchpad().successfulUseCount());
        assertEquals(START.plusSeconds(1).plus(Duration.ofDays(30)),
            launched.launchpad().expiresAt());
    }

    @Test
    void preservesExpiryWhenExtensionIsDisabled() {
        Instant originalExpiry = START.plus(Duration.ofDays(30));
        Launchpad initial = launchpad(0, 3, originalExpiry);

        Launchpad.UseResult launched = initial.use(
            START.plusSeconds(1),
            null,
            false,
            true,
            Duration.ofDays(30),
            false
        );

        assertEquals(Launchpad.Outcome.LAUNCHED, launched.outcome());
        assertEquals(originalExpiry, launched.launchpad().expiresAt());
    }

    @Test
    void exhaustsAtConfiguredSuccessfulUseLimit() {
        Launchpad beforeLast = launchpad(2, 3, START.plus(Duration.ofDays(30)));
        Launchpad.UseResult result =
            beforeLast.use(START.plusSeconds(1), null, false, true, Duration.ofDays(30));
        assertEquals(Launchpad.State.EXHAUSTED, result.launchpad().state());
        assertEquals(3, result.launchpad().successfulUseCount());
    }

    @Test
    void rejectsExpiredAndCooldownLaunches() {
        Launchpad expired = launchpad(0, 3, START);
        assertEquals(
            Launchpad.Outcome.UNAVAILABLE,
            expired.use(START, null, false, true, Duration.ofDays(30)).outcome()
        );
        Launchpad active = launchpad(0, 3, START.plusSeconds(60));
        assertEquals(
            Launchpad.Outcome.COOLDOWN,
            active.use(START, START.plusSeconds(2), false, true, Duration.ofDays(30)).outcome()
        );
    }

    private static Launchpad launchpad(int uses, int maxUses, Instant expiresAt) {
        return new Launchpad(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            new Launchpad.Location("frontier_iris", 1, 64, 2),
            90.0F,
            UUID.fromString("00000000-0000-0000-0000-000000000002"),
            uses,
            maxUses,
            START,
            null,
            expiresAt,
            "launchpad-v1",
            Launchpad.State.ACTIVE,
            1,
            0
        );
    }
}
