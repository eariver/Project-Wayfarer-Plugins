package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleException;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;
import java.time.Clock;

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
            assertTrue(failure.getMessage().contains("not implemented"));
        } finally {
            runtime.disable();
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
