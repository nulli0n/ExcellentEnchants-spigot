package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;

public class EnchantDoubleStrike extends IEnchantChanceTemplate implements CombatEnchant {

    private String particleName;
    private String particleData;
    private Sound sound;

    public static final String ID = "double_strike";

    public EnchantDoubleStrike(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.LOW);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.EXPLOSION_NORMAL.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.sound = cfg.getEnum("Settings.Sound", Sound.class);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.EXPLOSION_NORMAL.name());
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
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(damager)) return false;

        e.setDamage(e.getDamage() * 2D);
        EffectUtil.playEffect(victim.getEyeLocation(), this.particleName, this.particleData, 0.2f, 0.15f, 0.2f, 0.15f, 20);
        if (this.sound != null) MessageUtil.sound(victim.getLocation(), this.sound);
        return true;
    }
}
