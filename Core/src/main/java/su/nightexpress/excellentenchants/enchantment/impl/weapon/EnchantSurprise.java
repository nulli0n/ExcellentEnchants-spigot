package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantSurprise extends ExcellentEnchant implements Chanced, Potioned, CombatEnchant {

    public static final String ID = "surprise";

    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantSurprise(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to apply random potion effect to enemy on hit.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.75);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "2.25 * " + Placeholders.ENCHANTMENT_LEVEL);
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.BLINDNESS, false,
            "3.0 + " + Placeholders.ENCHANTMENT_LEVEL,
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
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        PotionEffect effect = new PotionEffect(Rnd.get(PotionEffectType.values()), this.getEffectDuration(level), Math.max(0, this.getEffectAmplifier(level) - 1), false, false);
        if (!victim.addPotionEffect(effect)) return false;

        if (this.hasVisualEffects()) {
            Color color = Color.fromRGB(Rnd.nextInt(256), Rnd.nextInt(256), Rnd.nextInt(256));
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2f);
            UniParticle.of(Particle.REDSTONE, dustOptions).play(victim.getEyeLocation(), 0.25, 0.1, 25);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
