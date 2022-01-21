package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantPotionTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;

public class EnchantHaste extends IEnchantPotionTemplate implements PassiveEnchant {

    public static final String ID = "haste";

    public EnchantHaste(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.FAST_DIGGING);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull LivingEntity entity, int level) {
        if (!this.isEnchantmentAvailable(entity)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(entity)) return false;

        return this.addEffect(entity, level);
    }
}
