package io.github.eariver.wayfarer.frontier;

import io.github.eariver.wayfarer.api.WayfarerServices;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class WayfarerFrontierPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        RegisteredServiceProvider<WayfarerServices> registration =
            getServer().getServicesManager().getRegistration(WayfarerServices.class);

        if (registration == null) {
            getLogger().severe("Wayfarer_Core services are unavailable; disabling fail-closed.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().warning("Scaffold only: implement the approved design specification before runtime integration.");
    }
}
