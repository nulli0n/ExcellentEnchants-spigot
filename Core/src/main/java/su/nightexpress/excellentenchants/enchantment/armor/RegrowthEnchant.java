package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class RegrowthEnchant extends GameEnchantment implements PassiveEnchant {

    private Modifier minHealth;
    private Modifier maxHealth;
    private Modifier healAmount;

    public RegrowthEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(15));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.minHealth = Modifier.load(config, "Regrowth.Min_Health",
            Modifier.addictive(0.5),
            "Min. health required for the regrowth effect."
        );

        this.maxHealth = Modifier.load(config, "Regrowth.Max_Health",
            Modifier.addictive(20),
            "Max. health where the regrowth stops."
        );

        this.healAmount = Modifier.load(config, "Regrowth.Heal_Amount",
            Modifier.addictive(0.1).perLevel(0.1).capacity(1D),
            "Amount of hearts to be restored."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MIN, level -> NumberUtil.format(this.getMinHealthToHeal(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MAX, level -> NumberUtil.format(this.getMaxHealthToHeal(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public double getMinHealthToHeal(int level) {
        return this.minHealth.getValue(level);
    }

    public double getMaxHealthToHeal(int level) {
        return this.maxHealth.getValue(level);
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        double maxHealth = EntityUtil.getAttribute(entity, Attribute.MAX_HEALTH);
        double health = entity.getHealth();
        if (health < this.getMinHealthToHeal(level) || health > this.getMaxHealthToHeal(level)) return false;
        if (health >= maxHealth) return false;

        double amount = Math.min(maxHealth, health + this.getHealAmount(level));
        entity.setHealth(amount);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(entity.getEyeLocation(), 0.25, 0.1, 5);
        }
        return true;
    }
}
