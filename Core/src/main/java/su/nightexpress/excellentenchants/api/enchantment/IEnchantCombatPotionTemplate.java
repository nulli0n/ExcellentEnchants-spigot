package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public abstract class IEnchantCombatPotionTemplate extends IEnchantPotionTemplate implements CombatEnchant {

    protected String particleName;
    protected String particleData;

    public IEnchantCombatPotionTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg,
                                        @NotNull EnchantPriority priority,
                                        @NotNull PotionEffectType effectType) {
        super(plugin, cfg, priority, effectType);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.particleName = cfg.getString("Settings.Particle.Name", "");
        this.particleData = cfg.getString("Settings.Particle.Data", "");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", "");
        cfg.addMissing("Settings.Particle.Data", "");
    }

    @Override
    @NotNull
    public final EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(damager)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;
        if (!this.addEffect(victim, level)) return false;

        EffectUtil.playEffect(victim.getEyeLocation(), this.particleName, this.particleData, 0.25f, 0.25f, 0.25f, 0.1f, 50);
        return true;
    }
}
