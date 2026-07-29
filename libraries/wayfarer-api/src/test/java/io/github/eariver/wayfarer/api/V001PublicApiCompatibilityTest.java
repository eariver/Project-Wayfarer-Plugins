package io.github.eariver.wayfarer.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

/**
 * Executable binary-surface baseline captured from product commit
 * 49e00e21716c1c13a2dbb170fdad1b19c4275612 (V0.0.1).
 *
 * <p>The assertions intentionally require the old members while permitting compatible additions.
 * Changing or removing an asserted member requires an explicit compatibility decision.</p>
 */
final class V001PublicApiCompatibilityTest {
    @Test
    void preservesV001ServiceMethods() throws ReflectiveOperationException {
        method(WayfarerServices.class, "serverId", String.class);
        method(WayfarerServices.class, "configVersion", int.class);
        method(WayfarerServices.class, "lifecycleState", WayfarerLifecycleState.class);
        method(WayfarerServices.class, "database", WayfarerDatabase.class);
        method(WayfarerServices.class, "audit", WayfarerAudit.class);
        method(WayfarerServices.class, "transactions", WayfarerTransactions.class);
        method(WayfarerServices.class, "waymark", WayfarerWaymark.class);
        method(WayfarerServices.class, "itemIdentity", WayfarerItemIdentity.class);
        method(WayfarerServices.class, "tasks", WayfarerTasks.class);
        method(WayfarerServices.class, "health", WayfarerHealth.class);

        method(WayfarerAudit.class, "record", CompletionStage.class, WayfarerAudit.AuditEvent.class);
        method(WayfarerHealth.class, "snapshot", WayfarerHealth.HealthSnapshot.class);
        method(WayfarerHealth.class, "overall", WayfarerHealth.Status.class);
        method(WayfarerHealth.class, "components", Map.class);
        method(WayfarerItemIdentity.class, "create", CompletionStage.class,
            WayfarerItemIdentity.CreateRequest.class);
        method(WayfarerItemIdentity.class, "find", CompletionStage.class, UUID.class);
        method(WayfarerItemIdentity.class, "validate", CompletionStage.class,
            WayfarerItemIdentity.ValidationRequest.class);
        method(WayfarerTasks.class, "database", CompletionStage.class, Supplier.class);
        method(WayfarerTasks.class, "bridge", CompletionStage.class, Object.class,
            Function.class, Predicate.class, Consumer.class);
        method(WayfarerTasks.class, "mainThread", CompletionStage.class, Runnable.class);
        method(WayfarerTransactions.class, "execute", CompletionStage.class,
            WayfarerTransactions.TransactionRequest.class);
        method(WayfarerTransactions.class, "reconcile", CompletionStage.class, UUID.class);
        method(WayfarerTransactions.class, "reconcile", CompletionStage.class, UUID.class,
            WayfarerTransactions.ReconcileAction.class);
        method(WayfarerTransactions.class, "inspect", CompletionStage.class, UUID.class);
        method(WayfarerWaymark.class, "balance", CompletionStage.class, UUID.class);
        method(WayfarerWaymark.class, "debit", CompletionStage.class,
            UUID.class, long.class, String.class);
        method(WayfarerWaymark.class, "credit", CompletionStage.class,
            UUID.class, long.class, String.class);
        method(WayfarerWaymark.class, "refund", CompletionStage.class,
            UUID.class, long.class, String.class);
        method(WayfarerWaymarkProvider.class, "probe", CompletionStage.class);
        method(WayfarerWaymarkProvider.class, "balance", CompletionStage.class, UUID.class);
        method(WayfarerWaymarkProvider.class, "debit", CompletionStage.class,
            UUID.class, long.class, String.class);
        method(WayfarerWaymarkProvider.class, "refund", CompletionStage.class,
            UUID.class, long.class, String.class, String.class);
        method(WayfarerWaymarkProvider.class, "resolve", CompletionStage.class,
            WayfarerWaymarkProvider.EffectKind.class, String.class, String.class);
    }

    @Test
    void preservesV001RecordConstructorsAndAccessors() {
        record(WayfarerAudit.AuditEvent.class,
            "eventId", UUID.class, "eventType", String.class, "actorUuid", UUID.class,
            "subjectType", String.class, "subjectId", String.class, "serverId", String.class,
            "detailsJson", String.class, "occurredAt", Instant.class);
        record(WayfarerHealth.ComponentHealth.class,
            "component", String.class, "status", WayfarerHealth.Status.class,
            "timestamp", Instant.class, "detail", String.class);
        record(WayfarerHealth.HealthSnapshot.class,
            "overall", WayfarerHealth.Status.class, "timestamp", Instant.class,
            "lifecycleState", WayfarerLifecycleState.class, "components", Map.class);
        record(WayfarerItemIdentity.CreateRequest.class,
            "itemType", String.class, "ownerUuid", UUID.class, "instanceEpoch", long.class,
            "schemaVersion", int.class, "displayRevision", int.class);
        record(WayfarerItemIdentity.Identity.class,
            "itemInstanceId", UUID.class, "itemType", String.class, "ownerUuid", UUID.class,
            "instanceEpoch", long.class, "schemaVersion", int.class, "displayRevision", int.class,
            "createdAt", Instant.class, "updatedAt", Instant.class, "lockVersion", long.class);
        record(WayfarerItemIdentity.RawClaim.class,
            "itemInstanceId", String.class, "itemType", String.class, "ownerUuid", String.class,
            "instanceEpoch", Long.class, "schemaVersion", Integer.class,
            "displayRevision", Integer.class);
        record(WayfarerItemIdentity.ValidationRequest.class,
            "claim", WayfarerItemIdentity.RawClaim.class, "allowedItemTypes", Set.class,
            "supportedSchemaVersions", Set.class, "expectedOwnerUuid", UUID.class,
            "expectedInstanceEpoch", OptionalLong.class);
        record(WayfarerItemIdentity.ValidationResult.class,
            "valid", boolean.class, "identity", Optional.class,
            "failureReason", Optional.class);
        record(WayfarerTasks.TaskBridgeResult.class,
            "immutableResult", Object.class, "applied", boolean.class);
        record(WayfarerTransactions.TransactionRequest.class,
            "idempotencyKey", String.class, "transactionType", String.class,
            "actorUuid", UUID.class, "subjectType", String.class, "subjectId", String.class,
            "amountWaymark", long.class, "payloadJson", String.class);
        record(WayfarerTransactions.TransactionResult.class,
            "transactionId", UUID.class, "state", WayfarerTransactions.State.class,
            "failureCode", String.class);
        record(WayfarerWaymark.OperationResult.class,
            "success", boolean.class, "providerReference", String.class,
            "failureCode", String.class);
        record(WayfarerWaymarkProvider.ProbeResult.class,
            "available", boolean.class, "providerId", String.class, "failureCode", String.class);
        record(WayfarerWaymarkProvider.EffectResult.class,
            "status", WayfarerWaymarkProvider.EffectStatus.class,
            "providerReference", String.class, "failureCode", String.class);
        record(WayfarerWaymarkProvider.ResolutionResult.class,
            "status", WayfarerWaymarkProvider.ResolutionStatus.class,
            "providerReference", String.class, "failureCode", String.class);
    }

    @Test
    void preservesV001EnumConstants() {
        constants(WayfarerLifecycleState.class,
            "NEW", "INITIALIZING", "ENABLED", "STOPPING", "DISABLED", "FAILED");
        constants(WayfarerHealth.Status.class,
            "UP", "DEGRADED", "DOWN", "UNKNOWN", "DISABLED");
        constants(WayfarerTransactions.ReconcileAction.class,
            "AUTO", "COMMIT", "REFUND", "FAIL");
        constants(WayfarerTransactions.State.class,
            "PREPARED", "DEBIT_PENDING", "DEBITED", "DOMAIN_COMMIT_PENDING", "COMMITTED",
            "REFUND_PENDING", "REFUNDED", "UNKNOWN", "RECONCILED_COMMITTED",
            "RECONCILED_REFUNDED", "FAILED");
        constants(WayfarerWaymarkProvider.EffectStatus.class,
            "SUCCEEDED", "INSUFFICIENT_FUNDS", "KNOWN_FAILURE", "UNKNOWN");
        constants(WayfarerWaymarkProvider.EffectKind.class, "DEBIT", "REFUND");
        constants(WayfarerWaymarkProvider.ResolutionStatus.class,
            "APPLIED", "NOT_APPLIED", "UNKNOWN");
    }

    private static void method(
        Class<?> owner,
        String name,
        Class<?> returnType,
        Class<?>... parameterTypes
    ) throws ReflectiveOperationException {
        Method reflected = owner.getMethod(name, parameterTypes);
        assertEquals(returnType, reflected.getReturnType(), owner.getName() + "#" + name);
    }

    private static void record(Class<?> type, Object... expectedNameAndTypePairs) {
        assertTrue(type.isRecord(), type.getName());
        Object[] actual = new Object[type.getRecordComponents().length * 2];
        for (int index = 0; index < type.getRecordComponents().length; index++) {
            actual[index * 2] = type.getRecordComponents()[index].getName();
            actual[index * 2 + 1] = type.getRecordComponents()[index].getType();
        }
        assertArrayEquals(expectedNameAndTypePairs, actual, type.getName());
    }

    private static <E extends Enum<E>> void constants(Class<E> type, String... requiredNames) {
        Set<String> actual = java.util.Arrays.stream(type.getEnumConstants())
            .map(Enum::name)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (String required : requiredNames) {
            assertTrue(actual.contains(required), type.getName() + "." + required);
        }
    }
}
