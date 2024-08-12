package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.Enchantment;
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
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;

public class GhastEnchant extends GameEnchantment implements ChanceMeta, BowEnchant {

    public static final String ID = "ghast";

    private boolean  fireSpread;
    private Modifier yield;

    public GhastEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Shoots fireballs instead of arrows.",
            EnchantRarity.RARE,
            1,
            ItemCategories.BOWS,
            Lists.newSet(
                EnderBowEnchant.ID, BomberEnchant.ID,
                ExplosiveArrowsEnchant.ID, PoisonedArrowsEnchant.ID, ConfusingArrowsEnchant.ID,
                WitheredArrowsEnchant.ID, ElectrifiedArrowsEnchant.ID, DragonfireArrowsEnchant.ID,
                DarknessArrowsEnchant.ID, VampiricArrowsEnchant.ID,
                HoverEnchant.ID, FlareEnchant.ID,
                BukkitThing.toString(Enchantment.FLAME),
                BukkitThing.toString(Enchantment.PUNCH),
                BukkitThing.toString(Enchantment.POWER)
            )
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config));

        this.fireSpread = ConfigValue.create("Settings.Fire_Spread",
            true,
            "When 'true' creates fire on nearby blocks.").read(config);

        this.yield = Modifier.read(config, "Settings.Yield",
            Modifier.add(2, 0, 1, 5),
            "Fireball explosion size/radius. The more value = the bigger the explosion.");
    }

    public boolean isFireSpread() {
        return fireSpread;
    }

    public float getYield(int level) {
        return (float) this.yield.getValue(level);
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
