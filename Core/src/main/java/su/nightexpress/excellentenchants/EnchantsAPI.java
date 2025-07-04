package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.manager.EnchantManager;

public class EnchantsAPI {

    private static EnchantsPlugin plugin;

    static void load(@NotNull EnchantsPlugin plugin) {
        EnchantsAPI.plugin = plugin;
    }

    @NotNull
    public static EnchantsPlugin getPlugin() {
        if (plugin == null) throw new IllegalStateException("API is not initialized!");

        return plugin;
    }

    @NotNull
    public static EnchantManager getEnchantManager() {
        return getPlugin().getEnchantManager();
    }

//    @NotNull
//    public static EnchantNMS getInternals() {
//        return getPlugin().getEnchantNMS();
//    }
}
