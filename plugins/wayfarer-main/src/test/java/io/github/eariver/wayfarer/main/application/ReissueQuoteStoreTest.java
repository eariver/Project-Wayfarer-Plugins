package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.common.SingleUseGate;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

final class ReissueQuoteStoreTest {
    private static final UUID PLAYER = UUID.randomUUID();
    private static final UUID TOOL = UUID.randomUUID();
    private static final UUID INSTANCE = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-08-01T00:00:00Z");

    @Test
    void quoteIsSingleUse() {
        ReissueQuoteStore store = new ReissueQuoteStore();
        ReissueQuote quote = store.issue(quote(NOW.plusSeconds(60)));

        assertEquals(quote, store.consume(PLAYER, NOW).orElseThrow());
        assertTrue(store.consume(PLAYER, NOW.plusSeconds(1)).isEmpty());
    }

    @Test
    void expiredQuoteCannotBeConsumed() {
        ReissueQuoteStore store = new ReissueQuoteStore();
        store.issue(quote(NOW.plusSeconds(60)));

        assertTrue(store.consume(PLAYER, NOW.plusSeconds(60)).isEmpty());
    }

    @Test
    void issuingNewQuoteReplacesOldQuote() {
        ReissueQuoteStore store = new ReissueQuoteStore();
        ReissueQuote oldQuote = store.issue(quote(NOW.plusSeconds(60)));
        ReissueQuote newQuote = store.issue(quote(NOW.plusSeconds(120)));

        assertTrue(store.consume(PLAYER, oldQuote.quoteId(), NOW).isEmpty());
        assertEquals(newQuote, store.consume(PLAYER, newQuote.quoteId(), NOW).orElseThrow());
    }

    private static ReissueQuote quote(Instant expiresAt) {
        return new ReissueQuote(
            UUID.randomUUID(),
            PLAYER,
            TOOL,
            3,
            "main-test",
            INSTANCE,
            1,
            GrowthTool.DeliveryStatus.DELIVERED,
            363,
            expiresAt,
            new SingleUseGate()
        );
    }
}
