package su.nightexpress.excellentenchants.enchantment.impl.bow;

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
import su.nexmedia.engine.api.config.JOption;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;

public class EnchantGhast extends ExcellentEnchant implements BowEnchant {

    public static final String ID = "ghast";

    private boolean fireSpread;
    private EnchantScaler yield;

    public EnchantGhast(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.HIGHEST);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.fireSpread = JOption.create("Settings.Fire_Spread", true,
            "When 'true' creates fire on nearby blocks.").read(cfg);
        this.yield = EnchantScaler.read(this, "Settings.Yield", "1.0 + " + Placeholders.ENCHANTMENT_LEVEL,
            "Fireball explosion size/radius. The more value = the bigger the explosion.");
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
    public boolean onShoot(@NotNull EntityShootBowEvent e, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;
        if (!(e.getProjectile() instanceof Projectile projectile)) return false;

        Fireball fireball;

        // Shoot small fireballs for the Multishot enchantment,
        // as large ones has a slow speed and punches each other on shoot.
        if (EnchantManager.hasEnchantment(bow, Enchantment.MULTISHOT)) {
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
    public boolean onHit(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent e, @NotNull Projectile projectile,
                            @NotNull LivingEntity shooter, @NotNull LivingEntity victim,
                            @NotNull ItemStack weapon, int level) {
        return false;
    }
}
