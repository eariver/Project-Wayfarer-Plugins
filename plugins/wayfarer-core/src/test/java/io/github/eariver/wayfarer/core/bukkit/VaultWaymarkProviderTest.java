package io.github.eariver.wayfarer.core.bukkit;

import io.github.eariver.wayfarer.api.WayfarerWaymarkProvider;
import io.github.eariver.wayfarer.core.task.MainThreadDispatcher;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectKind.DEBIT;
import static io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectStatus.INSUFFICIENT_FUNDS;
import static io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectStatus.KNOWN_FAILURE;
import static io.github.eariver.wayfarer.api.WayfarerWaymarkProvider.EffectStatus.SUCCEEDED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VaultWaymarkProviderTest {
    private static final UUID PLAYER_UUID =
        UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final String SECRET = "redis://:provider-secret@cache.invalid";

    @Test
    void probeReturnsSafeSelectedProviderIdentity() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().getName()).thenReturn("RedisEconomy");

        WayfarerWaymarkProvider.ProbeResult result =
            fixture.provider().probe().toCompletableFuture().join();

        assertTrue(result.available());
        assertEquals("Vault/RedisEconomy", result.providerId());
        assertNull(result.failureCode());
    }

    @Test
    void unexpectedProviderDoesNotExposeRawIdentity() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().getName()).thenReturn(SECRET);

        WayfarerWaymarkProvider.ProbeResult result =
            fixture.provider().probe().toCompletableFuture().join();

        assertFalse(result.available());
        assertNull(result.providerId());
        assertEquals("UNEXPECTED_PROVIDER", result.failureCode());
        assertFalse(result.toString().contains(SECRET));
    }

    @Test
    void balanceUsesUuidResolvedOnlyInsideMainThreadDispatch() {
        AtomicBoolean mainThread = new AtomicBoolean();
        AtomicReference<UUID> resolvedUuid = new AtomicReference<>();
        Economy economy = mock(Economy.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        MainThreadDispatcher dispatcher = operation -> {
            mainThread.set(true);
            try {
                operation.run();
            } finally {
                mainThread.set(false);
            }
        };
        VaultWaymarkProvider provider = new VaultWaymarkProvider(
            economy,
            dispatcher,
            uuid -> {
                assertTrue(mainThread.get());
                resolvedUuid.set(uuid);
                return player;
            },
            "RedisEconomy",
            ignored -> {}
        );
        when(economy.isEnabled()).thenReturn(true);
        when(economy.getBalance(player)).thenReturn(125D);

        assertDecimal("125.0", provider.balance(PLAYER_UUID).toCompletableFuture().join());
        assertEquals(PLAYER_UUID, resolvedUuid.get());
    }

    @Test
    void mapsDebitAndRefundSuccessWithoutSynthesizingReference() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().withdrawPlayer(fixture.player(), 25D))
            .thenReturn(response(EconomyResponse.ResponseType.SUCCESS, null));
        when(fixture.economy().depositPlayer(fixture.player(), 25D))
            .thenReturn(response(EconomyResponse.ResponseType.SUCCESS, null));

        WayfarerWaymarkProvider.EffectResult debit = fixture.provider()
            .debit(PLAYER_UUID, 25, "debit-operation")
            .toCompletableFuture()
            .join();
        WayfarerWaymarkProvider.EffectResult refund = fixture.provider()
            .refund(PLAYER_UUID, 25, "refund-operation", null)
            .toCompletableFuture()
            .join();

        assertEquals(SUCCEEDED, debit.status());
        assertEquals(SUCCEEDED, refund.status());
        assertNull(debit.providerReference());
        assertNull(refund.providerReference());
    }

    @Test
    void mapsFixedInsufficientFundsResponse() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().withdrawPlayer(fixture.player(), 50D))
            .thenReturn(response(
                EconomyResponse.ResponseType.FAILURE,
                "Insufficient funds"
            ));

        WayfarerWaymarkProvider.EffectResult result = fixture.provider()
            .debit(PLAYER_UUID, 50, "debit-operation")
            .toCompletableFuture()
            .join();

        assertEquals(INSUFFICIENT_FUNDS, result.status());
        assertEquals("INSUFFICIENT_FUNDS", result.failureCode());
        assertNull(result.providerReference());
    }

    @Test
    void knownFailureAndRawProviderErrorAreRedacted() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().withdrawPlayer(fixture.player(), 10D))
            .thenReturn(response(EconomyResponse.ResponseType.FAILURE, SECRET));

        WayfarerWaymarkProvider.EffectResult result = fixture.provider()
            .debit(PLAYER_UUID, 10, "debit-operation")
            .toCompletableFuture()
            .join();

        assertEquals(KNOWN_FAILURE, result.status());
        assertEquals("VAULT_FAILURE", result.failureCode());
        assertFalse(result.toString().contains(SECRET));
    }

    @Test
    void nullProviderResponseIsUnknown() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().withdrawPlayer(fixture.player(), 10D)).thenReturn(null);

        WayfarerWaymarkProvider.EffectResult result = fixture.provider()
            .debit(PLAYER_UUID, 10, "debit-operation")
            .toCompletableFuture()
            .join();

        assertEquals(WayfarerWaymarkProvider.EffectStatus.UNKNOWN, result.status());
        assertEquals("VAULT_RESULT_UNKNOWN", result.failureCode());
    }

    @Test
    void providerExceptionIsSanitizedAndCompletesExceptionally() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().withdrawPlayer(fixture.player(), 10D))
            .thenThrow(new IllegalStateException(SECRET));

        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> fixture.provider()
                .debit(PLAYER_UUID, 10, "debit-operation")
                .toCompletableFuture()
                .join()
        );

        assertEquals("Vault economy operation failed", failure.getCause().getMessage());
        assertFalse(failure.toString().contains(SECRET));
        assertEquals(List.of("Vault economy provider operation failed"), fixture.warnings());
    }

    @Test
    void timeoutBeforeMainThreadExecutionPreventsProviderCall() {
        Queue<Runnable> queued = new ArrayDeque<>();
        Fixture fixture = fixture(queued::add);
        when(fixture.economy().isEnabled()).thenReturn(true);

        CompletionException failure = assertThrows(
            CompletionException.class,
            () -> fixture.provider()
                .debit(PLAYER_UUID, 10, "debit-operation")
                .toCompletableFuture()
                .orTimeout(1, TimeUnit.MILLISECONDS)
                .join()
        );
        assertTrue(failure.getCause() instanceof java.util.concurrent.TimeoutException);

        queued.remove().run();

        verify(fixture.economy(), never())
            .withdrawPlayer(fixture.player(), 10D);
    }

    @Test
    void resolveRemainsUnknownAndDoesNotSynthesizeReference() {
        Fixture fixture = fixture(Runnable::run);

        WayfarerWaymarkProvider.ResolutionResult result = fixture.provider()
            .resolve(DEBIT, "debit-operation", null)
            .toCompletableFuture()
            .join();

        assertEquals(WayfarerWaymarkProvider.ResolutionStatus.UNKNOWN, result.status());
        assertNull(result.providerReference());
        assertEquals("EFFECT_LOOKUP_UNAVAILABLE", result.failureCode());
    }

    @Test
    void disabledProviderFailsClosedAndCloseRejectsFurtherWork() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(false);

        WayfarerWaymarkProvider.EffectResult disabled = fixture.provider()
            .debit(PLAYER_UUID, 10, "debit-operation")
            .toCompletableFuture()
            .join();
        assertEquals(KNOWN_FAILURE, disabled.status());
        assertEquals("PROVIDER_DISABLED", disabled.failureCode());

        fixture.provider().close();
        WayfarerWaymarkProvider.EffectResult closed = fixture.provider()
            .refund(PLAYER_UUID, 10, "refund-operation", null)
            .toCompletableFuture()
            .join();
        assertEquals(KNOWN_FAILURE, closed.status());
        assertThrows(
            CompletionException.class,
            () -> fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
        );
        verify(fixture.economy(), never()).depositPlayer(fixture.player(), 10D);
    }

    @Test
    void fractionalBalancePreservesVaultAuthority() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().getBalance(fixture.player())).thenReturn(12.5D);

        assertDecimal(
            "12.5",
            fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
        );
    }

    @Test
    void fractionalBalanceRemainsAfterIntegerDebitAndRefund() {
        Fixture fixture = fixture(Runnable::run);
        when(fixture.economy().isEnabled()).thenReturn(true);
        when(fixture.economy().getBalance(fixture.player()))
            .thenReturn(37.5D, 12.5D, 37.5D);
        when(fixture.economy().withdrawPlayer(fixture.player(), 25D))
            .thenReturn(response(EconomyResponse.ResponseType.SUCCESS, null));
        when(fixture.economy().depositPlayer(fixture.player(), 25D))
            .thenReturn(response(EconomyResponse.ResponseType.SUCCESS, null));

        assertDecimal(
            "37.5",
            fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
        );
        assertEquals(
            SUCCEEDED,
            fixture.provider().debit(PLAYER_UUID, 25L, "debit-operation")
                .toCompletableFuture()
                .join()
                .status()
        );
        assertDecimal(
            "12.5",
            fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
        );
        assertEquals(
            SUCCEEDED,
            fixture.provider().refund(
                PLAYER_UUID,
                25L,
                "refund-operation",
                null
            ).toCompletableFuture().join().status()
        );
        assertDecimal(
            "37.5",
            fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
        );
    }

    @Test
    void nonFiniteBalancesFailClosedWithoutLeakingRawValues() {
        for (double invalid : List.of(
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
        )) {
            Fixture fixture = fixture(Runnable::run);
            when(fixture.economy().isEnabled()).thenReturn(true);
            when(fixture.economy().getBalance(fixture.player())).thenReturn(invalid);

            CompletionException failure = assertThrows(
                CompletionException.class,
                () -> fixture.provider().balance(PLAYER_UUID).toCompletableFuture().join()
            );
            assertEquals("Vault economy operation failed", failure.getCause().getMessage());
            assertFalse(failure.toString().contains(Double.toString(invalid)));
        }
    }

    private static Fixture fixture(MainThreadDispatcher dispatcher) {
        Economy economy = mock(Economy.class);
        OfflinePlayer player = mock(OfflinePlayer.class);
        List<String> warnings = new ArrayList<>();
        VaultWaymarkProvider provider = new VaultWaymarkProvider(
            economy,
            dispatcher,
            ignored -> player,
            "RedisEconomy",
            warnings::add
        );
        return new Fixture(provider, economy, player, warnings);
    }

    private static EconomyResponse response(
        EconomyResponse.ResponseType type,
        String error
    ) {
        return new EconomyResponse(0D, 0D, type, error);
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private record Fixture(
        VaultWaymarkProvider provider,
        Economy economy,
        OfflinePlayer player,
        List<String> warnings
    ) {}
}
