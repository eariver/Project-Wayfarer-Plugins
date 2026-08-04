package io.github.eariver.wayfarer.main.application;

import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

public final class GrowthSessionStore {
    private final ConcurrentHashMap<UUID, Session> sessions = new ConcurrentHashMap<>();

    public void open(GrowthTool tool) {
        Objects.requireNonNull(tool, "tool");
        sessions.put(tool.ownerUuid(), new Session(tool, false));
    }

    public Optional<GrowthTool> current(UUID ownerUuid) {
        Session session = sessions.get(ownerUuid);
        return session == null ? Optional.empty() : Optional.of(session.snapshot());
    }

    public GrowthTool addProgress(UUID ownerUuid, long units, Instant now) {
        Session session = require(ownerUuid);
        synchronized (session) {
            session.tool = session.tool.addProgress(units, now);
            session.dirty = true;
            return session.tool;
        }
    }

    public GrowthTool update(UUID ownerUuid, UnaryOperator<GrowthTool> update) {
        Session session = require(ownerUuid);
        synchronized (session) {
            session.tool = Objects.requireNonNull(
                update.apply(session.tool),
                "updated tool"
            );
            session.dirty = true;
            return session.tool;
        }
    }

    public Set<UUID> ownerUuids() {
        return Set.copyOf(sessions.keySet());
    }

    public Optional<GrowthTool> takeDirty(UUID ownerUuid) {
        Session session = sessions.get(ownerUuid);
        if (session == null) {
            return Optional.empty();
        }
        synchronized (session) {
            if (!session.dirty) {
                return Optional.empty();
            }
            session.dirty = false;
            return Optional.of(session.tool);
        }
    }

    public List<GrowthTool> takeAllDirty() {
        List<GrowthTool> dirty = new ArrayList<>();
        sessions.forEach((owner, session) -> takeDirty(owner).ifPresent(dirty::add));
        return List.copyOf(dirty);
    }

    public void restoreDirty(GrowthTool tool) {
        Session session = sessions.get(tool.ownerUuid());
        if (session == null) {
            return;
        }
        synchronized (session) {
            if (session.tool.toolId().equals(tool.toolId())
                && session.tool.instanceEpoch() == tool.instanceEpoch()) {
                session.dirty = true;
            }
        }
    }

    public void acceptCheckpoint(GrowthTool submitted, GrowthTool persisted) {
        Objects.requireNonNull(submitted, "submitted");
        Objects.requireNonNull(persisted, "persisted");
        Session session = sessions.get(submitted.ownerUuid());
        if (session == null) {
            return;
        }
        synchronized (session) {
            if (session.tool.equals(submitted)) {
                session.tool = persisted;
            }
        }
    }

    public Optional<GrowthTool> close(UUID ownerUuid) {
        Session removed = sessions.remove(ownerUuid);
        return removed == null || !removed.dirty
            ? Optional.empty()
            : Optional.of(removed.snapshot());
    }

    private Session require(UUID ownerUuid) {
        Session session = sessions.get(ownerUuid);
        if (session == null) {
            throw new IllegalStateException("Growth session is unavailable");
        }
        return session;
    }

    private static final class Session {
        private GrowthTool tool;
        private boolean dirty;

        private Session(GrowthTool tool, boolean dirty) {
            this.tool = tool;
            this.dirty = dirty;
        }

        private synchronized GrowthTool snapshot() {
            return tool;
        }
    }
}
