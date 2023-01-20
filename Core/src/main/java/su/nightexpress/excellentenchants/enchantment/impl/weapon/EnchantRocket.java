package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;

public class EnchantRocket extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "rocket";

    private EnchantScaler fireworkPower;
    private ChanceImplementation chanceImplementation;

    public EnchantRocket(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.fireworkPower = EnchantScaler.read(this, "Settings.Firework_Power", Placeholders.ENCHANTMENT_LEVEL + " * 0.25",
            "Firework power. The more power = the higher fly distance.");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
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
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

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

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
