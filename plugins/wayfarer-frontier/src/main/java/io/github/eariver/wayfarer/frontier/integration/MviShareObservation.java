package io.github.eariver.wayfarer.frontier.integration;

import org.bukkit.entity.Player;

/**
 * Sanitized observation contract between the optional MVI adapter and the
 * Frontier runtime. It contains no MVI API types, preserving the soft
 * dependency boundary when MVI is absent.
 */
public record MviShareObservation(
    Player player,
    String eventName,
    String fromWorld,
    String toWorld,
    int writeProfileCount,
    int readProfileCount,
    boolean cancelled,
    boolean monitorPhase
) {}
