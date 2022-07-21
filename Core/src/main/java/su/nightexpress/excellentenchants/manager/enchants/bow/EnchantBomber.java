package su.nightexpress.excellentenchants.manager.enchants.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantBomber extends IEnchantChanceTemplate implements BowEnchant {

    private final Scaler fuseTicks;

    public static final String ID = "bomber";

    public static final String PLACEHOLDER_FUSE_TICKS = "%enchantment_fuse_ticks%";

    public EnchantBomber(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGHEST);

        this.fuseTicks = new EnchantScaler(this, "Settings.Fuse_Ticks");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
        this.cfg.addMissing("Settings.Fuse_Ticks", "100 - %enchantment_level% * 10");
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_FUSE_TICKS, NumberUtil.format((double) this.getFuseTicks(level) / 20D))
        );
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.ENDER_BOW);
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

        TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
        primed.setVelocity(projectile.getVelocity().multiply(e.getForce() * 1.25));
        primed.setFuseTicks(this.getFuseTicks(level));
        primed.setSource(shooter);
        e.setProjectile(primed);
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
