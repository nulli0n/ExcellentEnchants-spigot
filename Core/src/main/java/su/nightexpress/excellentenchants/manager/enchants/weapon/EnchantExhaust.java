package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantCombatPotionTemplate;

public class EnchantExhaust extends IEnchantCombatPotionTemplate {

    public static final String ID = "exhaust";

    public EnchantExhaust(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.HUNGER);
    }
}
