package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ArrowImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class EnchantExplosiveArrows extends ExcellentEnchant implements Chanced, Arrowed, BowEnchant {

    public static final String ID                          = "explosive_arrows";
    public static final String PLACEHOLDER_EXPLOSION_POWER = "%enchantment_explosion_power%";

    private boolean       explosionFireSpread;
    private boolean       explosionDamageItems;
    private boolean       explosionDamageBlocks;
    private EnchantScaler explosionSize;

    private ArrowImplementation arrowImplementation;
    private ChanceImplementation chanceImplementation;

    private Entity lastExploder;

    public EnchantExplosiveArrows(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to launch an explosive arrow.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.7);
        this.getDefaults().setConflicts(EnchantEnderBow.ID, EnchantGhast.ID, EnchantHover.ID, EnchantBomber.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.arrowImplementation = ArrowImplementation.create(this, UniParticle.of(Particle.SMOKE_NORMAL));
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5");
        this.explosionFireSpread = JOption.create("Settings.Explosion.Fire_Spread", true,
            "When 'true' creates fire on nearby blocks.").read(cfg);
        this.explosionDamageItems = JOption.create("Settings.Explosion.Damage_Items", false,
            "When 'true' inflicts damage to items on the ground.").read(cfg);
        this.explosionDamageBlocks = JOption.create("Settings.Explosion.Damage_Blocks", false,
            "When 'true' allows to break blocks by explosion.").read(cfg);
        this.explosionSize = EnchantScaler.read(this, "Settings.Explosion.Size",
            "2.0 + " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets the explosion size. The more size - the bigger explosion.");

        this.addPlaceholder(PLACEHOLDER_EXPLOSION_POWER, level -> NumberUtil.format(this.getExplosionSize(level)));
    }

    @NotNull
    @Override
    public ArrowImplementation getArrowImplementation() {
        return arrowImplementation;
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
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
        if (!this.isOurProjectile(projectile)) return false;

        if (projectile.getShooter() instanceof Entity entity) {
            this.lastExploder = entity;
        }

        World world = projectile.getWorld();
        float explSize = (float) this.getExplosionSize(level);
        boolean explFire = this.isExplosionFireSpread();
        boolean explBlocks = this.isExplosionDamageBlocks();
        boolean exploded = world.createExplosion(projectile.getLocation(), explSize, explFire, explBlocks, this.lastExploder);
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
