package io.github.eariver.wayfarer.frontier.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Objects;
import java.util.function.Function;

public final class FrontierModulePersistence implements AutoCloseable {
    public static final String HISTORY_TABLE = "wf_frontier_flyway_schema_history";
    private final HikariDataSource dataSource;

    private FrontierModulePersistence(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static FrontierModulePersistence open(
        FrontierModuleConfig.DatabaseConfig database,
        Function<String, String> environment
    ) {
        Objects.requireNonNull(database, "database");
        Objects.requireNonNull(environment, "environment");
        HikariConfig hikari = new HikariConfig();
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        hikari.setJdbcUrl(required(environment, database.jdbcUrlReference()));
        hikari.setUsername(required(environment, database.usernameReference()));
        hikari.setPassword(required(environment, database.passwordReference()));
        hikari.setMaximumPoolSize(database.maximumPoolSize());
        hikari.setMinimumIdle(database.minimumIdle());
        hikari.setConnectionTimeout(database.connectionTimeoutMillis());
        hikari.setPoolName("Wayfarer-Frontier");
        HikariDataSource source = new HikariDataSource(hikari);
        try {
            Flyway.configure(FrontierModulePersistence.class.getClassLoader())
                .dataSource(source)
                .table(HISTORY_TABLE)
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .locations("classpath:db/migration/frontier")
                .load()
                .migrate();
            return new FrontierModulePersistence(source);
        } catch (RuntimeException failure) {
            source.close();
            throw new IllegalStateException(
                "Frontier module persistence initialization failed"
            );
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
            throw new IllegalStateException(
                "Frontier persistence environment is unavailable"
            );
        }
        return value;
    }
}
