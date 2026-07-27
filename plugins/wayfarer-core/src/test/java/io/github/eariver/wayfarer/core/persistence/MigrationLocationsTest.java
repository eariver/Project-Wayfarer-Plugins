package io.github.eariver.wayfarer.core.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MigrationLocationsTest {
    @Test
    void canonicalizesExistingCoreLocation() {
        assertEquals(
            List.of("classpath:db/migration/core"),
            MigrationLocations.canonicalize(List.of("db/migration/core"))
        );
    }

    @Test
    void preservesCanonicalClasspathLocation() {
        assertEquals(
            "classpath:db/migration/core",
            MigrationLocations.canonicalize("classpath:/db/migration/core")
        );
    }

    @Test
    void rejectsTraversalAndNonClasspathSchemes() {
        assertThrows(
            PersistenceException.class,
            () -> MigrationLocations.canonicalize("../migration")
        );
        assertThrows(
            PersistenceException.class,
            () -> MigrationLocations.canonicalize("filesystem:migration")
        );
    }
}
