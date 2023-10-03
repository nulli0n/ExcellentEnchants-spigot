package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantVampire extends ExcellentEnchant implements Chanced, CombatEnchant {

    public static final String ID = "vampire";
    public static final String PLACEHOLDER_HEAL_AMOUNT = "%enchantment_heal_amount%";

    private EnchantScaler  healAmount;
    private boolean healMultiplier;
    private ChanceImplementation chanceImplementation;

    public EnchantVampire(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to heal for " + PLACEHOLDER_HEAL_AMOUNT + " heart(s) on hit.");
        this.getDefaults().setLevelMax(4);
        this.getDefaults().setTier(0.75);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this,
            "25.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5.0");

        this.healAmount = EnchantScaler.read(this, "Settings.Heal.Amount",
            "0.25 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Amount of health to be restored for attacker.");
        this.healMultiplier = JOption.create("Settings.Heal.As_Multiplier", false,
            "When 'true', the option above will work as a multiplier of the inflicted damage.").read(cfg);

        this.addPlaceholder(PLACEHOLDER_HEAL_AMOUNT, level -> NumberUtil.format(this.isHealMultiplier() ? getHealAmount(level) * 100D : getHealAmount(level)));
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

    @NotNull
    @Override
    public EventPriority getAttackPriority() {
        return EventPriority.MONITOR;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = damager.getHealth();
        if (healthHas == healthMax) return false;

        if (!this.checkTriggerChance(level)) return false;

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

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
