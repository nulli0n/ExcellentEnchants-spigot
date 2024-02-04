package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;

public class EnchantBlindness extends ExcellentEnchant implements Chanced, Potioned, CombatEnchant {

    public static final String ID = "blindness";

    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantBlindness(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to apply " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " (" + Placeholders.ENCHANTMENT_POTION_DURATION + "s.) on hit.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "15.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 3");
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.BLINDNESS, false,
            "3.5 + " + Placeholders.ENCHANTMENT_LEVEL,
            Placeholders.ENCHANTMENT_LEVEL);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.checkTriggerChance(level)) return false;
        if (!this.addEffect(victim, level)) return false;

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.SMOKE_NORMAL).play(victim.getEyeLocation(), 0.25, 0.1, 30);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
