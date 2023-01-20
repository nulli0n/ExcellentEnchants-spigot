package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantVampire extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "vampire";
    public static final String PLACEHOLDER_HEAL_AMOUNT = "%enchantment_heal_amount%";

    private EnchantScaler  healAmount;
    private boolean healMultiplier;
    private ChanceImplementation chanceImplementation;

    public EnchantVampire(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);

        this.healAmount = EnchantScaler.read(this, "Settings.Heal.Amount", "0.25 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Amount of health to be restored for attacker.");
        this.healMultiplier = JOption.create("Settings.Heal.As_Multiplier", false,
            "When 'true', the option above will work as a multiplier of the inflicted damage.").read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public boolean isHealMultiplier() {
        return healMultiplier;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        double healAmount = this.getHealAmount(level);

        return str -> super.replacePlaceholders(level).apply(str)
            .replace(PLACEHOLDER_HEAL_AMOUNT, NumberUtil.format(this.isHealMultiplier() ? healAmount * 100D : healAmount))
        ;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isAvailableToUse(damager)) return false;

        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = damager.getHealth();
        if (healthHas == healthMax) return false;

        if (!this.checkTriggerChance(level)) return false;

        double healAmount = this.getHealAmount(level);
        double healFinal = this.isHealMultiplier() ? e.getDamage() * healAmount : healAmount;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(damager, healFinal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return false;

        damager.setHealth(Math.min(healthMax, healthHas + healthEvent.getAmount()));

        if (this.hasVisualEffects()) {
            EffectUtil.playEffect(damager.getEyeLocation(), Particle.HEART, "", 0.2f, 0.15f, 0.2f, 0.15f, 5);
        }
        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
