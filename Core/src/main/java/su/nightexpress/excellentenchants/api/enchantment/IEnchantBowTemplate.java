package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.tasks.ArrowTrailsTask;

public abstract class IEnchantBowTemplate extends IEnchantChanceTemplate implements BowEnchant {

    protected final String arrowTrailName;
    protected final String arrowTrailData;
    protected final String arrowMeta;

    public IEnchantBowTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg, @NotNull EnchantPriority priority) {
        super(plugin, cfg, priority);
        this.arrowMeta = this.getId() + "_arrow";
        this.arrowTrailName = cfg.getString("Settings.Arrow.Trail.Name", "");
        this.arrowTrailData = cfg.getString("Settings.Arrow.Trail.Data", "");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("Settings.Arrow.Trail") && !cfg.isConfigurationSection("Settings.Arrow.Trail")) {
            String trail = cfg.getString("Settings.Arrow.Trail", "");

            cfg.set("Settings.Arrow.Trail", null);
            cfg.addMissing("Settings.Arrow.Trail.Name", trail);
        }
        cfg.addMissing("Settings.Arrow.Trail.Data", "");
    }

    public boolean isThisArrow(@NotNull Projectile projectile) {
        return projectile.hasMetadata(this.arrowMeta);
    }

    public void setThisArrow(@NotNull Projectile projectile) {
        projectile.setMetadata(this.arrowMeta, new FixedMetadataValue(plugin, true));
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!this.isThisArrow(projectile)) return false;

        return true;
    }

    @Override
    public boolean use(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isEnchantmentAvailable(shooter)) return false;
        if (!(e.getProjectile() instanceof Projectile arrow)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!bow.containsEnchantment(ARROW_INFINITE) && !this.takeCostItem(shooter)) return false;

        this.setThisArrow(arrow);
        if (!this.arrowTrailName.isEmpty()) {
            ArrowTrailsTask.add(arrow, this.arrowTrailName, this.arrowTrailData);
        }
        return true;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(victim)) return false;
        if (!(e.getDamager() instanceof Projectile projectile)) return false;
        if (!this.isThisArrow(projectile)) return false;

        return true;
    }
}
