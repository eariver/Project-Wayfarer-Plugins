package io.github.eariver.wayfarer.core.transaction;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultWayfarerWaymarkTest {
    @Test
    void providerCallsUseManagedWorkerAndMapEffects() {
        RecordingProvider provider = new RecordingProvider();
        try (ManagedExecutor executor = executor()) {
            DefaultWayfarerWaymark waymark = new DefaultWayfarerWaymark(
                provider,
                executor,
                Duration.ofSeconds(1)
            );
            UUID playerUuid = UUID.randomUUID();

            assertEquals(250L, waymark.balance(playerUuid).toCompletableFuture().join());
            assertTrue(
                waymark.debit(playerUuid, 25L, "debit-1")
                    .toCompletableFuture()
                    .join()
                    .success()
            );
            assertFalse(
                waymark.credit(playerUuid, 25L, "refund-1")
                    .toCompletableFuture()
                    .join()
                    .success()
            );
            assertTrue(provider.threadName.get().startsWith("Wayfarer-Waymark-Test-"));
        }
    }

    @Test
    void providerFailureIsSanitized() {
        RecordingProvider provider = new RecordingProvider();
        provider.balance = CompletableFuture.failedFuture(
            new IllegalStateException("provider-secret-detail")
        );
        try (ManagedExecutor executor = executor()) {
            DefaultWayfarerWaymark waymark = new DefaultWayfarerWaymark(
                provider,
                executor,
                Duration.ofSeconds(1)
            );

            Throwable failure = org.junit.jupiter.api.Assertions.assertThrows(
                java.util.concurrent.CompletionException.class,
                () -> waymark.balance(UUID.randomUUID()).toCompletableFuture().join()
            ).getCause();
            assertEquals("Waymark provider operation failed", failure.getMessage());
        }
    }

    private static ManagedExecutor executor() {
        return new ManagedExecutor(
            1,
            "Wayfarer-Waymark-Test",
            8,
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        );
    }

    private static final class RecordingProvider implements WayfarerWaymarkProvider {
        private final AtomicReference<String> threadName = new AtomicReference<>();
        private CompletionStage<Long> balance = CompletableFuture.completedFuture(250L);

        @Override
        public CompletionStage<ProbeResult> probe() {
            return CompletableFuture.completedFuture(
                new ProbeResult(true, "test", null)
            );
        }

        @Override
        public CompletionStage<Long> balance(UUID playerUuid) {
            recordThread();
            return balance;
        }

        @Override
        public CompletionStage<EffectResult> debit(
            UUID playerUuid,
            long amount,
            String operationId
        ) {
            recordThread();
            return CompletableFuture.completedFuture(
                new EffectResult(EffectStatus.SUCCEEDED, "debit-ref", null)
            );
        }

        @Override
        public CompletionStage<EffectResult> refund(
            UUID playerUuid,
            long amount,
            String operationId,
            String debitProviderReference
        ) {
            recordThread();
            return CompletableFuture.completedFuture(
                new EffectResult(EffectStatus.KNOWN_FAILURE, null, "REFUND_FAILED")
            );
        }

        @Override
        public CompletionStage<ResolutionResult> resolve(
            EffectKind effectKind,
            String operationId,
            String providerReference
        ) {
            recordThread();
            return CompletableFuture.completedFuture(
                new ResolutionResult(ResolutionStatus.UNKNOWN, null, "UNKNOWN")
            );
        }

        private void recordThread() {
            threadName.set(Thread.currentThread().getName());
        }
    }
}
