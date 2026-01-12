package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class TemperEnchant extends GameEnchantment implements AttackEnchant {

    private Modifier damageAmount;
    private Modifier damageStep;

    public TemperEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.damageAmount = Modifier.load(config, "Temper.Damage_Amount",
                Modifier.addictive(0).perLevel(5).capacity(100),
                "Extra damage (in %)"
        );

        // 改成按“损失生命百分比”计算的步长（X% missing health）
        this.damageStep = Modifier.load(config, "Settings.Damage.Step",
                // 默认每损失 5% 生命算一档，你可以在 YAML 里改这个值
                Modifier.addictive(5),
                "Damage will be increased for every X% of missing health. Where X is this value.",
                "By default increases damage by 5% for every 5% health missing."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT,
                level -> NumberUtil.format(this.getDamageAmount(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS,
                level -> NumberUtil.format(this.getDamageStep(level)));
    }

    public double getDamageAmount(int level) {
        return this.damageAmount.getValue(level);
    }

    // 现在表示“每档所需的损失生命百分比”
    public double getDamageStep(int level) {
        return this.damageStep.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event,
                            @NotNull LivingEntity damager,
                            @NotNull LivingEntity victim,
                            @NotNull ItemStack weapon,
                            int level) {

        double health = damager.getHealth();
        double maxHealth = EntityUtil.getAttribute(damager, Attribute.MAX_HEALTH);

        if (maxHealth <= 0) return false;
        if (health >= maxHealth) return false;

        // 损失生命百分比（0~100）
        double missingPercent = (maxHealth - health) / maxHealth * 100.0;

        double step = this.getDamageStep(level); // 每档需要损失的百分比
        if (step <= 0 || missingPercent < step) return false;

        double steps = Math.floor(missingPercent / step);
        if (steps <= 0) return false;

        // 每档增加 getDamageAmount(level)% 伤害
        double percent = 1D + (this.getDamageAmount(level) * steps / 100D);

        event.setDamage(event.getDamage() * percent);
        return true;
    }
}
