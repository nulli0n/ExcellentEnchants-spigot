package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class GhastEnchant extends AbstractEnchantmentData implements ChanceData, BowEnchant {

    public static final String ID = "ghast";

    private boolean            fireSpread;
    private Modifier           yield;
    private ChanceSettingsImpl chanceSettings;

    public GhastEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Shoots fireballs instead of arrows.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.UNCOMMON);

        this.setConflicts(
            EnderBowEnchant.ID, BomberEnchant.ID,
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

        this.fireSpread = ConfigValue.create("Settings.Fire_Spread",
            true,
            "When 'true' creates fire on nearby blocks.").read(config);

        this.yield = Modifier.read(config, "Settings.Yield",
            Modifier.add(2, 0, 1, 5),
            "Fireball explosion size/radius. The more value = the bigger the explosion.");
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public float getYield(int level) {
        return (float) this.yield.getValue(level);
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

        Fireball fireball;

        // Shoot small fireballs for the Multishot enchantment,
        // as large ones has a slow speed and punches each other on shoot.
        if (EnchantUtils.contains(bow, Enchantment.MULTISHOT)) {
            fireball = shooter.launchProjectile(SmallFireball.class);
            fireball.setVelocity(projectile.getVelocity().normalize().multiply(0.5f));
        }
        else {
            fireball = shooter.launchProjectile(Fireball.class);
            fireball.setDirection(projectile.getVelocity());
        }
        fireball.setIsIncendiary(this.isFireSpread());
        fireball.setYield(this.getYield(level));

        event.setProjectile(fireball);
        return true;
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile,
                            @NotNull LivingEntity shooter, @NotNull LivingEntity victim,
                            @NotNull ItemStack weapon, int level) {
        return false;
    }
}
