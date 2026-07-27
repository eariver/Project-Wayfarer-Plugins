package io.github.eariver.wayfarer.core.persistence;

import com.zaxxer.hikari.HikariDataSource;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleCoordinator;
import io.github.eariver.wayfarer.core.lifecycle.LifecycleStep;
import io.github.eariver.wayfarer.core.task.ManagedExecutor;
import io.github.eariver.wayfarer.core.task.ShutdownResult;
import io.github.eariver.wayfarer.core.task.ShutdownStatus;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PersistenceShutdownDrainTest {
    @Test
    void queuedAcceptedOperationsDrainBeforePoolCloseAndExecutorTermination()
        throws Exception {
        List<String> events = new CopyOnWriteArrayList<>();
        List<String> warnings = new CopyOnWriteArrayList<>();
        CountDownLatch taskOneStarted = new CountDownLatch(1);
        CountDownLatch releaseTaskOne = new CountDownLatch(1);
        HikariDataSource dataSource = mock(HikariDataSource.class);
        Connection firstConnection = mock(Connection.class);
        Connection secondConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(firstConnection, secondConnection);
        doAnswer(invocation -> {
            events.add("pool-close");
            return null;
        }).when(dataSource).close();

        ManagedExecutor executor = new ManagedExecutor(
            1,
            "Wayfarer-Persistence-Drain",
            Duration.ofSeconds(1),
            ignored -> {},
            warnings::add
        );
        MariaDbPool pool = new MariaDbPool(dataSource);
        pool.initializeInternalBoundary(executor, () -> false);
        InternalDatabase database = pool.internalDatabaseForTesting();
        AtomicReference<PersistenceDrainResult> drainResult = new AtomicReference<>();
        AtomicReference<ShutdownResult> shutdownResult = new AtomicReference<>();
        LifecycleCoordinator lifecycle = new LifecycleCoordinator(warnings::add);
        lifecycle.enable(
            List.of(
                new LifecycleStep("Config", () -> () -> events.add("config-release")),
                new LifecycleStep("Executor", () -> () -> {
                    shutdownResult.set(executor.shutdown());
                    events.add("executor-terminated");
                }),
                new LifecycleStep("MariaDB", () -> pool),
                new LifecycleStep(
                    "Migration",
                    () -> () -> events.add("migration-release")
                ),
                new LifecycleStep("DatabaseDrain", () -> () -> {
                    drainResult.set(
                        pool.stopAcceptingAndAwait(Duration.ofSeconds(1))
                    );
                    events.add("database-drained");
                })
            ),
            new LifecycleStep("Services", () -> () -> events.add("services-unpublished"))
        );

        var taskOne = database.read(connection -> {
            taskOneStarted.countDown();
            await(releaseTaskOne);
            events.add("task-1-complete");
            return 1;
        }).toCompletableFuture();
        assertTrue(taskOneStarted.await(1, TimeUnit.SECONDS));
        var taskTwo = database.read(connection -> {
            events.add("task-2-complete");
            return 2;
        }).toCompletableFuture();

        Thread disableThread = new Thread(
            lifecycle::disable,
            "Wayfarer-Persistence-Disable-Test"
        );
        disableThread.start();
        assertTrue(database.awaitIntakeStopped(Duration.ofSeconds(1)));
        assertFalse(database.isAccepting());
        releaseTaskOne.countDown();
        disableThread.join(2_000);

        assertFalse(disableThread.isAlive());
        assertEquals(1, taskOne.get(1, TimeUnit.SECONDS));
        assertEquals(2, taskTwo.get(1, TimeUnit.SECONDS));
        assertEquals(PersistenceDrainStatus.DRAINED, drainResult.get().status());
        assertEquals(ShutdownStatus.GRACEFUL, shutdownResult.get().status());
        assertEquals(0, shutdownResult.get().droppedTaskCount());
        assertTrue(pool.isClosed());
        assertTrue(executor.isTerminated());
        assertTrue(warnings.isEmpty());
        assertOrdered(events, "task-1-complete", "task-2-complete");
        assertOrdered(events, "task-2-complete", "pool-close");
        assertOrdered(events, "pool-close", "executor-terminated");
        verify(dataSource, times(1)).close();
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

    private static void assertOrdered(List<String> events, String before, String after) {
        int beforeIndex = events.indexOf(before);
        int afterIndex = events.indexOf(after);
        assertTrue(beforeIndex >= 0, () -> "Missing event " + before + ": " + events);
        assertTrue(afterIndex >= 0, () -> "Missing event " + after + ": " + events);
        assertTrue(
            beforeIndex < afterIndex,
            () -> "Expected " + before + " before " + after + ": " + events
        );
    }
}
