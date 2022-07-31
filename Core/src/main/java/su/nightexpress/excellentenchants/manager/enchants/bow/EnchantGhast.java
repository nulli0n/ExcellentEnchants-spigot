package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

public class EnchantGhast extends IEnchantChanceTemplate implements BowEnchant {

    private boolean fireSpread;
    private Scaler yield;

    public static final String ID = "ghast";

    public EnchantGhast(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGHEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.fireSpread = cfg.getBoolean("Settings.Fire_Spread");
        this.yield = new EnchantScaler(this, "Settings.Yield");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.addMissing("Settings.Fire_Spread", true);
        cfg.addMissing("Settings.Yield", "1.0 * " + PLACEHOLDER_LEVEL);
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public float getYield(int level) {
        return (float) this.yield.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean use(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isEnchantmentAvailable(shooter)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(e.getProjectile() instanceof Projectile projectile)) return false;
        if (!bow.containsEnchantment(ARROW_INFINITE) && !this.takeCostItem(shooter)) return false;

        Fireball fireball;

        // Shoot small fireballs for the Multishot enchantment,
        // as large ones has a slow speed and punches each other on shoot.
        if (bow.containsEnchantment(Enchantment.MULTISHOT)) {
            fireball = shooter.launchProjectile(SmallFireball.class);
            fireball.setVelocity(projectile.getVelocity().normalize().multiply(0.5f));
        }
        else {
            fireball = shooter.launchProjectile(Fireball.class);
            fireball.setDirection(projectile.getVelocity());
        }
        fireball.setIsIncendiary(this.isFireSpread());
        fireball.setYield(this.getYield(level));

        e.setProjectile(fireball);
        return true;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        // Support for the 'Power' enchantment.
        int power = weapon.getEnchantmentLevel(Enchantment.ARROW_DAMAGE);
        if (power < 1) return false;

        double damagePower = 0.5 + power * 0.5;
        e.setDamage(e.getDamage() + damagePower);
        return true;
    }
}
