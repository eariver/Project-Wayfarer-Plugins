package io.github.eariver.wayfarer.frontier.application;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bounded, per-player gate for Frontier entry delivery. A public MVI share
 * event is treated as a pre-application signal; the caller must continue on
 * the next main-thread turn. Without that signal, a finite fingerprint
 * stabilization window is used and then fails closed.
 */
public final class SafeEntryReadiness {
    private static final int REQUIRED_STABLE_OBSERVATIONS = 2;
    public static final int MAX_FINGERPRINT_OBSERVATIONS = 40;
    private static final int NO_FINGERPRINT = Integer.MIN_VALUE;

    private final ConcurrentHashMap<UUID, State> states =
        new ConcurrentHashMap<>();

    public Request request(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        State state = states.computeIfAbsent(playerUuid, ignored -> new State());
        synchronized (state) {
            state.generation++;
            state.observations = 0;
            state.stableObservations = 0;
            state.lastFingerprint = NO_FINGERPRINT;
            return new Request(playerUuid, state.generation);
        }
    }

    public Request currentOrRequest(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        State state = states.computeIfAbsent(playerUuid, ignored -> new State());
        synchronized (state) {
            if (state.generation == 0) {
                state.generation = 1;
            }
            return new Request(playerUuid, state.generation);
        }
    }

    public void markPublicShareEvent(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        State state = states.computeIfAbsent(playerUuid, ignored -> new State());
        synchronized (state) {
            state.publicEventObserved = true;
        }
    }

    public boolean hasPublicShareEvent(Request request) {
        State state = currentState(request);
        if (state == null) {
            return false;
        }
        synchronized (state) {
            return state.publicEventObserved;
        }
    }

    public void clearPublicShareEvent(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        State state = states.get(playerUuid);
        if (state == null) {
            return;
        }
        synchronized (state) {
            state.publicEventObserved = false;
        }
    }

    public Decision continueAfterPublicShareEvent(
        Request request,
        boolean onlineInExactWorld
    ) {
        State state = currentState(request);
        if (state == null) {
            return Decision.SUPERSEDED;
        }
        if (!onlineInExactWorld) {
            return Decision.CANCELLED;
        }
        synchronized (state) {
            if (!state.publicEventObserved) {
                return Decision.WAIT;
            }
            state.publicEventObserved = false;
            return Decision.READY;
        }
    }

    public Decision observeFingerprint(
        Request request,
        boolean onlineInExactWorld,
        int fingerprint
    ) {
        return observeFingerprint(
            request,
            onlineInExactWorld,
            fingerprint,
            0,
            0
        );
    }

    /**
     * Observes a backend/MVI inventory fingerprint with an optional minimum
     * count of managed items that must be visible before the fingerprint can
     * stabilize. Existing durable active items make an empty restored profile
     * unsafe: it must remain bounded WAIT rather than being treated as a
     * legitimate empty first entry.
     */
    public Decision observeFingerprint(
        Request request,
        boolean onlineInExactWorld,
        int fingerprint,
        int managedItemCount,
        int requiredManagedItems
    ) {
        if (managedItemCount < 0 || requiredManagedItems < 0) {
            throw new IllegalArgumentException(
                "Managed item counts cannot be negative"
            );
        }
        State state = currentState(request);
        if (state == null) {
            return Decision.SUPERSEDED;
        }
        if (!onlineInExactWorld) {
            return Decision.CANCELLED;
        }
        synchronized (state) {
            if (state.publicEventObserved) {
                return Decision.WAIT;
            }
            state.observations++;
            if (managedItemCount < requiredManagedItems) {
                state.lastFingerprint = NO_FINGERPRINT;
                state.stableObservations = 0;
                return state.observations >= MAX_FINGERPRINT_OBSERVATIONS
                    ? Decision.TIMEOUT
                    : Decision.WAIT;
            }
            if (state.lastFingerprint == fingerprint) {
                state.stableObservations++;
            } else {
                state.lastFingerprint = fingerprint;
                state.stableObservations = 1;
            }
            if (state.stableObservations >= REQUIRED_STABLE_OBSERVATIONS) {
                return Decision.READY;
            }
            if (state.observations >= MAX_FINGERPRINT_OBSERVATIONS) {
                return Decision.TIMEOUT;
            }
            return Decision.WAIT;
        }
    }

    public boolean isCurrent(Request request) {
        return currentState(request) != null;
    }

    public void cancel(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        states.remove(playerUuid);
    }

    public void cancelAll() {
        states.clear();
    }

    public Optional<Request> current(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        State state = states.get(playerUuid);
        if (state == null) {
            return Optional.empty();
        }
        synchronized (state) {
            return state.generation == 0
                ? Optional.empty()
                : Optional.of(new Request(playerUuid, state.generation));
        }
    }

    private State currentState(Request request) {
        Objects.requireNonNull(request, "request");
        State state = states.get(request.playerUuid());
        if (state == null) {
            return null;
        }
        synchronized (state) {
            return state.generation == request.generation() ? state : null;
        }
    }

    public enum Decision {
        WAIT,
        READY,
        SUPERSEDED,
        CANCELLED,
        TIMEOUT
    }

    public record Request(UUID playerUuid, long generation) {
        public Request {
            Objects.requireNonNull(playerUuid, "playerUuid");
            if (generation <= 0) {
                throw new IllegalArgumentException("generation must be positive");
            }
        }
    }

    private static final class State {
        private long generation;
        private boolean publicEventObserved;
        private int observations;
        private int stableObservations;
        private int lastFingerprint = NO_FINGERPRINT;
    }
}
