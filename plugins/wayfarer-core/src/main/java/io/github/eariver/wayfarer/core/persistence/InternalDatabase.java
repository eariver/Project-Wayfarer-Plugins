package io.github.eariver.wayfarer.core.persistence;

import io.github.eariver.wayfarer.core.task.ManagedExecutor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BooleanSupplier;

final class InternalDatabase {
    private final DataSource dataSource;
    private final ManagedExecutor executor;
    private final ThreadContext threadContext;
    private final PersistenceWorkGate workGate;
    private final BooleanSupplier poolOpen;

    InternalDatabase(
        DataSource dataSource,
        ManagedExecutor executor,
        ThreadContext threadContext,
        PersistenceWorkGate workGate,
        BooleanSupplier poolOpen
    ) {
        this.dataSource = Objects.requireNonNull(dataSource, "dataSource");
        this.executor = Objects.requireNonNull(executor, "executor");
        this.threadContext = Objects.requireNonNull(threadContext, "threadContext");
        this.workGate = Objects.requireNonNull(workGate, "workGate");
        this.poolOpen = Objects.requireNonNull(poolOpen, "poolOpen");
    }

    <T> CompletionStage<T> read(SqlOperation<T> operation) {
        Objects.requireNonNull(operation, "operation");
        return submit(() -> executeRead(operation));
    }

    <T> CompletionStage<T> transaction(SqlOperation<T> operation) {
        Objects.requireNonNull(operation, "operation");
        return submit(() -> executeTransaction(operation));
    }

    private <T> T executeRead(SqlOperation<T> operation) {
        ensureDatabaseThread();
        ensurePoolOpen();
        try (Connection connection = dataSource.getConnection()) {
            return operation.apply(connection);
        } catch (SQLException failure) {
            throw new PersistenceException("Database read failed");
        }
    }

    private <T> T executeTransaction(SqlOperation<T> operation) {
        ensureDatabaseThread();
        ensurePoolOpen();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            try {
                T result = operation.apply(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException failure) {
                rollback(connection);
                throw failure;
            }
        } catch (PersistenceException failure) {
            throw failure;
        } catch (SQLException failure) {
            throw new PersistenceException("Database transaction failed");
        } catch (RuntimeException failure) {
            throw new PersistenceException("Database transaction failed");
        }
    }

    private void ensureDatabaseThread() {
        if (threadContext.isMainThread()) {
            throw new PersistenceException("JDBC access is forbidden on the main thread");
        }
    }

    boolean isAccepting() {
        return workGate.isAccepting();
    }

    int inFlightCount() {
        return workGate.inFlightCount();
    }

    boolean awaitIntakeStopped(Duration timeout) {
        return workGate.awaitIntakeStopped(timeout);
    }

    private <T> CompletionStage<T> submit(Callable<T> operation) {
        PersistenceWorkGate.Permit permit = workGate.acquire();
        CompletableFuture<T> result;
        try {
            result = executor.submit(() -> {
                try {
                    return operation.call();
                } finally {
                    permit.close();
                }
            });
        } catch (RuntimeException submissionFailure) {
            permit.close();
            throw submissionFailure;
        }
        result.whenComplete((ignored, failure) -> {
            if (isSubmissionRejection(failure)) {
                permit.close();
            }
        });
        return result;
    }

    private void ensurePoolOpen() {
        if (!poolOpen.getAsBoolean()) {
            throw new RejectedExecutionException("MariaDB pool is closed");
        }
    }

    private static boolean isSubmissionRejection(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof RejectedExecutionException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // The sanitized operation failure remains authoritative.
        }
    }

    @FunctionalInterface
    interface SqlOperation<T> {
        T apply(Connection connection) throws SQLException;
    }
}
