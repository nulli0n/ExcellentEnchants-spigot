package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

public class EnchantRocket extends IEnchantChanceTemplate implements CombatEnchant {

    private Scaler fireworkPower;

    public static final String ID = "rocket";

    public EnchantRocket(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.fireworkPower = new EnchantScaler(this, "Settings.Firework_Power");
    }

    public final double getFireworkPower(int level) {
        return this.fireworkPower.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        if (victim.isInsideVehicle()) {
            victim.leaveVehicle();
        }

        Firework firework = EntityUtil.spawnRandomFirework(victim.getLocation());
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower((int) this.getFireworkPower(level));
        firework.setFireworkMeta(meta);
        firework.addPassenger(victim);

        MessageUtil.sound(victim.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);
        return true;
    }
}
