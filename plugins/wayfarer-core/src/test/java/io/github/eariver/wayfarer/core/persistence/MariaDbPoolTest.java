package io.github.eariver.wayfarer.core.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.eariver.wayfarer.common.secret.SecretValue;
import io.github.eariver.wayfarer.core.config.CoreConfig;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class MariaDbPoolTest {
    @Test
    void mapsTypedSettingsToFailFastPoolConfiguration() {
        try (CoreConfig.MariaDbSettings settings = settings(true)) {
            HikariConfig hikari = MariaDbPool.configuration("alpha.2 server", settings);
            assertEquals("jdbc:mariadb://example/wayfarer", hikari.getJdbcUrl());
            assertEquals("wayfarer", hikari.getUsername());
            assertEquals("Wayfarer-Core-alpha-2-server", hikari.getPoolName());
            assertEquals(7, hikari.getMaximumPoolSize());
            assertEquals(2, hikari.getMinimumIdle());
            assertEquals(750, hikari.getConnectionTimeout());
            assertEquals(750, hikari.getInitializationFailTimeout());
            assertTrue(hikari.isAutoCommit());
            assertEquals("TRANSACTION_READ_COMMITTED", hikari.getTransactionIsolation());
            assertEquals("SET time_zone = '+00:00'", hikari.getConnectionInitSql());
            assertFalse(hikari.isRegisterMbeans());
        }
    }

    @Test
    void disabledPoolFailsBeforeReadingCredentials() {
        try (CoreConfig.MariaDbSettings settings = settings(false)) {
            PersistenceException failure = assertThrows(
                PersistenceException.class,
                () -> MariaDbPool.open("test", settings)
            );
            assertEquals("MariaDB is disabled", failure.getMessage());
        }
    }

    @Test
    void poolNameIsBoundedAndSanitized() {
        String poolName = MariaDbPool.poolName("bad id.with spaces/and symbols");
        assertEquals("Wayfarer-Core-bad-id-with-spaces-and-symbols", poolName);
        assertTrue(MariaDbPool.poolName("x".repeat(100)).length() <= 64);
    }

    @Test
    void closeIsIdempotent() {
        HikariDataSource dataSource = mock(HikariDataSource.class);
        MariaDbPool pool = new MariaDbPool(dataSource);
        pool.close();
        pool.close();
        assertTrue(pool.isClosed());
        verify(dataSource, times(1)).close();
    }

    private static CoreConfig.MariaDbSettings settings(boolean enabled) {
        return new CoreConfig.MariaDbSettings(
            enabled,
            "URL",
            "USER",
            "PASSWORD",
            7,
            2,
            Duration.ofMillis(750),
            enabled ? SecretValue.of("jdbc:mariadb://example/wayfarer") : null,
            enabled ? SecretValue.of("wayfarer") : null,
            enabled ? SecretValue.of("top-secret") : null
        );
    }
}
