package io.github.eariver.wayfarer.core.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationLifecycleTest {
    @Test
    void closeIsIdempotent() {
        MigrationLifecycle migration = new MigrationLifecycle(1);
        migration.close();
        migration.close();
        assertTrue(migration.isClosed());
    }
}
