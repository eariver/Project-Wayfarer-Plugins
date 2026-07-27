package io.github.eariver.wayfarer.api;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WayfarerItemIdentityContractTest {
    private static final Set<String> FORBIDDEN_PACKAGES = Set.of(
        "java.sql",
        "javax.sql",
        "com.zaxxer",
        "org.flywaydb",
        "org.bukkit",
        "io.papermc",
        "io.github.eariver.wayfarer.core"
    );

    @Test
    void operationsAreAsynchronousAndPublicTypesAreJdkOnly() {
        for (Method method : WayfarerItemIdentity.class.getDeclaredMethods()) {
            assertEquals(CompletionStage.class, method.getReturnType());
        }
        for (Class<?> type : WayfarerItemIdentity.class.getDeclaredClasses()) {
            for (var component : type.isRecord() ? type.getRecordComponents() : new java.lang.reflect.RecordComponent[0]) {
                String name = component.getGenericType().getTypeName();
                assertFalse(
                    FORBIDDEN_PACKAGES.stream().anyMatch(name::contains),
                    () -> "Forbidden public type: " + name
                );
            }
        }
    }

    @Test
    void failureReasonsCoverEveryFailClosedOutcome() {
        assertTrue(Set.of(WayfarerItemIdentity.FailureReason.values()).containsAll(Set.of(
            WayfarerItemIdentity.FailureReason.MISSING_FIELD,
            WayfarerItemIdentity.FailureReason.INVALID_ITEM_INSTANCE_ID,
            WayfarerItemIdentity.FailureReason.INVALID_OWNER_UUID,
            WayfarerItemIdentity.FailureReason.INVALID_INSTANCE_EPOCH,
            WayfarerItemIdentity.FailureReason.INVALID_SCHEMA_VERSION,
            WayfarerItemIdentity.FailureReason.INVALID_DISPLAY_REVISION,
            WayfarerItemIdentity.FailureReason.UNKNOWN_ITEM_TYPE,
            WayfarerItemIdentity.FailureReason.UNKNOWN_SCHEMA_VERSION,
            WayfarerItemIdentity.FailureReason.IDENTITY_NOT_FOUND,
            WayfarerItemIdentity.FailureReason.ITEM_TYPE_MISMATCH,
            WayfarerItemIdentity.FailureReason.OWNER_MISMATCH,
            WayfarerItemIdentity.FailureReason.EPOCH_MISMATCH,
            WayfarerItemIdentity.FailureReason.SCHEMA_MISMATCH,
            WayfarerItemIdentity.FailureReason.DISPLAY_REVISION_MISMATCH
        )));
    }
}
