package io.github.eariver.wayfarer.preclient.fixture;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Test-only Vault plugin that makes the Vault API visible through Paper's plugin
 * classloader and publishes a representative Economy service.
 */
public final class PreclientWaymarkFixturePlugin extends JavaPlugin
    implements InvocationHandler {
    private static final Path MODE_FILE = Path.of("fixture-mode.txt");
    private static final String PROVIDER_NAME = "RedisEconomy";
    private double balance = 100_000D;

    @Override
    public void onEnable() {
        if ("outage".equals(mode())) {
            getLogger().info("WAYFARER_FIXTURE_VAULT: API_ONLY provider=unavailable");
            return;
        }
        Economy economy = (Economy) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[]{Economy.class},
            this
        );
        getServer().getServicesManager().register(
            Economy.class,
            economy,
            this,
            ServicePriority.Normal
        );
        getLogger().info(
            "WAYFARER_FIXTURE_VAULT: REGISTERED provider=" + PROVIDER_NAME
        );
    }

    @Override
    public synchronized Object invoke(
        Object proxy,
        Method method,
        Object[] arguments
    ) {
        if (method.getDeclaringClass() == Object.class) {
            return objectMethod(proxy, method, arguments);
        }
        String name = method.getName();
        getLogger().info(
            "WAYFARER_FIXTURE_VAULT: CALL " + name
                + " thread=" + Thread.currentThread().getName()
        );
        return switch (name) {
            case "getName" -> PROVIDER_NAME;
            case "isEnabled" -> true;
            case "hasBankSupport" -> false;
            case "fractionalDigits" -> 2;
            case "format" -> String.format(
                Locale.ROOT,
                "%.2f",
                ((Number) arguments[0]).doubleValue()
            );
            case "currencyNamePlural" -> "Waymarks";
            case "currencyNameSingular" -> "Waymark";
            case "hasAccount", "createPlayerAccount" -> true;
            case "getBalance" -> balance;
            case "has" -> balance >= lastNumber(arguments);
            case "withdrawPlayer" -> withdraw(lastNumber(arguments));
            case "depositPlayer" -> deposit(lastNumber(arguments));
            case "getBanks" -> List.of();
            case "createBank", "deleteBank", "bankBalance", "bankHas",
                 "bankWithdraw", "bankDeposit", "isBankOwner", "isBankMember" ->
                failure(0D, "Bank operations are unavailable");
            default -> throw new UnsupportedOperationException(
                "Unsupported Vault fixture method: " + name
            );
        };
    }

    private EconomyResponse withdraw(double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) {
            return failure(amount, "Invalid amount");
        }
        if (balance < amount) {
            return failure(amount, "Insufficient funds");
        }
        balance -= amount;
        return success(amount);
    }

    private EconomyResponse deposit(double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) {
            return failure(amount, "Invalid amount");
        }
        balance += amount;
        return success(amount);
    }

    private EconomyResponse success(double amount) {
        return new EconomyResponse(
            amount,
            balance,
            EconomyResponse.ResponseType.SUCCESS,
            null
        );
    }

    private EconomyResponse failure(double amount, String message) {
        return new EconomyResponse(
            amount,
            balance,
            EconomyResponse.ResponseType.FAILURE,
            message
        );
    }

    private String mode() {
        try {
            return Files.isRegularFile(MODE_FILE)
                ? Files.readString(MODE_FILE).trim()
                : "success";
        } catch (IOException failure) {
            throw new IllegalStateException("Vault fixture mode is unavailable");
        }
    }

    private static double lastNumber(Object[] arguments) {
        if (arguments == null || arguments.length == 0) {
            throw new IllegalArgumentException("Vault amount is absent");
        }
        Object value = arguments[arguments.length - 1];
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Vault amount is invalid");
        }
        return number.doubleValue();
    }

    private static Object objectMethod(
        Object proxy,
        Method method,
        Object[] arguments
    ) {
        return switch (method.getName()) {
            case "toString" -> "PreclientVaultEconomyFixture";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0];
            default -> throw new UnsupportedOperationException(
                "Unsupported Object method"
            );
        };
    }
}
