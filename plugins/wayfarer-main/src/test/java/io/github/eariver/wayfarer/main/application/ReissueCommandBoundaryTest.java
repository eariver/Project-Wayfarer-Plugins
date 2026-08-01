package io.github.eariver.wayfarer.main.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.eariver.wayfarer.common.SingleUseGate;
import io.github.eariver.wayfarer.main.domain.GrowthTool;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

final class ReissueCommandBoundaryTest {
    private static final UUID PLAYER = UUID.fromString(
        "00000000-0000-0000-0000-000000000201"
    );
    private static final UUID TOOL = UUID.fromString(
        "00000000-0000-0000-0000-000000000202"
    );
    private static final UUID INSTANCE = UUID.fromString(
        "00000000-0000-0000-0000-000000000203"
    );
    private static final UUID REISSUE = UUID.fromString(
        "00000000-0000-0000-0000-000000000204"
    );
    private static final UUID TRANSACTION = UUID.fromString(
        "00000000-0000-0000-0000-000000000205"
    );

    @Test
    void usePermissionAllowsPlayerWithoutAdminAndRejectsOtherActors() {
        assertTrue(ReissueCommandPolicy.mayUsePlayerReissue(true, true));
        assertFalse(ReissueCommandPolicy.mayUsePlayerReissue(true, false));
        assertFalse(ReissueCommandPolicy.mayUsePlayerReissue(false, true));
        assertTrue(ReissueCommandPolicy.mayUseAdminRecovery(true));
        assertFalse(ReissueCommandPolicy.mayUseAdminRecovery(false));
    }

    @Test
    void exactPlayerRoutesDispatchQuoteAndConfirmOnce() {
        AtomicInteger quoteCalls = new AtomicInteger();
        AtomicInteger confirmCalls = new AtomicInteger();

        ReissueCommandDispatcher.dispatchPlayer(
            new String[] {"tool", "reissue"},
            new ReissueCommandDispatcher.PlayerActionSink() {
                @Override
                public void quote() {
                    quoteCalls.incrementAndGet();
                }

                @Override
                public void confirm() {
                    confirmCalls.incrementAndGet();
                }
            }
        );
        ReissueCommandDispatcher.dispatchPlayer(
            new String[] {"tool", "reissue", "confirm"},
            new ReissueCommandDispatcher.PlayerActionSink() {
                @Override
                public void quote() {
                    quoteCalls.incrementAndGet();
                }

                @Override
                public void confirm() {
                    confirmCalls.incrementAndGet();
                }
            }
        );

        assertEquals(1, quoteCalls.get());
        assertEquals(1, confirmCalls.get());
    }

    @Test
    void invalidPlayerRoutesDoNotDispatchCoordinatorActions() {
        AtomicInteger coordinatorCalls = new AtomicInteger();
        String[][] invalidRoutes = {
            {"tool", "reissue", "confirm", "extra"},
            {"tool", "reissue", "cancel"},
            {"reissue"},
            {"tool", "repair"}
        };

        for (String[] route : invalidRoutes) {
            ReissueCommandDispatcher.dispatchPlayer(
                route,
                new ReissueCommandDispatcher.PlayerActionSink() {
                    @Override
                    public void quote() {
                        coordinatorCalls.incrementAndGet();
                    }

                    @Override
                    public void confirm() {
                        coordinatorCalls.incrementAndGet();
                    }
                }
            );
        }

        assertEquals(0, coordinatorCalls.get());
    }

    @Test
    void quoteMessageContainsCostEvolutionRepairAndConfirmCommand() {
        String message = ReissueCommandMessages.quote(quoteResult());

        assertTrue(message.contains("425 WM"));
        assertTrue(message.contains("Evolution Count: 3"));
        assertTrue(message.contains("fully repaired"));
        assertTrue(message.contains("/wayfarer-main tool reissue confirm"));
    }

    @Test
    void pendingMessageExplainsFreeDeliveryRetry() {
        String message = ReissueCommandMessages.confirm(
            new ReissueCoordinator.Result(
                ReissueCoordinator.Status.PENDING,
                REISSUE,
                TRANSACTION,
                null
            )
        );

        assertTrue(message.contains("free delivery"));
        assertTrue(message.contains("Free inventory space"));
        assertTrue(message.contains("rejoin"));
    }

    @Test
    void playerMessagesDoNotExposeIdsSqlOrExceptionDetails() {
        String quote = ReissueCommandMessages.quote(quoteResult());
        String pending = ReissueCommandMessages.confirm(
            new ReissueCoordinator.Result(
                ReissueCoordinator.Status.PENDING,
                REISSUE,
                TRANSACTION,
                "SQL_EXCEPTION"
            )
        );

        for (String message : new String[] {quote, pending}) {
            assertFalse(message.contains(REISSUE.toString()));
            assertFalse(message.contains(TRANSACTION.toString()));
            assertFalse(message.contains("SELECT"));
            assertFalse(message.contains("SQL"));
            assertFalse(message.contains("Exception"));
        }
    }

    @Test
    void adminRecoveryParserMapsEveryCoordinatorRoute() {
        AtomicInteger confirmPaymentCalls = new AtomicInteger();
        AtomicInteger resumePaymentCalls = new AtomicInteger();
        AtomicInteger resumeRotationCalls = new AtomicInteger();
        AtomicInteger markFailedCalls = new AtomicInteger();
        String[] actions = {
            "confirm-payment",
            "resume-payment",
            "resume-rotation"
        };
        for (int index = 0; index < actions.length; index++) {
            var result = ReissueCommandDispatcher.dispatchRecovery(
                new String[] {
                    "reconcile",
                    REISSUE.toString(),
                    actions[index],
                    "confirm"
                },
                new ReissueCommandDispatcher.RecoveryActionSink() {
                    @Override
                    public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> confirmPayment(
                        UUID reissueId
                    ) {
                        assertEquals(REISSUE, reissueId);
                        confirmPaymentCalls.incrementAndGet();
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> resumePayment(
                        UUID reissueId
                    ) {
                        assertEquals(REISSUE, reissueId);
                        resumePaymentCalls.incrementAndGet();
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> resumeRotation(
                        UUID reissueId
                    ) {
                        assertEquals(REISSUE, reissueId);
                        resumeRotationCalls.incrementAndGet();
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> markFailed(
                        UUID reissueId,
                        String failureCode
                    ) {
                        assertEquals(REISSUE, reissueId);
                        assertEquals("CORE_PAYMENT_FAILED", failureCode);
                        markFailedCalls.incrementAndGet();
                        return CompletableFuture.completedFuture(null);
                    }
                }
            ).orElseThrow();
            result.toCompletableFuture().join();
        }

        ReissueCommandDispatcher.dispatchRecovery(
            new String[] {
                "reconcile",
                REISSUE.toString(),
                "mark-failed",
                "CORE_PAYMENT_FAILED",
                "confirm"
            },
            new ReissueCommandDispatcher.RecoveryActionSink() {
                @Override
                public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> confirmPayment(
                    UUID reissueId
                ) {
                    throw new AssertionError("wrong route");
                }

                @Override
                public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> resumePayment(
                    UUID reissueId
                ) {
                    throw new AssertionError("wrong route");
                }

                @Override
                public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> resumeRotation(
                    UUID reissueId
                ) {
                    throw new AssertionError("wrong route");
                }

                @Override
                public java.util.concurrent.CompletionStage<ReissueCoordinator.Result> markFailed(
                    UUID reissueId,
                    String failureCode
                ) {
                    assertEquals(REISSUE, reissueId);
                    assertEquals("CORE_PAYMENT_FAILED", failureCode);
                    markFailedCalls.incrementAndGet();
                    return CompletableFuture.completedFuture(null);
                }
            }
        ).orElseThrow().toCompletableFuture().join();

        assertEquals(1, confirmPaymentCalls.get());
        assertEquals(1, resumePaymentCalls.get());
        assertEquals(1, resumeRotationCalls.get());
        assertEquals(1, markFailedCalls.get());
    }

    @Test
    void wrongAdminRouteOrArgumentCountDoesNotDispatchCoordinator() {
        AtomicInteger coordinatorCalls = new AtomicInteger();
        String[][] invalidRoutes = {
            {"reconcile", REISSUE.toString(), "resume-payment"},
            {"reconcile", REISSUE.toString(), "resume-payment", "no"},
            {"reconcile", REISSUE.toString(), "confirm-payment", "confirm", "extra"},
            {"reconcile", REISSUE.toString(), "unknown", "confirm"},
            {"reconcile", REISSUE.toString(), "mark-failed", "confirm"}
        };

        for (String[] route : invalidRoutes) {
            if (ReissueCommandParser.recoveryRoute(route).isPresent()) {
                coordinatorCalls.incrementAndGet();
            }
        }

        assertEquals(0, coordinatorCalls.get());
    }

    @Test
    void failureCodeIsSanitizedAndInspectRouteIsExact() {
        assertEquals(
            "ADMIN_FAILED",
            ReissueCommandParser.sanitizeFailureCode("DROP TABLE reissue")
        );
        assertEquals(
            REISSUE,
            ReissueCommandParser.inspectReissue(new String[] {
                "inspect", "reissue", REISSUE.toString()
            }).orElseThrow()
        );
        assertTrue(ReissueCommandParser.inspectReissue(new String[] {
            "inspect", "reissue"
        }).isEmpty());
    }

    @Test
    void sessionRefreshIsLimitedToDeliveredAndPending() {
        assertTrue(ReissueSessionPolicy.refreshAfterPaidResult(
            ReissueCoordinator.Status.DELIVERED
        ));
        assertTrue(ReissueSessionPolicy.refreshAfterPaidResult(
            ReissueCoordinator.Status.PENDING
        ));
        assertFalse(ReissueSessionPolicy.refreshAfterPaidResult(
            ReissueCoordinator.Status.UNKNOWN
        ));
        assertFalse(ReissueSessionPolicy.refreshAfterPaidResult(
            ReissueCoordinator.Status.REJECTED
        ));
    }

    private static ReissueCoordinator.QuoteResult quoteResult() {
        return new ReissueCoordinator.QuoteResult(
            ReissueCoordinator.QuoteStatus.ISSUED,
            new ReissueQuote(
                UUID.fromString("00000000-0000-0000-0000-000000000206"),
                PLAYER,
                TOOL,
                3,
                "main-1-test",
                INSTANCE,
                4,
                GrowthTool.DeliveryStatus.DELIVERED,
                425,
                Instant.parse("2026-08-01T00:01:00Z"),
                new SingleUseGate()
            ),
            null
        );
    }
}
