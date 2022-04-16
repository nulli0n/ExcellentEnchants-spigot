package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantBowPotionTemplate;

public class EnchantConfusingArrows extends IEnchantBowPotionTemplate {

    public static final String ID = "confusing_arrows";

    public EnchantConfusingArrows(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.CONFUSION);
    }
}
