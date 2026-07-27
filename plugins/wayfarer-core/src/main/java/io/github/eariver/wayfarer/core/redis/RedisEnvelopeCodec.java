package io.github.eariver.wayfarer.core.redis;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Pattern;

final class RedisEnvelopeCodec {
    static final int VERSION = 1;
    private static final int MAX_PAYLOAD_BYTES = 8 * 1024;
    private static final Pattern SERVER_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final Pattern MESSAGE_TYPE = Pattern.compile("[A-Z][A-Z0-9_]{0,63}");
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    String encode(RedisEnvelope envelope) {
        validate(envelope);
        return String.join(
            ".",
            Integer.toString(envelope.version()),
            envelope.messageId().toString(),
            encodeText(envelope.originServerId()),
            encodeText(envelope.messageType()),
            Long.toString(envelope.occurredAt().toEpochMilli()),
            encodeText(envelope.payload())
        );
    }

    RedisEnvelope decode(String encoded) {
        try {
            String[] parts = encoded.split("\\.", -1);
            if (parts.length != 6) {
                invalid();
            }
            RedisEnvelope envelope = new RedisEnvelope(
                Integer.parseInt(parts[0]),
                UUID.fromString(parts[1]),
                decodeText(parts[2]),
                decodeText(parts[3]),
                decodeText(parts[5]),
                Instant.ofEpochMilli(Long.parseLong(parts[4]))
            );
            validate(envelope);
            return envelope;
        } catch (IllegalArgumentException failure) {
            throw new RedisRuntimeException("Redis message envelope is invalid");
        }
    }

    private static void validate(RedisEnvelope envelope) {
        if (envelope == null
            || envelope.version() != VERSION
            || envelope.messageId() == null
            || envelope.occurredAt() == null
            || !SERVER_ID.matcher(value(envelope.originServerId())).matches()
            || !MESSAGE_TYPE.matcher(value(envelope.messageType())).matches()) {
            invalid();
        }
        RedisDataValidator.safePayload(
            envelope.payload(),
            MAX_PAYLOAD_BYTES,
            "message payload"
        );
    }

    private static String encodeText(String value) {
        return ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeText(String value) {
        return new String(DECODER.decode(value), StandardCharsets.UTF_8);
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static void invalid() {
        throw new RedisRuntimeException("Redis message envelope is invalid");
    }
}
