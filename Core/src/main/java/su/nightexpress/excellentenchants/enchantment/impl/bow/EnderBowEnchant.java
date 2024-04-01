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
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class EnderBowEnchant extends AbstractEnchantmentData implements ChanceData, BowEnchant {

    public static final String ID = "ender_bow";

    private ChanceSettingsImpl chanceSettings;

    public EnderBowEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Shoots ender pearls instead of arrows.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.VERY_RARE);

        this.setConflicts(
            BomberEnchant.ID, GhastEnchant.ID,
            ExplosiveArrowsEnchant.ID, PoisonedArrowsEnchant.ID, ConfusingArrowsEnchant.ID,
            WitheredArrowsEnchant.ID, ElectrifiedArrowsEnchant.ID, DragonfireArrowsEnchant.ID,
            DarknessArrowsEnchant.ID, VampiricArrowsEnchant.ID,
            HoverEnchant.ID, FlareEnchant.ID,
            Enchantment.ARROW_FIRE.getKey().getKey(),
            Enchantment.ARROW_KNOCKBACK.getKey().getKey(),
            Enchantment.ARROW_DAMAGE.getKey().getKey()
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config);
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
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
