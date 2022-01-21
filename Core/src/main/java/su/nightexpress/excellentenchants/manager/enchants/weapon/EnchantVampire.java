package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantVampire extends IEnchantChanceTemplate implements CombatEnchant {

    private final String particleName;
    private final String particleData;
    private final Scaler healAmount;
    private final boolean healMultiplier;

    public static final String ID = "vampire";
    public static final String PLACEHOLDER_HEAL_AMOUNT = "%enchantment_heal_amount%";

    public EnchantVampire(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOWEST);

        this.particleName = cfg.getString("Settings.Particle.Name", Particle.HEART.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.healAmount = new EnchantScaler(this, "Settings.Heal.Amount");
        this.healMultiplier = cfg.getBoolean("Settings.Heal.As_Multiplier");
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    public boolean isHealMultiplier() {
        return healMultiplier;
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.remove("Settings.Heal_Of_Damage");
        cfg.addMissing("Settings.Particle.Name", Particle.HEART.name());
        cfg.addMissing("Settings.Particle.Data", "");
        cfg.addMissing("Settings.Heal.Amount", "0.25 * " + PLACEHOLDER_LEVEL);
        cfg.addMissing("Settings.Heal.As_Multiplier", false);
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

        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_HEAL_AMOUNT, NumberUtil.format(this.isHealMultiplier() ? healAmount * 100D : healAmount))
        );
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;

        double healthMax = EntityUtil.getAttribute(damager, Attribute.GENERIC_MAX_HEALTH);
        double healthHas = damager.getHealth();
        if (healthHas == healthMax) return false;

        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        double healAmount = this.getHealAmount(level);
        double healFinal = this.isHealMultiplier() ? e.getDamage() * healAmount : healAmount;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(damager, healFinal, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return false;

        damager.setHealth(Math.min(healthMax, healthHas + healthEvent.getAmount()));

        EffectUtil.playEffect(damager.getEyeLocation(), this.particleName, this.particleData, 0.2f, 0.15f, 0.2f, 0.15f, 5);
        return true;
    }
}
