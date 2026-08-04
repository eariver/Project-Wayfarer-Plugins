package io.github.eariver.wayfarer.common;

import java.util.concurrent.atomic.AtomicBoolean;

public final class SingleUseGate {
    private final AtomicBoolean accepted = new AtomicBoolean();

    public boolean tryAcquire() {
        return accepted.compareAndSet(false, true);
    }

    public boolean acquired() {
        return accepted.get();
    }
}
