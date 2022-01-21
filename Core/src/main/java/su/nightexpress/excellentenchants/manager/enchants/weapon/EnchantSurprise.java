package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantCombatPotionTemplate;

public class EnchantSurprise extends IEnchantCombatPotionTemplate {

    public static final String ID = "surprise";

    public EnchantSurprise(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM, PotionEffectType.BLINDNESS);

    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        this.potionEffectType = Rnd.get(PotionEffectType.values());
        return super.use(e, damager, victim, weapon, level);
    }
}
