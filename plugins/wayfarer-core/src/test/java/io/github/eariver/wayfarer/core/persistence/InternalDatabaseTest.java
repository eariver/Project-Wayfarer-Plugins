package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalDatabaseTest {
    @Test
    void mainThreadGuardFailsBeforeConnectionAcquisition() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = new InternalDatabase(
                dataSource,
                executor,
                () -> true,
                () -> true,
                () -> true
            );
            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> database.read(connection -> 1).toCompletableFuture().join()
            );
            assertEquals(
                "JDBC access is forbidden on the main thread",
                failure.getCause().getMessage()
            );
            verify(dataSource, never()).getConnection();
        }
    }

    @Test
    void sqlFailureIsSanitized() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(
            new SQLException("jdbc:mariadb://secret-host/db password=secret")
        );
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = new InternalDatabase(
                dataSource,
                executor,
                () -> false,
                () -> true,
                () -> true
            );
            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> database.read(connection -> 1).toCompletableFuture().join()
            );
            assertEquals("Database read failed", failure.getCause().getMessage());
            assertFalse(failure.getCause().getMessage().contains("secret"));
        }
    }

    @Test
    void transactionCommitsAndRollsBackExplicitly() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection success = mock(Connection.class);
        Connection failure = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(success, failure);
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = new InternalDatabase(
                dataSource,
                executor,
                () -> false,
                () -> true,
                () -> true
            );
            assertEquals(
                7,
                database.transaction(connection -> 7).toCompletableFuture().join()
            );
            verify(success).setAutoCommit(false);
            verify(success).setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            verify(success).commit();

            assertThrows(
                CompletionException.class,
                () -> database.transaction(connection -> {
                    throw new SQLException("private database diagnostic");
                }).toCompletableFuture().join()
            );
            verify(failure).rollback();
        }
    }

    private static ManagedExecutor executor() {
        return new ManagedExecutor(
            1,
            "Wayfarer-Database-Test",
            Duration.ofSeconds(1),
            ignored -> {},
            ignored -> {}
        );
    }
}
