package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.api.WayfarerTasks;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GrowthCheckpointService {
    private final GrowthSessionStore sessions;
    private final GrowthToolRepository repository;
    private final WayfarerTasks tasks;
    private final Clock clock;
    private final AtomicBoolean accepting = new AtomicBoolean(true);

    public GrowthCheckpointService(
        GrowthSessionStore sessions,
        GrowthToolRepository repository,
        WayfarerTasks tasks,
        Clock clock
    ) {
        this.sessions = Objects.requireNonNull(sessions, "sessions");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.tasks = Objects.requireNonNull(tasks, "tasks");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public CompletionStage<Boolean> checkpoint(UUID ownerUuid) {
        if (!accepting.get()) {
            return CompletableFuture.completedFuture(false);
        }
        return sessions.takeDirty(ownerUuid)
            .map(this::persist)
            .orElseGet(() -> CompletableFuture.completedFuture(true));
    }

    public CompletionStage<Integer> stopAndFlush() {
        accepting.set(false);
        List<GrowthTool> dirty = sessions.takeAllDirty();
        CompletionStage<Integer> result = CompletableFuture.completedFuture(0);
        for (GrowthTool tool : dirty) {
            result = result.thenCompose(count ->
                persist(tool).thenApply(saved -> saved ? count + 1 : count)
            );
        }
        return result;
    }

    private CompletionStage<Boolean> persist(GrowthTool tool) {
        return tasks.database(() ->
            repository.checkpoint(tool, tool.lockVersion(), clock.instant())
        ).handle((saved, failure) -> {
            if (failure != null || !saved) {
                sessions.restoreDirty(tool);
                return false;
            }
            return true;
        });
    }
}
