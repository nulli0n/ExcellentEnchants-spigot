package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DefendEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightSound;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class FireShieldEnchant extends GameEnchantment implements DefendEnchant {

    private Modifier fireDuration;
    private boolean addFireImmune;

    public FireShieldEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(4, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fireDuration = Modifier.load(config, "FireShield.Duration",
            Modifier.addictive(4).perLevel(1).capacity(10),
            "Sets the fire duration (in seconds)."
        );

        this.addFireImmune = ConfigValue.create("FireShield.AddResistance",
            true,
            "Controls whether Fire Resistance effect should be added to the enchantment's wearer."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(this.getFireDuration(level)));
    }

    @NotNull
    @Override
    public EnchantPriority getProtectPriority() {
        return EnchantPriority.NORMAL;
    }

    public double getFireDuration(int level) {
        return this.fireDuration.getValue(level);
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        int fireTicks = (int) (this.getFireDuration(level) * 20);

        if (this.addFireImmune) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, fireTicks, 0));
        }

        int damagerFireTicks = damager.getFireTicks();
        if (damagerFireTicks >= fireTicks) return false;

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.FLAME).play(victim.getEyeLocation(), 0.5, 0.1, 35);
            NightSound.of(Sound.ITEM_FIRECHARGE_USE).play(victim.getLocation());
        }

        damager.setFireTicks(fireTicks);
        return true;
    }
}
