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
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.template.PotionEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantSurprise extends PotionEnchant implements Chanced, CombatEnchant {

    public static final String ID = "surprise";

    private ChanceImplementation chanceImplementation;

    public EnchantSurprise(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM, PotionEffectType.BLINDNESS, false);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        PotionEffect effect = new PotionEffect(Rnd.get(PotionEffectType.values()), this.getEffectDuration(level), Math.max(0, this.getEffectAmplifier(level) - 1), false, false);
        if (!victim.addPotionEffect(effect)) return false;

        if (this.hasVisualEffects()) {
            Color color = Color.fromRGB(Rnd.nextInt(256), Rnd.nextInt(256), Rnd.nextInt(256));
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2f);
            SimpleParticle.of(Particle.REDSTONE, dustOptions).play(victim.getEyeLocation(), 0.25, 0.1, 25);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
