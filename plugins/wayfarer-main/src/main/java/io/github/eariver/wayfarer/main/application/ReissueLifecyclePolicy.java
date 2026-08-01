package io.github.eariver.wayfarer.main.application;

import java.util.concurrent.atomic.AtomicBoolean;

/** Small lifecycle gate for recovery activation and late callbacks. */
public final class ReissueLifecyclePolicy {
    private ReissueLifecyclePolicy() {
    }

    public static boolean canExposeCommands(
        boolean accepting,
        boolean pluginEnabled,
        boolean recoverySucceeded,
        boolean commandAvailable
    ) {
        return accepting && pluginEnabled && recoverySucceeded && commandAvailable;
    }

    public static boolean acceptsCallback(
        boolean accepting,
        boolean runtimeEnabled
    ) {
        return accepting && runtimeEnabled;
    }

    public static final class RecoveryStartGate {
        private final AtomicBoolean started = new AtomicBoolean();

        public boolean tryStart() {
            return started.compareAndSet(false, true);
        }
    }
}
