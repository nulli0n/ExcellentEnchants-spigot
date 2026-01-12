package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
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
    /** 这里表示“比例”，0.01 = 1% 最大生命值 */
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

        // 现在 Heal_Amount 表示“最大生命值百分比”，0.01 = 1%
        this.healAmount = Modifier.load(config, "Regrowth.Heal_Amount",
                // 默认：2% + 每级 2%，最高 10%
                Modifier.addictive(0.02).perLevel(0.02).capacity(0.10D),
                "Heal percent of max health. 0.01 = 1%."
        );

        // %amount%：显示百分比（比如 2、4、6）
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT,
                level -> NumberUtil.format(this.getHealPercent(level) * 100D)
        );
        // %min%、%max%：还是按“血量值”显示
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MIN,
                level -> NumberUtil.format(this.getMinHealthToHeal(level))
        );
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MAX,
                level -> NumberUtil.format(this.getMaxHealthToHeal(level))
        );
    }

    /** 返回比例：0.01 = 1% */
    public double getHealPercent(int level) {
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

        // 按最大生命值百分比计算回复量
        double percent = this.getHealPercent(level);      // 比如 0.02 = 2%
        double heal    = maxHealth * percent;             // 实际回复血量
        double amount  = Math.min(maxHealth, health + heal);

        entity.setHealth(amount);

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(entity.getEyeLocation(), 0.25, 0.1, 5);
        }
        return true;
    }
}
