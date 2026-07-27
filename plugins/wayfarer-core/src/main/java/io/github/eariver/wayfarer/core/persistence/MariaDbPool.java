package io.github.eariver.wayfarer.core.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MariaDbPool implements AutoCloseable {
    private static final String POOL_PREFIX = "Wayfarer-Core-";
    private final HikariDataSource dataSource;
    private final AtomicBoolean closed = new AtomicBoolean();
    private final PersistenceWorkGate workGate = new PersistenceWorkGate();
    private InternalDatabase database;

    MariaDbPool(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static MariaDbPool open(
        String serverId,
        CoreConfig.MariaDbSettings settings
    ) {
        Objects.requireNonNull(serverId, "serverId");
        Objects.requireNonNull(settings, "settings");
        if (!settings.enabled()) {
            throw new PersistenceException("MariaDB is disabled");
        }
        if (settings.jdbcUrl() == null
            || settings.username() == null
            || settings.password() == null) {
            throw new PersistenceException("MariaDB credentials are unavailable");
        }

        HikariDataSource dataSource = null;
        try {
            HikariConfig hikari = configuration(serverId, settings);
            dataSource = new HikariDataSource(hikari);
            try (Connection connection = dataSource.getConnection()) {
                if (!connection.isValid(
                    Math.max(1, Math.toIntExact(settings.connectionTimeout().toSeconds()))
                )) {
                    throw new PersistenceException("MariaDB connection validation failed");
                }
                verifyUtcSession(connection);
            }
            return new MariaDbPool(dataSource);
        } catch (RuntimeException | SQLException failure) {
            if (dataSource != null) {
                dataSource.close();
            }
            throw new PersistenceException("MariaDB pool initialization failed");
        }
    }

    DataSource dataSource() {
        if (closed.get()) {
            throw new PersistenceException("MariaDB pool is closed");
        }
        return dataSource;
    }

    public void initializeInternalBoundary(
        ManagedExecutor executor,
        ThreadContext threadContext
    ) {
        if (closed.get()) {
            throw new PersistenceException("MariaDB pool is closed");
        }
        database = new InternalDatabase(
            dataSource,
            executor,
            threadContext,
            workGate,
            () -> !isClosed()
        );
    }

    public PersistenceDrainResult stopAcceptingAndAwait(Duration timeout) {
        return workGate.stopAcceptingAndAwait(timeout);
    }

    InternalDatabase internalDatabaseForTesting() {
        if (database == null) {
            throw new IllegalStateException("Internal database boundary is unavailable");
        }
        return database;
    }

    public boolean isClosed() {
        return closed.get() || dataSource.isClosed();
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            workGate.close();
            database = null;
            dataSource.close();
        }
    }

    static String poolName(String serverId) {
        String sanitized = serverId.replaceAll("[^A-Za-z0-9_-]+", "-")
            .replaceAll("-{2,}", "-")
            .replaceAll("^-|-$", "");
        if (sanitized.isBlank()) {
            sanitized = "server";
        }
        int maximumSuffixLength = 64 - POOL_PREFIX.length();
        if (sanitized.length() > maximumSuffixLength) {
            sanitized = sanitized.substring(0, maximumSuffixLength);
        }
        return POOL_PREFIX + sanitized;
    }

    static HikariConfig configuration(
        String serverId,
        CoreConfig.MariaDbSettings settings
    ) {
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(settings.jdbcUrl().use(String::new));
        hikari.setUsername(settings.username().use(String::new));
        hikari.setPassword(settings.password().use(String::new));
        hikari.setMaximumPoolSize(settings.maximumPoolSize());
        hikari.setMinimumIdle(settings.minimumIdle());
        hikari.setConnectionTimeout(settings.connectionTimeout().toMillis());
        hikari.setInitializationFailTimeout(settings.connectionTimeout().toMillis());
        hikari.setPoolName(poolName(serverId));
        hikari.setAutoCommit(true);
        hikari.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        hikari.setConnectionInitSql("SET time_zone = '+00:00'");
        hikari.setRegisterMbeans(false);
        return hikari;
    }

    private static void verifyUtcSession(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT @@session.time_zone")) {
            if (!result.next() || !"+00:00".equals(result.getString(1))) {
                throw new PersistenceException("MariaDB UTC session initialization failed");
            }
        }
    }
}
