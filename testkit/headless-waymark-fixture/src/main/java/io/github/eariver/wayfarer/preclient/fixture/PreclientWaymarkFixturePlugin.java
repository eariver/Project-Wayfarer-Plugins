package io.github.eariver.wayfarer.preclient.fixture;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * Test-only provider registered with the API class identity owned by the candidate Core plugin.
 */
public final class PreclientWaymarkFixturePlugin extends JavaPlugin
    implements InvocationHandler {
    private static final String PROVIDER_TYPE =
        "io.github.eariver.wayfarer.api.WayfarerWaymarkProvider";
    private static final Path MODE_FILE = Path.of("fixture-mode.txt");
    private static final Path EFFECT_FILE = Path.of("fixture-effects.properties");
    private static final Path CRASH_MARKER_FILE = Path.of("fixture-crash-marker.txt");
    private ClassLoader apiLoader;

    @Override
    public void onEnable() {
        Plugin core = Objects.requireNonNull(
            getServer().getPluginManager().getPlugin("Wayfarer_Core"),
            "Wayfarer_Core plugin identity"
        );
        apiLoader = core.getClass().getClassLoader();
        try {
            Class<?> serviceType = Class.forName(PROVIDER_TYPE, true, apiLoader);
            Object provider = Proxy.newProxyInstance(
                apiLoader,
                new Class<?>[]{serviceType},
                this
            );
            register(serviceType, provider);
            getLogger().info("WAYFARER_FIXTURE: REGISTERED candidate-api-identity");
        } catch (ClassNotFoundException failure) {
            throw new IllegalStateException("Candidate provider API is unavailable", failure);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            return objectMethod(proxy, method, arguments);
        }
        String mode = mode();
        String name = method.getName();
        System.out.println(
            "WAYFARER_FIXTURE: CALL " + name
                + " thread=" + Thread.currentThread().getName()
                + " mode=" + mode
        );
        System.out.flush();
        return switch (name) {
            case "probe" -> completed(record(
                "ProbeResult",
                !"outage".equals(mode),
                "preclient-fixture",
                "outage".equals(mode) ? "FIXTURE_OUTAGE" : null
            ));
            case "balance" -> completed(new BigDecimal("100000"));
            case "debit" -> effect("DEBIT", (String) arguments[2], mode);
            case "refund" -> effect("REFUND", (String) arguments[2], mode);
            case "resolve" -> resolve((String) arguments[1], mode);
            default -> throw new UnsupportedOperationException("Unsupported fixture method");
        };
    }

    private Object effect(String kind, String operationId, String mode) throws Exception {
        if ("outage".equals(mode)) {
            return CompletableFuture.failedFuture(
                new IllegalStateException("fixture provider outage")
            );
        }
        if ("timeout-before-effect".equals(mode)) {
            return new CompletableFuture<>();
        }
        String reference = kind.toLowerCase(java.util.Locale.ROOT) + "-" + operationId;
        persistEffect(operationId, kind, reference);
        if (("crash-after-debit".equals(mode) && "DEBIT".equals(kind))
            || ("crash-after-refund".equals(mode) && "REFUND".equals(kind))) {
            Files.writeString(
                CRASH_MARKER_FILE,
                "HALT_AFTER_" + kind + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
            System.out.println(
                "WAYFARER_FIXTURE: HALT_AFTER_" + kind + " operation=" + operationId
            );
            System.out.flush();
            Runtime.getRuntime().halt("DEBIT".equals(kind) ? 73 : 74);
        }
        if ("timeout-after-effect".equals(mode)) {
            return new CompletableFuture<>();
        }
        if ("unknown-after-effect".equals(mode)) {
            return completed(record(
                "EffectResult",
                enumeration("EffectStatus", "UNKNOWN"),
                reference,
                "FIXTURE_UNKNOWN_AFTER_EFFECT"
            ));
        }
        return completed(record(
            "EffectResult",
            enumeration("EffectStatus", "SUCCEEDED"),
            reference,
            null
        ));
    }

    private Object resolve(String operationId, String mode) throws Exception {
        if ("resolve-unknown".equals(mode)) {
            return completed(record(
                "ResolutionResult",
                enumeration("ResolutionStatus", "UNKNOWN"),
                null,
                "FIXTURE_RESOLUTION_UNKNOWN"
            ));
        }
        Properties effects = loadEffects();
        String value = effects.getProperty(operationId);
        if (value == null) {
            return completed(record(
                "ResolutionResult",
                enumeration("ResolutionStatus", "NOT_APPLIED"),
                null,
                "FIXTURE_NOT_APPLIED"
            ));
        }
        String[] fields = value.split("\\|", 2);
        return completed(record(
            "ResolutionResult",
            enumeration("ResolutionStatus", "APPLIED"),
            fields[1],
            null
        ));
    }

    private synchronized void persistEffect(
        String operationId,
        String kind,
        String reference
    ) throws IOException {
        Properties effects = loadEffects();
        effects.setProperty(operationId, kind + "|" + reference);
        try (OutputStream output = Files.newOutputStream(
            EFFECT_FILE,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
        )) {
            effects.store(output, "test-only Waymark fixture effects");
        }
    }

    private synchronized Properties loadEffects() throws IOException {
        Properties effects = new Properties();
        if (Files.isRegularFile(EFFECT_FILE)) {
            try (InputStream input = Files.newInputStream(EFFECT_FILE)) {
                effects.load(input);
            }
        }
        return effects;
    }

    private String mode() throws IOException {
        return Files.isRegularFile(MODE_FILE)
            ? Files.readString(MODE_FILE).trim()
            : "success";
    }

    private Object record(String simpleName, Object... arguments) throws Exception {
        Class<?> type = Class.forName(PROVIDER_TYPE + "$" + simpleName, true, apiLoader);
        for (Constructor<?> constructor : type.getDeclaredConstructors()) {
            if (constructor.getParameterCount() == arguments.length) {
                return constructor.newInstance(arguments);
            }
        }
        throw new IllegalStateException("Fixture API record constructor is unavailable");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object enumeration(String simpleName, String value) throws ClassNotFoundException {
        Class enumType = Class.forName(PROVIDER_TYPE + "$" + simpleName, true, apiLoader);
        return Enum.valueOf(enumType, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void register(Class serviceType, Object provider) {
        getServer().getServicesManager().register(
            serviceType,
            provider,
            this,
            ServicePriority.Normal
        );
    }

    private static CompletableFuture<Object> completed(Object value) {
        return CompletableFuture.completedFuture(value);
    }

    private static Object objectMethod(Object proxy, Method method, Object[] arguments) {
        return switch (method.getName()) {
            case "toString" -> "PreclientWaymarkFixture";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == arguments[0];
            default -> throw new UnsupportedOperationException("Unsupported Object method");
        };
    }
}
