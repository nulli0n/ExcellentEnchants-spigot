package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;

public class EnchantEnderBow extends IEnchantChanceTemplate implements BowEnchant {

    public static final String ID = "ender_bow";

    public EnchantEnderBow(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGHEST);
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.BOMBER);
        this.addConflict(EnchantRegister.GHAST);
        this.addConflict(EnchantRegister.EXPLOSIVE_ARROWS);
        this.addConflict(EnchantRegister.WITHERED_ARROWS);
        this.addConflict(EnchantRegister.POISONED_ARROWS);
        this.addConflict(EnchantRegister.DRAGONFIRE_ARROWS);
        this.addConflict(EnchantRegister.ELECTRIFIED_ARROWS);
        this.addConflict(EnchantRegister.CONFUSING_ARROWS);
        this.addConflict(Enchantment.ARROW_FIRE);
        this.addConflict(Enchantment.ARROW_DAMAGE);
        this.addConflict(Enchantment.ARROW_KNOCKBACK);
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

        EnderPearl pearl = shooter.launchProjectile(EnderPearl.class);
        pearl.setVelocity(projectile.getVelocity());
        e.setProjectile(pearl);
        return true;
    }

    @Override
    public boolean use(@NotNull ProjectileHitEvent e, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
