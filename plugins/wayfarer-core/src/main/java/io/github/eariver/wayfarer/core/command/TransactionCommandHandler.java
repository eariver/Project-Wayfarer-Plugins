package io.github.eariver.wayfarer.core.command;

import io.github.eariver.wayfarer.api.WayfarerTransactions;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class TransactionCommandHandler {
    public static final String INSPECT_PERMISSION = "wayfarer.admin.transaction.inspect";
    public static final String RECONCILE_PERMISSION = "wayfarer.admin.transaction.reconcile";
    private static final String USAGE =
        "Usage: /wayfarer admin transaction inspect <id> | "
            + "reconcile <id> <commit|refund|fail> confirm";

    private final Supplier<WayfarerTransactions> transactions;
    private final OperationalEventSink events;
    private final Consumer<Runnable> mainThread;
    private final Consumer<String> warningSink;

    public TransactionCommandHandler(
        Supplier<WayfarerTransactions> transactions,
        OperationalEventSink events,
        Consumer<Runnable> mainThread,
        Consumer<String> warningSink
    ) {
        this.transactions = Objects.requireNonNull(transactions, "transactions");
        this.events = Objects.requireNonNull(events, "events");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
        this.warningSink = Objects.requireNonNull(warningSink, "warningSink");
    }

    public boolean execute(CommandAudience audience, String[] arguments) {
        Objects.requireNonNull(audience, "audience");
        Objects.requireNonNull(arguments, "arguments");
        if (arguments.length < 4
            || !"admin".equalsIgnoreCase(arguments[0])
            || !"transaction".equalsIgnoreCase(arguments[1])) {
            audience.sendMessage(USAGE);
            return false;
        }
        UUID transactionId;
        try {
            transactionId = UUID.fromString(arguments[3]);
        } catch (IllegalArgumentException failure) {
            audience.sendMessage("Transaction ID is invalid.");
            return true;
        }
        return switch (arguments[2].toLowerCase(Locale.ROOT)) {
            case "inspect" -> inspect(audience, arguments, transactionId);
            case "reconcile" -> reconcile(audience, arguments, transactionId);
            default -> {
                audience.sendMessage(USAGE);
                yield false;
            }
        };
    }

    private boolean inspect(
        CommandAudience audience,
        String[] arguments,
        UUID transactionId
    ) {
        if (arguments.length != 4) {
            audience.sendMessage(USAGE);
            return false;
        }
        if (!audience.hasPermission(INSPECT_PERMISSION)) {
            audience.sendMessage("You do not have permission to inspect transactions.");
            safeEvent("ADMIN_TRANSACTION_INSPECT_PERMISSION_DENIED", audience, transactionId);
            return true;
        }
        complete(
            audience,
            transactions.get().inspect(transactionId),
            details -> audience.sendMessage(
                "Transaction " + details.transactionId()
                    + " | state=" + details.state()
                    + " | type=" + details.transactionType()
                    + " | subject=" + details.subjectType() + ":" + details.subjectId()
                    + " | amount=" + details.amountWaymark()
                    + " | debitOperation="
                    + presence(details.debitOperationId())
                    + " | debitReference="
                    + presence(details.debitProviderReference())
                    + " | refundOperation="
                    + presence(details.refundOperationId())
                    + " | refundReference="
                    + presence(details.refundProviderReference())
                    + " | failure="
                    + (details.failureCode() == null ? "none" : details.failureCode())
                    + " | version=" + details.lockVersion()
            )
        );
        return true;
    }

    private boolean reconcile(
        CommandAudience audience,
        String[] arguments,
        UUID transactionId
    ) {
        if (arguments.length != 6 || !"confirm".equalsIgnoreCase(arguments[5])) {
            audience.sendMessage(
                "Reconciliation requires an explicit trailing 'confirm'."
            );
            return true;
        }
        if (!audience.hasPermission(RECONCILE_PERMISSION)) {
            audience.sendMessage("You do not have permission to reconcile transactions.");
            safeEvent("ADMIN_TRANSACTION_RECONCILE_PERMISSION_DENIED", audience, transactionId);
            return true;
        }
        WayfarerTransactions.ReconcileAction action;
        try {
            action = WayfarerTransactions.ReconcileAction.valueOf(
                arguments[4].toUpperCase(Locale.ROOT)
            );
            if (action == WayfarerTransactions.ReconcileAction.AUTO) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException failure) {
            audience.sendMessage("Reconcile action must be commit, refund, or fail.");
            return true;
        }
        safeEvent("ADMIN_TRANSACTION_RECONCILED", audience, transactionId);
        complete(
            audience,
            transactions.get().reconcile(transactionId, action),
            result -> audience.sendMessage(
                "Transaction " + result.transactionId()
                    + " reconciliation result=" + result.state()
            )
        );
        return true;
    }

    private <T> void complete(
        CommandAudience audience,
        CompletionStage<T> stage,
        Consumer<T> success
    ) {
        Objects.requireNonNull(stage, "transaction completion").whenComplete((value, failure) ->
            mainThread.accept(() -> {
                if (failure == null) {
                    success.accept(value);
                } else {
                    audience.sendMessage("Transaction operation failed; inspect health and audit.");
                }
            })
        );
    }

    private void safeEvent(
        String eventType,
        CommandAudience audience,
        UUID transactionId
    ) {
        try {
            events.record(new OperationalEvent(
                eventType,
                audience.actorUuid().orElse(null),
                audience.audienceKind(),
                "TRANSACTION",
                transactionId.toString(),
                null
            )).whenComplete((ignored, failure) -> {
                if (failure != null) {
                    warn();
                }
            });
        } catch (RuntimeException failure) {
            warn();
        }
    }

    private void warn() {
        try {
            warningSink.accept("Wayfarer transaction command audit failed");
        } catch (RuntimeException ignored) {
            // Command response remains available.
        }
    }

    private static String presence(String value) {
        return value == null ? "none" : "present";
    }
}
