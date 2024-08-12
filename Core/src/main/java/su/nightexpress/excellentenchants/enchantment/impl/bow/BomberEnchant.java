package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
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
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_TIME;

public class BomberEnchant extends GameEnchantment implements ChanceMeta, BowEnchant {

    public static final String ID = "bomber";

    private Modifier fuseTicks;

    public BomberEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList(ENCHANTMENT_CHANCE + "% chance to launch TNT that explodes in " + GENERIC_TIME + "s."),
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOWS,
            null,
            Lists.newSet(
                EnderBowEnchant.ID, GhastEnchant.ID,
                ExplosiveArrowsEnchant.ID, PoisonedArrowsEnchant.ID, ConfusingArrowsEnchant.ID,
                WitheredArrowsEnchant.ID, ElectrifiedArrowsEnchant.ID, DragonfireArrowsEnchant.ID,
                DarknessArrowsEnchant.ID,
                HoverEnchant.ID, FlareEnchant.ID,
                BukkitThing.toString(Enchantment.FLAME),
                BukkitThing.toString(Enchantment.PUNCH),
                BukkitThing.toString(Enchantment.POWER)
            )
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(3.5, 1.5, 1, 100)));

        this.fuseTicks = Modifier.read(config, "Settings.Fuse_Ticks",
            Modifier.add(110, -10, 1),
            "Sets fuse ticks (before it will explode) for the launched TNT.");

        this.addPlaceholder(GENERIC_TIME, level -> NumberUtil.format((double) this.getFuseTicks(level) / 20D));
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
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

        int fuseTicks = Math.max(1, this.getFuseTicks(level));

        TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
        primed.setVelocity(projectile.getVelocity().multiply(event.getForce() * 1.25));
        primed.setFuseTicks(fuseTicks);
        primed.setSource(shooter);
        event.setProjectile(primed);
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
