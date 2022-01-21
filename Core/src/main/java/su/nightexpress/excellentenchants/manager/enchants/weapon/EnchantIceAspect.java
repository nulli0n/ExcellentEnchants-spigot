package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantCombatPotionTemplate;

public class EnchantIceAspect extends IEnchantCombatPotionTemplate {

    public static final String ID = "ice_aspect";

    public EnchantIceAspect(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.SLOW);
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!super.use(e, damager, victim, weapon, level)) return false;

        if (Version.CURRENT.isHigher(Version.V1_16_R3)) {
            victim.setFreezeTicks(victim.getMaxFreezeTicks());
        }
        return true;
    }
}
