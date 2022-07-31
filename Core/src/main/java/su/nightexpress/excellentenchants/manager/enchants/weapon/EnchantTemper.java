package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantTemper extends IEnchantChanceTemplate implements CombatEnchant {

    public static final String ID                          = "temper";
    public static final String PLACEHOLDER_DAMAGE_AMOUNT   = "%enchantment_damage_amount%";
    public static final String PLACEHOLDER_DAMAGE_CAPACITY = "%enchantment_damage_capacity%";
    public static final String PLACEHOLDER_HEALTH_POINT    = "%enchantment_health_point%";

    private EnchantScaler damageAmount;
    private EnchantScaler damageCapacity;
    private EnchantScaler healthPoint;

    public EnchantTemper(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.damageAmount = new EnchantScaler(this, "Settings.Damage.Amount");
        this.damageCapacity = new EnchantScaler(this, "Settings.Damage.Capacity");
        this.healthPoint = new EnchantScaler(this, "Settings.Health.Point");
    }

    public double getDamageAmount(int level) {
        return this.damageAmount.getValue(level);
    }

    public double getDamageCapacity(int level) {
        return this.damageCapacity.getValue(level);
    }

    public double getHealthPoint(int level) {
        return this.healthPoint.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_DAMAGE_AMOUNT, NumberUtil.format(this.getDamageAmount(level) * 100D))
            .replace(PLACEHOLDER_DAMAGE_CAPACITY, NumberUtil.format(this.getDamageCapacity(level) * 100D))
            .replace(PLACEHOLDER_HEALTH_POINT, NumberUtil.format(this.getHealthPoint(level)))
        );
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double healthPoint = this.getHealthPoint(level);
        double healthHas = damager.getHealth();
        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthDiff = healthMax - healthHas;
        if (healthHas >= healthMax || healthDiff < healthPoint) return false;

        int pointAmount = (int) (healthDiff / healthPoint);
        if (pointAmount == 0) return false;

        if (!this.takeCostItem(damager)) return false;

        double damageAmount = this.getDamageAmount(level);
        double damageCap = this.getDamageCapacity(level);
        double damageFinal = Math.min(damageCap, 1D + damageAmount * pointAmount);

        e.setDamage(e.getDamage() * damageFinal);
        return true;
    }
}
