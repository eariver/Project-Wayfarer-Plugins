package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleCoordinator;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleStep;
import io.github.eariver.wayfarer.core.persistence.MariaDbPool;
import io.github.eariver.wayfarer.core.persistence.MigrationLifecycle;
import io.github.eariver.wayfarer.core.persistence.PersistenceException;
import io.github.eariver.wayfarer.core.persistence.ThreadContext;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.core.service.WayfarerServiceFactory;
import io.github.eariver.wayfarer.core.task.DefaultWayfarerTasks;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import io.github.eariver.wayfarer.core.task.ShutdownResult;

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
    private final ThreadContext threadContext;
    private ManagedExecutor executor;
    private MariaDbPool mariaDbPool;
    private MigrationLifecycle migration;
    private WayfarerServices services;

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink
    ) {
        this(config, publisher, mainThread, clock, warningSink, () -> false);
    }

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink,
        ThreadContext threadContext
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.threadContext = Objects.requireNonNull(threadContext, "threadContext");
        this.lifecycle = new LifecycleCoordinator(warningSink);
        this.health = new HealthRegistry(clock, lifecycle::state);
    }

    public void enable() {
        try {
            lifecycle.enable(List.of(
                new LifecycleStep("Config", this::initializeConfig),
                new LifecycleStep("Executor", this::initializeExecutor),
                new LifecycleStep("MariaDB", this::initializeMariaDb),
                new LifecycleStep("Migration", this::initializeMigration)
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

    boolean isMariaDbPoolClosed() {
        return mariaDbPool == null || mariaDbPool.isClosed();
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
            applyExecutorShutdownHealth(executor.shutdown());
        };
    }

    private void applyExecutorShutdownHealth(ShutdownResult result) {
        switch (result.status()) {
            case GRACEFUL -> health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DISABLED,
                "Executor stopped gracefully"
            );
            case FORCED_TERMINATED -> health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DISABLED,
                "Executor stopped after forced termination"
            );
            case INCOMPLETE -> health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DOWN,
                "Executor did not terminate after forced shutdown"
            );
            case INTERRUPTED -> health.update(
                HealthRegistry.EXECUTOR,
                WayfarerHealth.Status.DOWN,
                "Executor shutdown was interrupted"
            );
        }
    }

    private AutoCloseable initializeMariaDb() {
        if (!config.mariadb().enabled()) {
            health.update(
                HealthRegistry.MARIA_DB,
                WayfarerHealth.Status.UNKNOWN,
                "Disabled by configuration"
            );
            return () -> {};
        }
        try {
            mariaDbPool = executor.submit(
                () -> MariaDbPool.open(config.serverId(), config.mariadb())
            ).join();
            mariaDbPool.initializeInternalBoundary(
                executor,
                threadContext,
                lifecycle::acceptsCallbacks
            );
            health.update(
                HealthRegistry.MARIA_DB,
                WayfarerHealth.Status.UP,
                "Connection pool available"
            );
            return () -> {
                mariaDbPool.close();
                health.update(
                    HealthRegistry.MARIA_DB,
                    WayfarerHealth.Status.DISABLED,
                    "Connection pool closed"
                );
            };
        } catch (RuntimeException failure) {
            if (mariaDbPool != null && !mariaDbPool.isClosed()) {
                mariaDbPool.close();
            }
            health.update(
                HealthRegistry.MARIA_DB,
                WayfarerHealth.Status.DOWN,
                "Connection pool initialization failed"
            );
            throw new PersistenceException("MariaDB initialization failed");
        }
    }

    private AutoCloseable initializeMigration() {
        if (!config.migration().enabled()) {
            health.update(
                HealthRegistry.MIGRATION,
                WayfarerHealth.Status.UNKNOWN,
                "Disabled by configuration"
            );
            return () -> {};
        }
        if (!config.mariadb().enabled() || mariaDbPool == null || mariaDbPool.isClosed()) {
            health.update(
                HealthRegistry.MIGRATION,
                WayfarerHealth.Status.DOWN,
                "MariaDB dependency unavailable"
            );
            throw new PersistenceException("Migration requires MariaDB");
        }
        try {
            migration = executor.submit(
                () -> MigrationLifecycle.migrate(
                    mariaDbPool,
                    config.migration().locations()
                )
            ).join();
            health.update(
                HealthRegistry.MIGRATION,
                WayfarerHealth.Status.UP,
                "Core schema validated"
            );
            return () -> {
                migration.close();
                health.update(
                    HealthRegistry.MIGRATION,
                    WayfarerHealth.Status.DISABLED,
                    "Migration lifecycle released"
                );
            };
        } catch (RuntimeException failure) {
            health.update(
                HealthRegistry.MIGRATION,
                WayfarerHealth.Status.DOWN,
                "Core schema migration failed"
            );
            throw new PersistenceException("Migration initialization failed");
        }
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
