package io.github.eariver.wayfarer.frontier.integration;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Objects;
import java.util.function.Predicate;

public final class WorldEditLaunchpadProtection implements AutoCloseable {
    private final Predicate<Coordinate> protectedCoordinate;

    public WorldEditLaunchpadProtection(
        Predicate<Coordinate> protectedCoordinate
    ) {
        this.protectedCoordinate = Objects.requireNonNull(
            protectedCoordinate,
            "protectedCoordinate"
        );
        WorldEdit.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        if (event.getStage() != EditSession.Stage.BEFORE_CHANGE
            || event.getWorld() == null) {
            return;
        }
        String worldName = event.getWorld().getName();
        event.setExtent(new ProtectedExtent(
            event.getExtent(),
            worldName,
            protectedCoordinate
        ));
    }

    @Override
    public void close() {
        WorldEdit.getInstance().getEventBus().unregister(this);
    }

    public record Coordinate(String worldName, int x, int y, int z) {}

    private static final class ProtectedExtent
        extends AbstractDelegateExtent {
        private final String worldName;
        private final Predicate<Coordinate> protectedCoordinate;

        private ProtectedExtent(
            Extent extent,
            String worldName,
            Predicate<Coordinate> protectedCoordinate
        ) {
            super(extent);
            this.worldName = worldName;
            this.protectedCoordinate = protectedCoordinate;
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(
            BlockVector3 location,
            T block
        ) throws WorldEditException {
            if (protectedCoordinate.test(new Coordinate(
                worldName,
                location.x(),
                location.y(),
                location.z()
            ))) {
                return false;
            }
            return super.setBlock(location, block);
        }
    }
}
