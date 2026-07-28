package io.github.eariver.wayfarer.core.persistence;

import org.flywaydb.core.Flyway;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MigrationLifecycle implements AutoCloseable {
    private final AtomicBoolean closed = new AtomicBoolean();
    private final int appliedMigrationCount;

    MigrationLifecycle(int appliedMigrationCount) {
        this.appliedMigrationCount = appliedMigrationCount;
    }

    public static MigrationLifecycle migrate(
        MariaDbPool pool,
        List<String> configuredLocations
    ) {
        Objects.requireNonNull(pool, "pool");
        List<String> locations = MigrationLocations.canonicalize(configuredLocations);
        try {
            Flyway.configure(MigrationLifecycle.class.getClassLoader())
                .dataSource(pool.dataSource())
                .locations(locations.toArray(String[]::new))
                .cleanDisabled(true)
                .failOnMissingLocations(true)
                .ignoreMigrationPatterns("*:pending")
                .load()
                .validate();
            Flyway flyway = Flyway.configure(MigrationLifecycle.class.getClassLoader())
                .dataSource(pool.dataSource())
                .locations(locations.toArray(String[]::new))
                .cleanDisabled(true)
                .failOnMissingLocations(true)
                .load();
            flyway.migrate();
            flyway.validate();
            int applied = flyway.info().applied().length;
            if (applied < 1) {
                throw new PersistenceException("No Core migration was applied");
            }
            return new MigrationLifecycle(applied);
        } catch (PersistenceException failure) {
            throw failure;
        } catch (RuntimeException failure) {
            throw new PersistenceException("Database migration failed");
        }
    }

    public int appliedMigrationCount() {
        return appliedMigrationCount;
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        closed.set(true);
    }
}
