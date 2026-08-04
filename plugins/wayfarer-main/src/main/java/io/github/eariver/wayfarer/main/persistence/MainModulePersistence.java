package io.github.eariver.wayfarer.main.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.eariver.wayfarer.main.config.MainModuleConfig;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.function.Function;

public final class MainModulePersistence implements AutoCloseable {
    public static final String HISTORY_TABLE = "wf_main_flyway_schema_history";
    private final HikariDataSource dataSource;

    private MainModulePersistence(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static MainModulePersistence open(
        MainModuleConfig.DatabaseConfig database,
        Function<String, String> environment
    ) {
        Objects.requireNonNull(database, "database");
        Objects.requireNonNull(environment, "environment");
        String jdbcUrl = required(environment, database.jdbcUrlReference());
        String username = required(environment, database.usernameReference());
        String password = required(environment, database.passwordReference());
        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(database.maximumPoolSize());
        hikari.setMinimumIdle(database.minimumIdle());
        hikari.setConnectionTimeout(database.connectionTimeoutMillis());
        hikari.setPoolName("Wayfarer-Main");
        HikariDataSource source = new HikariDataSource(hikari);
        try {
            Flyway.configure(MainModulePersistence.class.getClassLoader())
                .dataSource(source)
                .table(HISTORY_TABLE)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .locations("classpath:db/migration/main")
                .load()
                .migrate();
            return new MainModulePersistence(source);
        } catch (RuntimeException failure) {
            source.close();
            throw new IllegalStateException("Main module persistence initialization failed");
        }
    }

    public DataSource dataSource() {
        return dataSource;
    }

    @Override
    public void close() {
        dataSource.close();
    }

    private static String required(
        Function<String, String> environment,
        String reference
    ) {
        String value = environment.apply(reference);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Main persistence environment is unavailable");
        }
        return value;
    }
}
