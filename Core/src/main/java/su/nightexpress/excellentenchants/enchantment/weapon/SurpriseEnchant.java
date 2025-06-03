package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

@Deprecated
public class SurpriseEnchant extends GameEnchantment implements AttackEnchant {

    public SurpriseEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(3, 2));
        this.addComponent(EnchantComponent.POTION_EFFECT, PotionEffects.temporal(PotionEffectType.BLINDNESS, Modifier.addictive(1)));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        //PotionEffect effect = new PotionEffect(Rnd.get(BukkitThing.allFromRegistry(Registry.EFFECT)), this.getEffectDuration(level), Math.max(0, this.getEffectAmplifier(level) - 1), false, false);
        //if (!victim.addPotionEffect(effect)) return false;

        if (this.hasVisualEffects()) {
            Color color = Color.fromRGB(Rnd.nextInt(256), Rnd.nextInt(256), Rnd.nextInt(256));
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 2f);
            UniParticle.of(Particle.DUST, dustOptions).play(victim.getEyeLocation(), 0.25, 0.1, 25);
        }
        return true;
    }
}
