package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalDatabaseTest {
    @Test
    void mainThreadGuardFailsBeforeConnectionAcquisition() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = database(
                dataSource,
                executor,
                () -> true,
                new PersistenceWorkGate()
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
            PersistenceWorkGate gate = new PersistenceWorkGate();
            InternalDatabase database = database(dataSource, executor, () -> false, gate);
            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> database.read(connection -> 1).toCompletableFuture().join()
            );
            assertEquals("Database read failed", failure.getCause().getMessage());
            assertFalse(failure.getCause().getMessage().contains("secret"));
            assertEquals(
                PersistenceDrainStatus.DRAINED,
                gate.stopAcceptingAndAwait(Duration.ofSeconds(1)).status()
            );
        }
    }

    @Test
    void transactionCommitsAndRollsBackExplicitly() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection success = mock(Connection.class);
        Connection failure = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(success, failure);
        try (ManagedExecutor executor = executor()) {
            PersistenceWorkGate gate = new PersistenceWorkGate();
            InternalDatabase database = database(dataSource, executor, () -> false, gate);
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
            assertEquals(0, database.inFlightCount());
            assertEquals(
                PersistenceDrainStatus.DRAINED,
                gate.stopAcceptingAndAwait(Duration.ofSeconds(1)).status()
            );
        }
    }

    @Test
    void rejectsNewOperationAfterIntakeStopsWithoutAcquiringConnection() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        PersistenceWorkGate gate = new PersistenceWorkGate();
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = database(dataSource, executor, () -> false, gate);
            assertEquals(
                PersistenceDrainStatus.DRAINED,
                gate.stopAcceptingAndAwait(Duration.ofSeconds(1)).status()
            );

            assertThrows(
                RejectedExecutionException.class,
                () -> database.read(connection -> 1)
            );
            assertThrows(
                RejectedExecutionException.class,
                () -> database.transaction(connection -> 1)
            );
            verify(dataSource, never()).getConnection();
        }
    }

    @Test
    void acceptedOperationCompletesAfterIntakeStops() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        PersistenceWorkGate gate = new PersistenceWorkGate();
        try (ManagedExecutor executor = executor()) {
            InternalDatabase database = database(dataSource, executor, () -> false, gate);
            CompletableFuture<Integer> operation = database.read(ignored -> {
                started.countDown();
                await(release);
                return 41;
            }).toCompletableFuture();
            assertTrue(started.await(1, TimeUnit.SECONDS));

            CompletableFuture<PersistenceDrainResult> drain = CompletableFuture.supplyAsync(
                () -> gate.stopAcceptingAndAwait(Duration.ofSeconds(1))
            );
            assertTrue(database.awaitIntakeStopped(Duration.ofSeconds(1)));
            release.countDown();

            assertEquals(41, operation.get(1, TimeUnit.SECONDS));
            assertEquals(
                PersistenceDrainStatus.DRAINED,
                drain.get(1, TimeUnit.SECONDS).status()
            );
            assertEquals(0, database.inFlightCount());
        }
    }

    @Test
    void executorSubmissionRejectionReleasesPermit() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        PersistenceWorkGate gate = new PersistenceWorkGate();
        ManagedExecutor executor = executor();
        executor.shutdown();
        InternalDatabase database = database(dataSource, executor, () -> false, gate);

        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> database.read(connection -> 1).toCompletableFuture().join()
        );
        assertTrue(failure.getCause() instanceof RejectedExecutionException);
        assertEquals(0, database.inFlightCount());
        assertEquals(
            PersistenceDrainStatus.DRAINED,
            gate.stopAcceptingAndAwait(Duration.ofSeconds(1)).status()
        );
        verify(dataSource, never()).getConnection();
    }

    private static void await(CountDownLatch latch) throws SQLException {
        try {
            if (!latch.await(1, TimeUnit.SECONDS)) {
                throw new SQLException("test latch timed out");
            }
        } catch (InterruptedException interruption) {
            Thread.currentThread().interrupt();
            throw new SQLException("test latch was interrupted");
        }
    }

    private static InternalDatabase database(
        DataSource dataSource,
        ManagedExecutor executor,
        ThreadContext threadContext,
        PersistenceWorkGate gate
    ) {
        return new InternalDatabase(
            dataSource,
            executor,
            threadContext,
            gate,
            () -> true
        );
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
