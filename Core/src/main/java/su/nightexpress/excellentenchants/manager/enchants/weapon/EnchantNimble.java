package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;

public class EnchantNimble extends IEnchantChanceTemplate implements DeathEnchant {

    public static final String ID = "nimble";

    public EnchantNimble(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.isEnchantmentAvailable(dead)) return false;

        Player player = dead.getKiller();
        if (player == null) return false;

        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        e.getDrops().forEach(item -> PlayerUtil.addItem(player, item));
        e.getDrops().clear();
        return true;
    }
}
