package io.github.eariver.wayfarer.frontier.domain;

public enum DeathPersistResult {
    PENDING_CREATED,
    REOPENED_TO_PENDING,
    ALREADY_PENDING,
    CANCELLED_OR_OBSOLETE,
    STALE_SKIPPED,
    CONFLICT,
    NOT_FOUND_LOGICAL
}
