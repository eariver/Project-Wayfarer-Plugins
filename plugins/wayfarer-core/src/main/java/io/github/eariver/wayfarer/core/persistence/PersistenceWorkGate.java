package io.github.eariver.wayfarer.core.persistence;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class PersistenceWorkGate {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();
    private State state = State.OPEN;
    private int inFlight;
    private PersistenceDrainResult terminalResult;

    Permit acquire() {
        lock.lock();
        try {
            if (state != State.OPEN) {
                throw new RejectedExecutionException("MariaDB work intake is stopping");
            }
            inFlight++;
            return new Permit(this);
        } finally {
            lock.unlock();
        }
    }

    PersistenceDrainResult stopAcceptingAndAwait(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must be positive");
        }

        lock.lock();
        try {
            if (terminalResult != null) {
                return terminalResult;
            }
            if (state == State.OPEN) {
                state = State.DRAINING;
                stateChanged.signalAll();
            }

            long remainingNanos = timeout.toNanos();
            while (inFlight > 0 && terminalResult == null) {
                if (remainingNanos <= 0) {
                    return finish(PersistenceDrainStatus.TIMED_OUT);
                }
                try {
                    remainingNanos = stateChanged.awaitNanos(remainingNanos);
                } catch (InterruptedException interruption) {
                    PersistenceDrainResult result =
                        finish(PersistenceDrainStatus.INTERRUPTED);
                    Thread.currentThread().interrupt();
                    return result;
                }
            }
            if (terminalResult != null) {
                return terminalResult;
            }
            return finish(PersistenceDrainStatus.DRAINED);
        } finally {
            lock.unlock();
        }
    }

    boolean isAccepting() {
        lock.lock();
        try {
            return state == State.OPEN;
        } finally {
            lock.unlock();
        }
    }

    int inFlightCount() {
        lock.lock();
        try {
            return inFlight;
        } finally {
            lock.unlock();
        }
    }

    boolean awaitIntakeStopped(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }

        lock.lock();
        try {
            long remainingNanos = timeout.toNanos();
            while (state == State.OPEN && remainingNanos > 0) {
                try {
                    remainingNanos = stateChanged.awaitNanos(remainingNanos);
                } catch (InterruptedException interruption) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return state != State.OPEN;
        } finally {
            lock.unlock();
        }
    }

    void close() {
        lock.lock();
        try {
            state = State.CLOSED;
            stateChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private PersistenceDrainResult finish(PersistenceDrainStatus status) {
        state = State.CLOSED;
        terminalResult = new PersistenceDrainResult(status, inFlight);
        stateChanged.signalAll();
        return terminalResult;
    }

    private void release() {
        lock.lock();
        try {
            if (inFlight <= 0) {
                return;
            }
            inFlight--;
            if (inFlight == 0) {
                stateChanged.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    private enum State {
        OPEN,
        DRAINING,
        CLOSED
    }

    static final class Permit implements AutoCloseable {
        private final PersistenceWorkGate owner;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Permit(PersistenceWorkGate owner) {
            this.owner = owner;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.release();
            }
        }
    }
}
