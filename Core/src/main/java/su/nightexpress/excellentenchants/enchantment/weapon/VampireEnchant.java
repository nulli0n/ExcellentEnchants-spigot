package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class VampireEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier healAmount;
    private boolean  healMultiplier;

    public VampireEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(8, 4));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.healAmount = Modifier.load(config, "Vampire.Amount",
            Modifier.addictive(0.25).perLevel(0.25).capacity(10),
            "Amount of health to be restored for attacker.");

        this.healMultiplier = ConfigValue.create("Vampire.Multiplier",
            false,
            "When 'true', the option above will work as a multiplier of the inflicted damage."
        ).read(config);

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.isHealMultiplier() ? getHealAmount(level) * 100D : getHealAmount(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public boolean isHealMultiplier() {
        return healMultiplier;
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        double healthMax = EntityUtil.getAttribute(damager, Attribute.MAX_HEALTH);
        double healthHas = damager.getHealth();
        if (healthHas == healthMax) return false;

        double healAmount = this.getHealAmount(level);
        double healFinal = this.isHealMultiplier() ? event.getFinalDamage() * healAmount : healAmount;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(damager, healFinal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return false;

        damager.setHealth(Math.min(healthMax, healthHas + healthEvent.getAmount()));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(damager.getEyeLocation(), 0.25, 0.15, 5);
        }
        return true;
    }
}
