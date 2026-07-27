package io.github.eariver.wayfarer.core.redis;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisEnvelopeCodecTest {
    private static final UUID MESSAGE_ID = UUID.fromString(
        "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"
    );

    @Test
    void versionedEnvelopeRoundTripsAsImmutableSafeData() {
        RedisEnvelope envelope = new RedisEnvelope(
            1,
            MESSAGE_ID,
            "main-1",
            "CACHE_INVALIDATED",
            "{\"namespace\":\"waymark\",\"revision\":4}",
            Instant.parse("2026-07-28T00:00:00.123Z")
        );
        RedisEnvelopeCodec codec = new RedisEnvelopeCodec();

        assertEquals(envelope, codec.decode(codec.encode(envelope)));
    }

    @Test
    void invalidVersionAndSensitivePayloadFailClosed() {
        RedisEnvelopeCodec codec = new RedisEnvelopeCodec();
        assertThrows(
            RedisRuntimeException.class,
            () -> codec.encode(new RedisEnvelope(
                2,
                MESSAGE_ID,
                "main-1",
                "CACHE_INVALIDATED",
                "{}",
                Instant.EPOCH
            ))
        );
        assertThrows(
            RedisRuntimeException.class,
            () -> codec.encode(new RedisEnvelope(
                1,
                MESSAGE_ID,
                "main-1",
                "CACHE_INVALIDATED",
                "{\"endpoint\":\"redis://cache.invalid\"}",
                Instant.EPOCH
            ))
        );
        assertThrows(RedisRuntimeException.class, () -> codec.decode("not-an-envelope"));
    }
}
