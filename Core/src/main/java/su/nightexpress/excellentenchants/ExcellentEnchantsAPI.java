package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.tier.TierManager;

public class ExcellentEnchantsAPI {

    public static final ExcellentEnchants PLUGIN = ExcellentEnchants.getPlugin(ExcellentEnchants.class);

    @NotNull
    public static EnchantManager getEnchantManager() {
        return PLUGIN.getEnchantManager();
    }

    @NotNull
    public static TierManager getTierManager() {
        return PLUGIN.getTierManager();
    }
}
