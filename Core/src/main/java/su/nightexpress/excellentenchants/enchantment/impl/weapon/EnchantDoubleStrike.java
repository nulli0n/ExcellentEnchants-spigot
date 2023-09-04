package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantDoubleStrike extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "double_strike";

    private ChanceImplementation chanceImplementation;

    public EnchantDoubleStrike(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOW);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to inflict double damage.");
        this.getDefaults().setLevelMax(4);
        this.getDefaults().setTier(1.0);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "4.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.8");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.setDamage(event.getDamage() * 2D);

        if (this.hasVisualEffects()) {
            SimpleParticle.of(Particle.EXPLOSION_NORMAL).play(victim.getEyeLocation(), 0.25, 0.15, 15);
            LocationUtil.sound(victim.getLocation(), Sound.ENTITY_GENERIC_EXPLODE);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
