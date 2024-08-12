package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_RADIUS;

public class ExplosiveArrowsEnchant extends GameEnchantment implements ChanceMeta, ArrowMeta, BowEnchant, SimpeListener {

    public static final String ID = "explosive_arrows";

    private boolean  explosionFireSpread;
    private boolean  explosionDamageItems;
    private boolean  explosionDamageBlocks;
    private Modifier explosionSize;

    private Entity lastExploder;

    public ExplosiveArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to launch an explosive arrow.",
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOWS,
            Lists.newSet(EnderBowEnchant.ID, GhastEnchant.ID, BomberEnchant.ID)
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setArrowEffects(ArrowEffects.create(config, UniParticle.of(Particle.SMOKE)));

        this.meta.setProbability(Probability.create(config, Modifier.add(3, 2, 1, 100)));

        this.explosionFireSpread = ConfigValue.create("Settings.Explosion.Fire_Spread",
            true,
            "When 'true' creates fire on nearby blocks.").read(config);

        this.explosionDamageItems = ConfigValue.create("Settings.Explosion.Damage_Items",
            false,
            "When 'true' inflicts damage to items on the ground.").read(config);

        this.explosionDamageBlocks = ConfigValue.create("Settings.Explosion.Damage_Blocks",
            false,
            "When 'true' allows to break blocks by explosion.").read(config);

        this.explosionSize = Modifier.read(config, "Settings.Explosion.Size",
            Modifier.add(1, 1, 1, 5),
            "Sets the explosion size. The more size - the bigger explosion.");

        this.addPlaceholder(GENERIC_RADIUS, level -> NumberUtil.format(this.getExplosionSize(level)));
    }

    public final double getExplosionSize(int level) {
        return this.explosionSize.getValue(level);
    }

    public final boolean isExplosionFireSpread() {
        return this.explosionFireSpread;
    }

    public final boolean isExplosionDamageBlocks() {
        return this.explosionDamageBlocks;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (projectile.getShooter() instanceof Entity entity) {
            this.lastExploder = entity;
        }

        World world = projectile.getWorld();
        float explSize = (float) this.getExplosionSize(level);
        boolean explFire = this.isExplosionFireSpread();
        boolean explBlocks = this.isExplosionDamageBlocks();
        /*boolean exploded = */world.createExplosion(projectile.getLocation(), explSize, explFire, explBlocks, this.lastExploder);
        this.lastExploder = null;
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;
        if (this.explosionDamageItems) return;
        if (this.lastExploder == null || this.lastExploder != event.getDamager()) return;

        if (event.getEntity() instanceof Item || event.getEntity() instanceof ItemFrame) {
            event.setCancelled(true);
        }
    }
}
