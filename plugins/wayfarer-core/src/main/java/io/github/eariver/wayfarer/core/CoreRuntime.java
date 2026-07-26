package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleCoordinator;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleStep;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.core.service.WayfarerServiceFactory;
import io.github.eariver.wayfarer.core.task.DefaultWayfarerTasks;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class CoreRuntime {
    private final CoreConfig config;
    private final ServicePublisher publisher;
    private final MainThreadDispatcher mainThread;
    private final Consumer<String> warningSink;
    private final LifecycleCoordinator lifecycle;
    private final HealthRegistry health;
    private ManagedExecutor executor;
    private WayfarerServices services;

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.lifecycle = new LifecycleCoordinator(warningSink);
        this.health = new HealthRegistry(clock, lifecycle::state);
    }

    public void enable() {
        try {
            lifecycle.enable(List.of(
                new LifecycleStep("Config", this::initializeConfig),
                new LifecycleStep("Executor", this::initializeExecutor)
            ), new LifecycleStep("Services", this::initializeServices));
            health.refreshLifecycle();
        } catch (RuntimeException failure) {
            health.refreshLifecycle();
            throw failure;
        }
    }

    public void disable() {
        lifecycle.disable();
        health.refreshLifecycle();
    }

    public WayfarerLifecycleState state() {
        return lifecycle.state();
    }

    public WayfarerHealth health() {
        return health;
    }

    public WayfarerServices services() {
        if (services == null) {
            throw new IllegalStateException("Wayfarer services are unavailable");
        }
        return services;
    }

    public String failureDetail() {
        return lifecycle.failureDetail();
    }

    private AutoCloseable initializeConfig() {
        health.update(HealthRegistry.CONFIG, WayfarerHealth.Status.UP, "Config version validated");
        return () -> {
            config.close();
            health.update(
                HealthRegistry.CONFIG,
                WayfarerHealth.Status.DISABLED,
                "Configuration released"
            );
        };
    }

    private AutoCloseable initializeExecutor() {
        executor = new ManagedExecutor(
            config.executor().threads(),
            config.executor().threadNamePrefix(),
            config.shutdownTimeout(),
            failure -> health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DOWN,
                "Task failure observed: " + failure.getClass().getSimpleName()
            ),
            warning -> {
                health.update(HealthRegistry.EXECUTOR, WayfarerHealth.Status.DOWN, warning);
                warningSink.accept(warning);
            }
        );
        health.update(HealthRegistry.EXECUTOR, WayfarerHealth.Status.UP, "Executor accepting tasks");
        return () -> {
            executor.close();
            health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DISABLED,
                "Executor stopped"
            );
        };
    }

    private AutoCloseable initializeServices() {
        DefaultWayfarerTasks tasks = new DefaultWayfarerTasks(
            executor,
            mainThread,
            lifecycle::acceptsCallbacks
        );
        services = WayfarerServiceFactory.create(
            config.serverId(),
            config.configVersion(),
            lifecycle::state,
            tasks,
            health
        );
        publisher.publish(services, health);
        health.update(HealthRegistry.SERVICES, WayfarerHealth.Status.UP, "Services registered");
        return () -> {
            publisher.unpublish();
            services = null;
            health.update(
                HealthRegistry.SERVICES,
                WayfarerHealth.Status.DISABLED,
                "Services unregistered"
            );
        };
    }
}
