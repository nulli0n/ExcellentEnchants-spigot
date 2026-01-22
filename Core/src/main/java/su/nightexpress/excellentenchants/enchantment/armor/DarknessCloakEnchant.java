package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.nio.file.Path;

public class DarknessCloakEnchant extends GameEnchantment implements DefendEnchant {

    public DarknessCloakEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(11, 3));
        this.addComponent(EnchantComponent.POTION_EFFECT, PotionEffects.temporal(PotionEffectType.DARKNESS, Modifier.addictive(3).perLevel(1).capacity(10)));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @NotNull
    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.addPotionEffect(damager, level)) return false;

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.ASH).play(damager.getEyeLocation(), 0.75, 0.1, 30);
        }

        return true;
    }
}
