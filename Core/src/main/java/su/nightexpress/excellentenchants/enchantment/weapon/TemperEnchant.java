package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
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

        this.damageStep = Modifier.load(config, "Settings.Damage.Step",
            Modifier.addictive(0.5),
            "Damage will be increased for every X entity's health points missing. Where X is this value.",
            "By default increases damage by 5% for every 0.5 HP missing."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getDamageAmount(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.getDamageStep(level)));
    }

    public double getDamageAmount(int level) {
        return this.damageAmount.getValue(level);
    }

    public double getDamageStep(int level) {
        return this.damageStep.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        double health = damager.getHealth();
        double maxHealth = EntityUtil.getAttribute(damager, Attribute.MAX_HEALTH);
        if (health >= maxHealth) return false;

        double missingHealth = maxHealth - health;
        double step = this.getDamageStep(level);
        if (step == 0 || missingHealth < step) return false;

        double steps = Math.floor(missingHealth / step);
        if (steps == 0) return false;

        double percent = 1D + (this.getDamageAmount(level) * steps / 100D);

        event.setDamage(event.getDamage() * percent);
        return true;
    }
}
