package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class RegrowthEnchant extends GameEnchantment implements ChanceMeta, PassiveEnchant {

    public static final String ID = "regrowth";

    private Modifier minHealth;
    private Modifier maxHealth;
    private Modifier healAmount;

    public RegrowthEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.TAIGA_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Restores " + GENERIC_AMOUNT + "❤ every few seconds.",
            EnchantRarity.MYTHIC,
            5,
            ItemCategories.TORSO
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(100, 0, 1, 100)));
        this.meta.setPeriod(Period.create(config));

        this.minHealth = Modifier.read(config, "Settings.Heal.Min_Health",
            Modifier.add(0.5, 0, 0),
            "Minimal entity health for the enchantment to have effect."
        );

        this.maxHealth = Modifier.read(config, "Settings.Heal.Max_Health",
            Modifier.add(20, 0, 0),
            "Maximal entity health when the enchantment will not heal anymore."
        );

        this.healAmount = Modifier.read(config, "Settings.Heal.Amount",
            Modifier.add(0, 0.1, 1, 10),
            "Amount of hearts to be restored."
        );

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
        this.addPlaceholder(GENERIC_MIN, level -> NumberUtil.format(this.getMinHealthToHeal(level)));
        this.addPlaceholder(GENERIC_MAX, level -> NumberUtil.format(this.getMaxHealthToHeal(level)));
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
        if (!this.checkTriggerChance(level)) return false;

        double healthMax = EntityUtil.getAttribute(entity, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = entity.getHealth();
        if (healthHas < this.getMinHealthToHeal(level) || healthHas > this.getMaxHealthToHeal(level)) return false;
        if (healthHas >= healthMax) return false;

        double amount = Math.min(healthMax, healthHas + this.getHealAmount(level));
        entity.setHealth(amount);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(entity.getEyeLocation(), 0.25, 0.1, 5);
        }
        return true;
    }
}
