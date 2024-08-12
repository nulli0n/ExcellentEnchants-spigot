package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.rarity.RarityManager;

public class EnchantsAPI {

    private static EnchantsPlugin plugin;

    static void load(@NotNull EnchantsPlugin plugin) {
        EnchantsAPI.plugin = plugin;
    }

    @NotNull
    private static EnchantsPlugin plugin() {
        if (plugin == null) throw new IllegalStateException("API is not initialized!");

        return plugin;
    }

    @NotNull
    public static EnchantManager getEnchantManager() {
        return plugin().getEnchantManager();
    }

    @NotNull
    public static RarityManager getRarityManager() {
        return plugin().getRarityManager();
    }
}
