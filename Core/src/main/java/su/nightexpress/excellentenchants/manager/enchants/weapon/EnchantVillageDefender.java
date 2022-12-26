package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Illager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantVillageDefender extends IEnchantChanceTemplate implements CombatEnchant {

    private boolean damageMultiplier;
    private Scaler  damageAmount;
    private String  particleName;
    private String  particleData;

    public static final String ID = "village_defender";
    public static final String PLACEHOLDER_DAMAGE_AMOUNT = "%enchantment_damage_amount%";

    public EnchantVillageDefender(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.damageAmount = new EnchantScaler(this, "Settings.Damage.Formula");
        this.damageMultiplier = cfg.getBoolean("Settings.Damage.As_Modifier");
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.VILLAGER_ANGRY.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
    }

    public double getDamageAddict(int level) {
        return this.damageAmount.getValue(level);
    }

    public boolean isDamageMultiplier() {
        return damageMultiplier;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_DAMAGE_AMOUNT, NumberUtil.format(this.getDamageAddict(level)))
        );
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.VILLAGER_ANGRY.name());
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!(victim instanceof Illager)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        double damageAdd = this.getDamageAddict(level);
        double damageHas = e.getDamage();
        double damageFinal = this.isDamageMultiplier() ? (damageHas * damageAdd) : (damageHas + damageAdd);

        e.setDamage(damageFinal);
        EffectUtil.playEffect(victim.getEyeLocation(), this.particleName, this.particleData, 0.15, 0.15, 0.15, 0.13f, 3);
        return true;
    }
}
