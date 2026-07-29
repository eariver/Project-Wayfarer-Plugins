package io.github.eariver.wayfarer.frontier;

import io.github.eariver.wayfarer.api.WayfarerServices;
import io.github.eariver.wayfarer.api.WayfarerLifecycleState;
import io.github.eariver.wayfarer.frontier.config.FrontierModuleConfig;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class WayfarerFrontierPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        try {
            FrontierModuleConfig.load(getConfig());
        } catch (RuntimeException failure) {
            failClosed("Wayfarer_Frontier configuration is invalid.");
            return;
        }

        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);

        if (registration == null) {
            failClosed("Wayfarer_Core services are unavailable.");
            return;
        }

        try {
            WayfarerServices services = registration.getProvider();
            if (services.lifecycleState() != WayfarerLifecycleState.ENABLED) {
                failClosed("Wayfarer_Core is not enabled.");
                return;
            }
            services.tasks();
            services.audit();
            services.transactions();
            services.waymark();
            services.itemIdentity();
        } catch (RuntimeException failure) {
            failClosed("Required Wayfarer_Core capabilities are unavailable.");
            return;
        }

        // ADR 0009 must be reviewed before a concrete module pool and repository are integrated.
        failClosed("Wayfarer_Frontier persistence integration awaits Plugin review.");
    }

    private void failClosed(String message) {
        getLogger().severe(message + " Disabling fail-closed.");
        getServer().getPluginManager().disablePlugin(this);
    }
}
