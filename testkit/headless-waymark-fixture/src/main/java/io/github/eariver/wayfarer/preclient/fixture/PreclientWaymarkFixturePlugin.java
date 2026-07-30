package io.github.eariver.wayfarer.preclient.fixture;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.Plugin;
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
        register(Economy.class);
        Plugin core = getServer().getPluginManager().getPlugin("Wayfarer_Core");
        if (core != null) {
            try {
                Class<?> coreEconomy = Class.forName(
                    Economy.class.getName(),
                    false,
                    core.getClass().getClassLoader()
                );
                if (coreEconomy != Economy.class) {
                    register(coreEconomy);
                }
                getLogger().info(
                    "WAYFARER_FIXTURE_VAULT: API_IDENTITY shared="
                        + (coreEconomy == Economy.class)
                );
            } catch (ClassNotFoundException failure) {
                throw new IllegalStateException(
                    "Core Vault API identity is unavailable"
                );
            }
        }
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
            case "withdrawPlayer" ->
                withdraw(method.getReturnType(), lastNumber(arguments));
            case "depositPlayer" ->
                deposit(method.getReturnType(), lastNumber(arguments));
            case "getBanks" -> List.of();
            case "createBank", "deleteBank", "bankBalance", "bankHas",
                 "bankWithdraw", "bankDeposit", "isBankOwner", "isBankMember" ->
                response(
                    method.getReturnType(),
                    0D,
                    false,
                    "Bank operations are unavailable"
                );
            default -> throw new UnsupportedOperationException(
                "Unsupported Vault fixture method: " + name
            );
        };
    }

    private Object withdraw(Class<?> responseType, double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) {
            return response(responseType, amount, false, "Invalid amount");
        }
        if (balance < amount) {
            return response(responseType, amount, false, "Insufficient funds");
        }
        balance -= amount;
        return response(responseType, amount, true, null);
    }

    private Object deposit(Class<?> responseType, double amount) {
        if (!Double.isFinite(amount) || amount <= 0D) {
            return response(responseType, amount, false, "Invalid amount");
        }
        balance += amount;
        return response(responseType, amount, true, null);
    }

    private Object response(
        Class<?> responseType,
        double amount,
        boolean success,
        String message
    ) {
        try {
            Class<?> responseKind = Class.forName(
                responseType.getName() + "$ResponseType",
                true,
                responseType.getClassLoader()
            );
            @SuppressWarnings({"rawtypes", "unchecked"})
            Object kind = Enum.valueOf(
                (Class<? extends Enum>) responseKind.asSubclass(Enum.class),
                success ? "SUCCESS" : "FAILURE"
            );
            return responseType.getConstructor(
                double.class,
                double.class,
                responseKind,
                String.class
            ).newInstance(amount, balance, kind, message);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(
                "Vault fixture response construction failed"
            );
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void register(Class<?> economyType) {
        Object economy = Proxy.newProxyInstance(
            economyType.getClassLoader(),
            new Class<?>[]{economyType},
            this
        );
        getServer().getServicesManager().register(
            (Class) economyType,
            economy,
            this,
            ServicePriority.Normal
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
