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
            state.lastObservedFingerprint = NO_FINGERPRINT;
            state.lastVisibleManagedItems = 0;
            state.lastRequiredManagedItems = 0;
            state.timedOut = false;
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
            if (state.timedOut) {
                return Decision.TIMEOUT;
            }
            if (state.publicEventObserved) {
                return Decision.WAIT;
            }
            state.observations++;
            state.lastObservedFingerprint = fingerprint;
            state.lastVisibleManagedItems = managedItemCount;
            state.lastRequiredManagedItems = requiredManagedItems;
            if (managedItemCount < requiredManagedItems) {
                state.lastFingerprint = NO_FINGERPRINT;
                state.stableObservations = 0;
                if (state.observations >= MAX_FINGERPRINT_OBSERVATIONS) {
                    state.timedOut = true;
                    return Decision.TIMEOUT;
                }
                return Decision.WAIT;
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
                state.timedOut = true;
                return Decision.TIMEOUT;
            }
            return Decision.WAIT;
        }
    }

    /**
     * Returns the terminal bounded observation for a timed out request.
     * Player identity is intentionally not part of this evidence.
     */
    public Optional<TimeoutObservation> timeoutObservation(
        Request request,
        String source
    ) {
        Objects.requireNonNull(source, "source");
        State state = currentState(request);
        if (state == null) {
            return Optional.empty();
        }
        synchronized (state) {
            if (!state.timedOut) {
                return Optional.empty();
            }
            return Optional.of(new TimeoutObservation(
                source,
                request.generation(),
                state.observations,
                state.lastVisibleManagedItems,
                state.lastRequiredManagedItems,
                state.lastObservedFingerprint,
                Decision.TIMEOUT
            ));
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

    public record TimeoutObservation(
        String source,
        long generation,
        int pollCount,
        int visibleManagedItems,
        int requiredManagedItems,
        int fingerprint,
        Decision decision
    ) {
        public TimeoutObservation {
            Objects.requireNonNull(source, "source");
            if (source.isBlank() || generation <= 0 || pollCount <= 0
                || visibleManagedItems < 0 || requiredManagedItems < 0
                || decision != Decision.TIMEOUT) {
                throw new IllegalArgumentException(
                    "Invalid timeout observation"
                );
            }
        }
    }

    private static final class State {
        private long generation;
        private boolean publicEventObserved;
        private int observations;
        private int stableObservations;
        private int lastFingerprint = NO_FINGERPRINT;
        private int lastObservedFingerprint = NO_FINGERPRINT;
        private int lastVisibleManagedItems;
        private int lastRequiredManagedItems;
        private boolean timedOut;
    }
}
