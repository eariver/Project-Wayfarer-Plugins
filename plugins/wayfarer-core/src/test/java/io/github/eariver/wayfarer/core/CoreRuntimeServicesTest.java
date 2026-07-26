package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleException;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreRuntimeServicesTest {
    @Test
    void registersOnlyAfterPriorInitializationSucceeds() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        runtime.enable();
        try {
            assertEquals(1, publisher.publishCount);
            assertEquals(
                WayfarerHealth.Status.UP,
                publisher.snapshotAtPublish.components().get("Config").status()
            );
            assertEquals(
                WayfarerHealth.Status.UP,
                publisher.snapshotAtPublish.components().get("Executor").status()
            );
            assertEquals(
                WayfarerLifecycleState.ENABLED,
                publisher.snapshotAtPublish.lifecycleState()
            );
        } finally {
            runtime.disable();
        }
    }

    @Test
    void unregistersServicesOnDisable() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        runtime.enable();
        runtime.disable();
        assertEquals(1, publisher.unpublishCount);
        assertFalse(publisher.published);
        assertEquals(WayfarerLifecycleState.DISABLED, runtime.state());
    }

    @Test
    void failedPriorInitializationDoesNotRegisterServices() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(
            TestCoreConfigs.withThreadPrefix("invalid-prefix"),
            publisher
        );
        assertThrows(LifecycleException.class, runtime::enable);
        assertEquals(0, publisher.publishCount);
        assertEquals(WayfarerLifecycleState.FAILED, runtime.state());
        runtime.disable();
    }

    @Test
    void failedServicePublicationLeavesNoPublishedService() {
        RecordingPublisher publisher = new RecordingPublisher();
        publisher.failPublication = true;
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        assertThrows(LifecycleException.class, runtime::enable);
        assertFalse(publisher.published);
        assertEquals(WayfarerLifecycleState.FAILED, runtime.state());
        runtime.disable();
    }

    @Test
    void implementationClassIsNotPublicApi() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        runtime.enable();
        try {
            assertFalse(Modifier.isPublic(runtime.services().getClass().getModifiers()));
        } finally {
            runtime.disable();
        }
    }

    @Test
    void serviceUsesApiModuleClassIdentity() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        runtime.enable();
        try {
            assertTrue(runtime.services() instanceof WayfarerServices);
            assertSame(
                WayfarerServices.class.getClassLoader(),
                runtime.services().getClass().getClassLoader()
            );
        } finally {
            runtime.disable();
        }
    }

    @Test
    void unimplementedDependenciesFailWithStableMessage() {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(TestCoreConfigs.valid(), publisher);
        runtime.enable();
        try {
            IllegalStateException failure = assertThrows(
                IllegalStateException.class,
                () -> runtime.services().database()
            );
            assertEquals("MariaDB is unavailable", failure.getMessage());
        } finally {
            runtime.disable();
        }
    }

    @Test
    void forcedButConfirmedShutdownIsReportedDisabled() throws Exception {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(
            TestCoreConfigs.withShutdownTimeout(Duration.ofMillis(20)),
            publisher
        );
        CountDownLatch started = new CountDownLatch(1);
        runtime.enable();
        CompletionStage<Void> task = runtime.services().tasks().database(() -> {
            started.countDown();
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException expected) {
                Thread.currentThread().interrupt();
            }
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        runtime.disable();

        WayfarerHealth.ComponentHealth executor =
            runtime.health().snapshot().components().get("Executor");
        assertEquals(WayfarerHealth.Status.DISABLED, executor.status());
        assertEquals("Executor stopped after forced termination", executor.detail());
        task.toCompletableFuture().get(1, TimeUnit.SECONDS);
    }

    @Test
    void incompleteExecutorShutdownIsReportedDown() throws Exception {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(
            TestCoreConfigs.withShutdownTimeout(Duration.ofMillis(20)),
            publisher
        );
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        runtime.enable();
        CompletionStage<Void> task = runtime.services().tasks().database(() -> {
            started.countDown();
            awaitUninterruptibly(release);
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        try {
            runtime.disable();

            WayfarerHealth.ComponentHealth executor =
                runtime.health().snapshot().components().get("Executor");
            assertEquals(WayfarerLifecycleState.DISABLED, runtime.state());
            assertEquals(WayfarerHealth.Status.DOWN, executor.status());
            assertEquals(
                "Executor did not terminate after forced shutdown",
                executor.detail()
            );
        } finally {
            release.countDown();
            task.toCompletableFuture().get(1, TimeUnit.SECONDS);
        }
    }

    @Test
    void incompleteShutdownDoesNotReportExecutorStopped() throws Exception {
        RecordingPublisher publisher = new RecordingPublisher();
        CoreRuntime runtime = runtime(
            TestCoreConfigs.withShutdownTimeout(Duration.ofMillis(20)),
            publisher
        );
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        runtime.enable();
        CompletionStage<Void> task = runtime.services().tasks().database(() -> {
            started.countDown();
            awaitUninterruptibly(release);
            return null;
        });
        assertTrue(started.await(1, TimeUnit.SECONDS));

        try {
            runtime.disable();
            String detail = runtime.health().snapshot()
                .components()
                .get("Executor")
                .detail();
            assertFalse(detail.contains("Executor stopped"));
        } finally {
            release.countDown();
            task.toCompletableFuture().get(1, TimeUnit.SECONDS);
        }
    }

    private static void awaitUninterruptibly(CountDownLatch release) {
        boolean interrupted = false;
        while (release.getCount() > 0) {
            try {
                release.await();
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private static CoreRuntime runtime(
        io.github.eariver.wayfarer.core.config.CoreConfig config,
        RecordingPublisher publisher
    ) {
        return new CoreRuntime(
            config,
            publisher,
            Runnable::run,
            Clock.systemUTC(),
            ignored -> {}
        );
    }

    private static final class RecordingPublisher implements ServicePublisher {
        private int publishCount;
        private int unpublishCount;
        private boolean published;
        private boolean failPublication;
        private WayfarerHealth.HealthSnapshot snapshotAtPublish;

        @Override
        public void publish(WayfarerServices services, WayfarerHealth health) {
            publishCount++;
            snapshotAtPublish = health.snapshot();
            if (failPublication) {
                published = false;
                throw new IllegalStateException("publication failed");
            }
            published = true;
        }

        @Override
        public void unpublish() {
            unpublishCount++;
            published = false;
        }
    }
}
