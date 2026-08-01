package io.github.eariver.wayfarer.main.application;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Process-local quote store.  Durable payment state starts only after a
 * quote has been consumed and a REISSUE operation has been prepared.
 */
public final class ReissueQuoteStore {
    private final ConcurrentHashMap<UUID, ReissueQuote> currentByPlayer =
        new ConcurrentHashMap<>();

    public ReissueQuote issue(ReissueQuote quote) {
        Objects.requireNonNull(quote, "quote");
        currentByPlayer.put(quote.playerUuid(), quote);
        return quote;
    }

    public Optional<ReissueQuote> current(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        return Optional.ofNullable(currentByPlayer.get(playerUuid));
    }

    public Optional<ReissueQuote> consume(UUID playerUuid, Instant now) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(now, "now");
        AtomicReference<ReissueQuote> accepted = new AtomicReference<>();
        currentByPlayer.compute(playerUuid, (ignored, quote) -> {
            if (quote == null || !now.isBefore(quote.expiresAt())) {
                return null;
            }
            if (quote.singleUseGate().tryAcquire()) {
                accepted.set(quote);
            }
            return quote;
        });
        return Optional.ofNullable(accepted.get());
    }

    /** Compatibility overload for callers that carry the quote token. */
    public Optional<ReissueQuote> consume(
        UUID playerUuid,
        UUID quoteId,
        Instant now
    ) {
        Objects.requireNonNull(quoteId, "quoteId");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(now, "now");
        AtomicReference<ReissueQuote> accepted = new AtomicReference<>();
        currentByPlayer.compute(playerUuid, (ignored, quote) -> {
            if (quote == null || !quoteId.equals(quote.quoteId())
                || !now.isBefore(quote.expiresAt())) {
                return quote == null || quoteId.equals(
                    quote == null ? null : quote.quoteId()
                ) ? null : quote;
            }
            if (quote.singleUseGate().tryAcquire()) {
                accepted.set(quote);
            }
            return quote;
        });
        return Optional.ofNullable(accepted.get());
    }
}
