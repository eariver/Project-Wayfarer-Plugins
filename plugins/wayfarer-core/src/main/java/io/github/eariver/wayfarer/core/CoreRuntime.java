package io.github.eariver.wayfarer.core;

import io.github.eariver.wayfarer.api.WayfarerHealth;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerTransactions;
import io.github.eariver.wayfarer.api.WayfarerWaymark;
import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.health.HealthRegistry;
import io.github.eariver.wayfarer.core.identity.PlayerIdentityListenerRegistrar;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleCoordinator;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleStep;
import io.github.eariver.wayfarer.core.persistence.MariaDbPool;
import io.github.eariver.wayfarer.core.persistence.MigrationLifecycle;
import io.github.eariver.wayfarer.core.persistence.DurableAudit;
import io.github.eariver.wayfarer.core.persistence.IdentityRuntime;
import io.github.eariver.wayfarer.core.persistence.PersistenceDrainResult;
import io.github.eariver.wayfarer.core.persistence.PersistenceDrainStatus;
import io.github.eariver.wayfarer.core.persistence.PersistenceException;
import io.github.eariver.wayfarer.core.persistence.ThreadContext;
import io.github.eariver.wayfarer.core.redis.RedisRuntime;
import io.github.eariver.wayfarer.core.redis.RedisRuntimeException;
import io.github.eariver.wayfarer.core.service.ServicePublisher;
import io.github.eariver.wayfarer.core.service.WayfarerServiceFactory;
import io.github.eariver.wayfarer.core.task.DefaultWayfarerTasks;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import io.github.eariver.wayfarer.core.task.ShutdownResult;
import io.github.eariver.wayfarer.core.transaction.DefaultWayfarerWaymark;
import io.github.eariver.wayfarer.core.transaction.TransactionEngine;
import io.github.eariver.wayfarer.core.transaction.WaymarkProviderSource;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public final class CoreRuntime {
    private final CoreConfig config;
    private final ServicePublisher publisher;
    private final MainThreadDispatcher mainThread;
    private final Consumer<String> warningSink;
    private final LifecycleCoordinator lifecycle;
    private final HealthRegistry health;
    private final ThreadContext threadContext;
    private final Clock clock;
    private final PlayerIdentityListenerRegistrar identityListenerRegistrar;
    private final WaymarkProviderSource waymarkProviderSource;
    private ManagedExecutor executor;
    private MariaDbPool mariaDbPool;
    private MigrationLifecycle migration;
    private DurableAudit audit;
    private IdentityRuntime identity;
    private RedisRuntime redis;
    private WayfarerTransactions transactions;
    private WayfarerWaymark waymark;
    private PersistenceDrainResult persistenceDrainResult;
    private WayfarerServices services;

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink
    ) {
        this(
            config,
            publisher,
            mainThread,
            clock,
            warningSink,
            () -> false,
            PlayerIdentityListenerRegistrar.unavailable(),
            WaymarkProviderSource.unavailable()
        );
    }

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink,
        ThreadContext threadContext
    ) {
        this(
            config,
            publisher,
            mainThread,
            clock,
            warningSink,
            threadContext,
            PlayerIdentityListenerRegistrar.unavailable(),
            WaymarkProviderSource.unavailable()
        );
    }

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink,
        ThreadContext threadContext,
        PlayerIdentityListenerRegistrar identityListenerRegistrar
    ) {
        this(
            config,
            publisher,
            mainThread,
            clock,
            warningSink,
            threadContext,
            identityListenerRegistrar,
            WaymarkProviderSource.unavailable()
        );
    }

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink,
        ThreadContext threadContext,
        PlayerIdentityListenerRegistrar identityListenerRegistrar,
        WayfarerWaymarkProvider waymarkProvider
    ) {
        this(
            config,
            publisher,
            mainThread,
            clock,
            warningSink,
            threadContext,
            identityListenerRegistrar,
            WaymarkProviderSource.fixed(waymarkProvider)
        );
    }

    public CoreRuntime(
        CoreConfig config,
        ServicePublisher publisher,
        MainThreadDispatcher mainThread,
        Clock clock,
        Consumer<String> warningSink,
        ThreadContext threadContext,
        PlayerIdentityListenerRegistrar identityListenerRegistrar,
        WaymarkProviderSource waymarkProviderSource
    ) {
        this.config = Objects.requireNonNull(config, "config");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
        this.threadContext = Objects.requireNonNull(threadContext, "threadContext");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.identityListenerRegistrar = Objects.requireNonNull(
            identityListenerRegistrar,
            "identityListenerRegistrar"
        );
        this.waymarkProviderSource = Objects.requireNonNull(
            waymarkProviderSource,
            "waymarkProviderSource"
        );
        this.lifecycle = new LifecycleCoordinator(warningSink);
        this.health = new HealthRegistry(clock, lifecycle::state);
    }

    public void enable() {
        try {
            lifecycle.enable(List.of(
                new LifecycleStep("Config", this::initializeConfig),
                new LifecycleStep("Executor", this::initializeExecutor),
                new LifecycleStep("MariaDB", this::initializeMariaDb),
                new LifecycleStep("Migration", this::initializeMigration),
                new LifecycleStep("IdentityFinalization", this::initializeIdentityFinalization),
                new LifecycleStep("DatabaseDrain", this::initializeDatabaseDrain),
                new LifecycleStep("Audit", this::initializeAudit),
                new LifecycleStep("IdentityQuiescence", this::initializeIdentityQuiescence),
                new LifecycleStep("Identity", this::initializeIdentity),
                new LifecycleStep("PlayerIdentityListener", this::initializeIdentityListener),
                new LifecycleStep("Redis", this::initializeRedis),
                new LifecycleStep("WaymarkTransactions", this::initializeWaymarkTransactions)
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
            config.executor().queueCapacity(),
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
                threadContext
            );
            health.update(
                HealthRegistry.MARIA_DB,
                WayfarerHealth.Status.UP,
                "Connection pool available"
            );
            return () -> {
                mariaDbPool.close();
                applyMariaDbClosedHealth();
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

    private AutoCloseable initializeDatabaseDrain() {
        if (!config.mariadb().enabled()) {
            return () -> {};
        }
        if (mariaDbPool == null || mariaDbPool.isClosed()) {
            throw new PersistenceException("Database drain requires MariaDB");
        }
        return () -> recordPersistenceDrainResult(
            mariaDbPool.stopAcceptingAndAwait(config.shutdownTimeout())
        );
    }

    private AutoCloseable initializeAudit() {
        if (!config.audit().enabled()) {
            health.update(
                HealthRegistry.AUDIT,
                WayfarerHealth.Status.UNKNOWN,
                "Disabled by configuration"
            );
            return () -> {};
        }
        if (mariaDbPool == null || mariaDbPool.isClosed() || migration == null) {
            health.update(
                HealthRegistry.AUDIT,
                WayfarerHealth.Status.DOWN,
                "Durable audit dependency unavailable"
            );
            throw new PersistenceException("Audit requires migrated MariaDB");
        }
        audit = mariaDbPool.createDurableAudit(config, health, clock, warningSink);
        try {
            audit.initialize().toCompletableFuture()
                .orTimeout(config.shutdownTimeout().toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .join();
        } catch (RuntimeException failure) {
            audit.close();
            throw new PersistenceException("Audit initialization failed");
        }
        return audit;
    }

    private AutoCloseable initializeIdentity() {
        if (!config.audit().enabled()) {
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.UNKNOWN,
                "Durable audit disabled"
            );
            return () -> {};
        }
        if (audit == null) {
            health.update(
                HealthRegistry.IDENTITY,
                WayfarerHealth.Status.DOWN,
                "Durable audit dependency unavailable"
            );
            throw new PersistenceException("Identity requires durable audit");
        }
        identity = mariaDbPool.createIdentityRuntime(
            audit,
            health,
            config.serverId(),
            clock,
            UUID::randomUUID,
            warningSink
        );
        try {
            identity.initialize().toCompletableFuture()
                .orTimeout(config.shutdownTimeout().toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)
                .join();
        } catch (RuntimeException failure) {
            identity.close();
            throw new PersistenceException("Identity initialization failed");
        }
        return identity;
    }

    private AutoCloseable initializeIdentityFinalization() {
        return () -> {
            if (identity == null) {
                return;
            }
            PersistenceDrainResult drain = persistenceDrainResult;
            if (drain == null) {
                drain = new PersistenceDrainResult(
                    PersistenceDrainStatus.TIMED_OUT,
                    0
                );
            }
            identity.finishClosing(drain, config.shutdownTimeout());
        };
    }

    private AutoCloseable initializeIdentityQuiescence() {
        return () -> {
            if (identity != null) {
                identity.quiesce(config.shutdownTimeout());
            }
        };
    }

    private AutoCloseable initializeIdentityListener() {
        if (identity == null) {
            return () -> {};
        }
        return identityListenerRegistrar.register(identity);
    }

    private AutoCloseable initializeRedis() {
        if (!config.redis().enabled()) {
            health.update(
                HealthRegistry.REDIS,
                WayfarerHealth.Status.UNKNOWN,
                "Disabled by configuration"
            );
            return () -> {};
        }
        try {
            redis = executor.submit(() -> RedisRuntime.connect(
                config.redis(),
                config.serverId(),
                health,
                threadContext,
                executor,
                warningSink
            )).join();
            return redis;
        } catch (RuntimeException failure) {
            health.update(
                HealthRegistry.REDIS,
                WayfarerHealth.Status.DOWN,
                "Redis initialization failed"
            );
            throw new RedisRuntimeException("Redis initialization failed");
        }
    }

    private AutoCloseable initializeWaymarkTransactions() {
        if (!config.waymark().enabled()) {
            health.update("Waymark", WayfarerHealth.Status.UNKNOWN, "Disabled by configuration");
            health.update("Transaction", WayfarerHealth.Status.UNKNOWN, "Waymark disabled");
            return () -> {};
        }
        WayfarerWaymarkProvider waymarkProvider;
        try {
            waymarkProvider = waymarkProviderSource.discover().orElse(null);
        } catch (RuntimeException failure) {
            waymarkProvider = null;
        }
        if (waymarkProvider == null) {
            health.update("Waymark", WayfarerHealth.Status.DOWN, "Provider authority unavailable");
            health.update("Transaction", WayfarerHealth.Status.DOWN, "Provider unavailable");
            return () -> {};
        }
        if (mariaDbPool == null || mariaDbPool.isClosed() || audit == null) {
            health.update("Transaction", WayfarerHealth.Status.DOWN, "Durable dependency unavailable");
            return () -> {};
        }
        try {
            WayfarerWaymarkProvider.ProbeResult probe = waymarkProvider.probe()
                .toCompletableFuture()
                .orTimeout(
                    config.waymark().operationTimeout().toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .join();
            if (!probe.available()) {
                throw new IllegalStateException("Waymark provider probe failed");
            }
            TransactionEngine candidate = new TransactionEngine(
                mariaDbPool.createTransactionRepository(),
                waymarkProvider,
                audit,
                config.serverId(),
                config.waymark().operationTimeout(),
                clock,
                warning -> {
                    health.update(
                        HealthRegistry.AUDIT,
                        WayfarerHealth.Status.DOWN,
                        "Transaction audit mirror failed"
                    );
                    warn(warning);
                }
            );
            candidate.recoverPending(100)
                .toCompletableFuture()
                .orTimeout(
                    config.shutdownTimeout().toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .join();
            transactions = candidate;
            waymark = new DefaultWayfarerWaymark(waymarkProvider);
            health.update("Waymark", WayfarerHealth.Status.UP, "Provider capability available");
            health.update("Transaction", WayfarerHealth.Status.UP, "Transaction engine available");
            return () -> {
                transactions = null;
                waymark = null;
                health.update("Transaction", WayfarerHealth.Status.DISABLED, "Transaction intake stopped");
                health.update("Waymark", WayfarerHealth.Status.DISABLED, "Provider boundary released");
            };
        } catch (RuntimeException failure) {
            health.update("Waymark", WayfarerHealth.Status.DOWN, "Provider initialization failed");
            health.update("Transaction", WayfarerHealth.Status.DOWN, "Transaction initialization failed");
            warn("Wayfarer transaction service remained unavailable after provider recovery");
            transactions = null;
            waymark = null;
            return () -> {};
        }
    }

    void recordPersistenceDrainResult(PersistenceDrainResult result) {
        persistenceDrainResult = Objects.requireNonNull(result, "result");
        switch (result.status()) {
            case DRAINED -> {
                // MariaDB remains available until migration release and pool close complete.
            }
            case TIMED_OUT -> {
                health.update(
                    HealthRegistry.MARIA_DB,
                    WayfarerHealth.Status.DOWN,
                    "Database work drain timed out with "
                        + result.remainingInFlight()
                        + " operation(s) remaining"
                );
                warn("Wayfarer database work exceeded shutdown drain timeout");
            }
            case INTERRUPTED -> {
                health.update(
                    HealthRegistry.MARIA_DB,
                    WayfarerHealth.Status.DOWN,
                    "Database work drain was interrupted with "
                        + result.remainingInFlight()
                        + " operation(s) remaining"
                );
                warn("Wayfarer database work drain was interrupted");
            }
        }
    }

    private void applyMariaDbClosedHealth() {
        if (persistenceDrainResult == null
            || persistenceDrainResult.status() == PersistenceDrainStatus.DRAINED) {
            health.update(
                HealthRegistry.MARIA_DB,
                WayfarerHealth.Status.DISABLED,
                "Connection pool closed after database work drain"
            );
            return;
        }
        String incompleteDetail = switch (persistenceDrainResult.status()) {
            case DRAINED -> throw new IllegalStateException("Unexpected drained result");
            case TIMED_OUT -> "Connection pool closed after database work drain timeout with "
                + persistenceDrainResult.remainingInFlight()
                + " operation(s) remaining";
            case INTERRUPTED -> "Connection pool closed after interrupted database work drain with "
                + persistenceDrainResult.remainingInFlight()
                + " operation(s) remaining";
        };
        health.update(
            HealthRegistry.MARIA_DB,
            WayfarerHealth.Status.DOWN,
            incompleteDetail
        );
    }

    private void warn(String warning) {
        try {
            warningSink.accept(warning);
        } catch (RuntimeException ignored) {
            // Cleanup and health transitions must survive unavailable diagnostics.
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
            health,
            audit,
            identity == null ? null : identity.itemIdentity(),
            transactions,
            waymark
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
