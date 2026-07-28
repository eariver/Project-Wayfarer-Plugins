package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerTransactions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionCommandHandlerTest {
    private static final UUID ID =
        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void inspectRequiresPermissionAndAuditsDenial() {
        Fixture fixture = fixture(false, false);
        fixture.handler.execute(
            fixture.audience,
            new String[]{"admin", "transaction", "inspect", ID.toString()}
        );

        assertEquals(0, fixture.transactions.inspectCalls);
        assertEquals("ADMIN_TRANSACTION_INSPECT_PERMISSION_DENIED", fixture.events.get(0));
    }

    @Test
    void inspectRedactsProviderReferenceAndRespondsOnDispatcher() {
        Fixture fixture = fixture(true, false);
        fixture.handler.execute(
            fixture.audience,
            new String[]{"admin", "transaction", "inspect", ID.toString()}
        );

        assertEquals(1, fixture.transactions.inspectCalls);
        assertTrue(fixture.audience.messages.get(0).contains("debitReference=present"));
        assertTrue(fixture.audience.messages.stream().noneMatch(
            message -> message.contains("provider-secret-reference")
        ));
    }

    @Test
    void reconcileRequiresConfirmationBeforeMutation() {
        Fixture fixture = fixture(false, true);
        fixture.handler.execute(
            fixture.audience,
            new String[]{"admin", "transaction", "reconcile", ID.toString(), "refund"}
        );
        assertEquals(0, fixture.transactions.reconcileCalls);
        assertTrue(fixture.audience.messages.get(0).contains("confirm"));
    }

    @Test
    void consoleWithPermissionCanReconcileAndActionIsAudited() {
        Fixture fixture = fixture(false, true);
        fixture.audience.console = true;
        fixture.handler.execute(
            fixture.audience,
            new String[]{
                "admin", "transaction", "reconcile", ID.toString(), "refund", "confirm"
            }
        );
        assertEquals(1, fixture.transactions.reconcileCalls);
        assertEquals(WayfarerTransactions.ReconcileAction.REFUND, fixture.transactions.action);
        assertTrue(fixture.events.contains("ADMIN_TRANSACTION_RECONCILED"));
        assertTrue(fixture.audience.messages.get(0).contains("RECONCILED_REFUNDED"));
    }

    @Test
    void synchronousServiceFailureIsSanitizedForInspectAndReconcile() {
        FakeAudience audience = new FakeAudience(true, true);
        List<String> warnings = new ArrayList<>();
        TransactionCommandHandler handler = new TransactionCommandHandler(
            () -> {
                throw new IllegalStateException("provider-secret-internal-detail");
            },
            ignored -> CompletableFuture.completedFuture(null),
            Runnable::run,
            warnings::add
        );

        assertDoesNotThrow(() -> handler.execute(
            audience,
            new String[]{"admin", "transaction", "inspect", ID.toString()}
        ));
        assertDoesNotThrow(() -> handler.execute(
            audience,
            new String[]{
                "admin", "transaction", "reconcile", ID.toString(), "fail", "confirm"
            }
        ));

        assertEquals(List.of(
            "Transaction service is unavailable; inspect health.",
            "Transaction service is unavailable; inspect health."
        ), audience.messages);
        assertEquals(List.of(
            "Wayfarer transaction service is unavailable",
            "Wayfarer transaction service is unavailable"
        ), warnings);
        assertTrue(audience.messages.stream().noneMatch(
            message -> message.contains("provider-secret-internal-detail")
        ));
    }

    private static Fixture fixture(boolean inspect, boolean reconcile) {
        FakeTransactions transactions = new FakeTransactions();
        FakeAudience audience = new FakeAudience(inspect, reconcile);
        List<String> events = new ArrayList<>();
        TransactionCommandHandler handler = new TransactionCommandHandler(
            () -> transactions,
            event -> {
                events.add(event.eventType());
                return CompletableFuture.completedFuture(null);
            },
            Runnable::run,
            ignored -> {}
        );
        return new Fixture(handler, transactions, audience, events);
    }

    private record Fixture(
        TransactionCommandHandler handler,
        FakeTransactions transactions,
        FakeAudience audience,
        List<String> events
    ) {}

    private static final class FakeTransactions implements WayfarerTransactions {
        private int inspectCalls;
        private int reconcileCalls;
        private ReconcileAction action;

        @Override
        public java.util.concurrent.CompletionStage<TransactionResult> execute(
            TransactionRequest request
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.concurrent.CompletionStage<TransactionResult> reconcile(
            UUID transactionId
        ) {
            return reconcile(transactionId, ReconcileAction.AUTO);
        }

        @Override
        public java.util.concurrent.CompletionStage<TransactionResult> reconcile(
            UUID transactionId,
            ReconcileAction action
        ) {
            reconcileCalls++;
            this.action = action;
            return CompletableFuture.completedFuture(
                new TransactionResult(transactionId, State.RECONCILED_REFUNDED, null)
            );
        }

        @Override
        public java.util.concurrent.CompletionStage<TransactionDetails> inspect(
            UUID transactionId
        ) {
            inspectCalls++;
            return CompletableFuture.completedFuture(new TransactionDetails(
                transactionId,
                "safe-key",
                "SHOP",
                UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"),
                "WAYMARK",
                "subject",
                25,
                State.UNKNOWN,
                "debit-operation",
                "provider-secret-reference",
                "refund-operation",
                "refund-secret-reference",
                "UNCERTAIN",
                3
            ));
        }
    }

    private static final class FakeAudience implements CommandAudience {
        private final boolean inspect;
        private final boolean reconcile;
        private final List<String> messages = new ArrayList<>();
        private boolean console;

        private FakeAudience(boolean inspect, boolean reconcile) {
            this.inspect = inspect;
            this.reconcile = reconcile;
        }

        @Override
        public boolean hasPermission(String permission) {
            return inspect && TransactionCommandHandler.INSPECT_PERMISSION.equals(permission)
                || reconcile
                && TransactionCommandHandler.RECONCILE_PERMISSION.equals(permission);
        }

        @Override
        public boolean console() {
            return console;
        }

        @Override
        public Optional<UUID> actorUuid() {
            return Optional.empty();
        }

        @Override
        public OperationalEvent.AudienceKind audienceKind() {
            return console
                ? OperationalEvent.AudienceKind.CONSOLE
                : OperationalEvent.AudienceKind.PLAYER;
        }

        @Override
        public void sendMessage(String message) {
            messages.add(message);
        }
    }
}
