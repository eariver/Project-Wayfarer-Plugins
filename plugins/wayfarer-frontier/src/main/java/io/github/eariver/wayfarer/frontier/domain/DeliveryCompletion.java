package io.github.eariver.wayfarer.frontier.domain;

public enum DeliveryCompletion {
    TRANSITIONED_TO_DELIVERED,
    ALREADY_DELIVERED,
    CANCELLED,
    NOT_FOUND,
    STALE_IDENTITY,
    TRUE_CONFLICT,
    MALFORMED_PAYLOAD
}
