package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantEnderBow extends ExcellentEnchant implements BowEnchant, Chanced {

    public static final String ID = "ender_bow";

    private ChanceImplementation chanceImplementation;

    public EnchantEnderBow(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Shoots ender pearls instead of arrows.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(1.0);

        this.getDefaults().setConflicts(
            EnchantBomber.ID, EnchantGhast.ID,
            EnchantExplosiveArrows.ID, EnchantPoisonedArrows.ID, EnchantConfusingArrows.ID,
            EnchantWitheredArrows.ID, EnchantElectrifiedArrows.ID, EnchantDragonfireArrows.ID,
            DarknessArrowsEnchant.ID, VampiricArrowsEnchant.ID,
            EnchantHover.ID, FlareEnchant.ID,
            Enchantment.ARROW_FIRE.getKey().getKey(),
            Enchantment.ARROW_KNOCKBACK.getKey().getKey(),
            Enchantment.ARROW_DAMAGE.getKey().getKey()
        );
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.chanceImplementation = ChanceImplementation.create(this, "100");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    @NotNull
    @Override
    public EventPriority getShootPriority() {
        return EventPriority.LOWEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.checkTriggerChance(level)) return false;
        if (!(event.getProjectile() instanceof Projectile projectile)) return false;

        EnderPearl pearl = shooter.launchProjectile(EnderPearl.class);
        pearl.setVelocity(projectile.getVelocity());
        event.setProjectile(pearl);
        return true;
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
