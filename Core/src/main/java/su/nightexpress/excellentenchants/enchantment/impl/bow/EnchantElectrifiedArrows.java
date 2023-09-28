package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ArrowImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class EnchantElectrifiedArrows extends ExcellentEnchant implements Chanced, Arrowed, BowEnchant {

    public static final String ID = "electrified_arrows";

    private static final String META_NO_ITEM_DAMAGE = "itemNoDamage";

    private ArrowImplementation arrowImplementation;
    private ChanceImplementation chanceImplementation;

    public EnchantElectrifiedArrows(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to launch an electrified arrow.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
        this.getDefaults().setConflicts(EnchantEnderBow.ID, EnchantGhast.ID, EnchantHover.ID, EnchantBomber.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.arrowImplementation = ArrowImplementation.create(this, UniParticle.of(Particle.FIREWORKS_SPARK));
        this.chanceImplementation = ChanceImplementation.create(this,
            "10.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5");
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

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;

        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (!this.isOurProjectile(projectile)) return false;
        if (event.getHitEntity() != null || event.getHitBlock() == null) return false;

        Block block = event.getHitBlock();
        block.getWorld().strikeLightning(block.getLocation()).setMetadata(META_NO_ITEM_DAMAGE, new FixedMetadataValue(plugin, true));
        if (this.hasVisualEffects()) {
            Location center = LocationUtil.getCenter(block.getLocation());
            UniParticle.blockCrack(block.getType()).play(center, 1, 0.05, 120);
            UniParticle.of(Particle.FIREWORKS_SPARK).play(center, 1, 0.05, 120);
        }
        return true;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isOurProjectile(projectile)) return false;

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (victim.isDead()) return;
            victim.setNoDamageTicks(0);
            victim.getWorld().strikeLightning(victim.getLocation()).setMetadata(META_NO_ITEM_DAMAGE, new FixedMetadataValue(plugin, true));
        });

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(EntityDamageByEntityEvent e) {
        if (!e.getDamager().hasMetadata(META_NO_ITEM_DAMAGE)) return;
        if (e.getEntity() instanceof Item || e.getEntity() instanceof ItemFrame) {
            e.setCancelled(true);
            e.getEntity().setFireTicks(0);
        }
    }
}
