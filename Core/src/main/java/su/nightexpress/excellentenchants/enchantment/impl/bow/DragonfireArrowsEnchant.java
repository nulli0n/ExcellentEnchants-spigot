package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowData;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowSettings;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ArrowSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Version;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class DragonfireArrowsEnchant extends AbstractEnchantmentData implements ChanceData, ArrowData, BowEnchant {

    public static final String ID = "dragonfire_arrows";

    private Modifier fireDuration;
    private Modifier fireRadius;

    private ArrowSettingsImpl  arrowSettings;
    private ChanceSettingsImpl chanceSettings;

    public DragonfireArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to launch an dragonfire arrow (R=" + GENERIC_RADIUS + ", " + GENERIC_DURATION + "s).");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
        this.setConflicts(EnderBowEnchant.ID, GhastEnchant.ID, BomberEnchant.ID);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.arrowSettings = ArrowSettingsImpl.create(config, UniParticle.of(Particle.DRAGON_BREATH));

        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(3, 3, 1, 100));

        this.fireDuration = Modifier.read(config, "Settings.Fire.Duration",
            Modifier.multiply(100, 1, 1, 60 * 20),
            "Sets the dragonfire cloud effect duration (in ticks). 20 ticks = 1 second.");

        this.fireRadius = Modifier.read(config, "Settings.Fire.Radius",
            Modifier.add(1, 1, 1, 5),
            "Sets the dragonfire cloud effect radius.");

        this.addPlaceholder(GENERIC_DURATION, level -> NumberUtil.format(this.getFireDuration(level) / 20D));
        this.addPlaceholder(GENERIC_RADIUS, level -> NumberUtil.format(this.getFireRadius(level)));
    }

    @NotNull
    @Override
    public ArrowSettings getArrowSettings() {
        return arrowSettings;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    public int getFireDuration(int level) {
        return (int) this.fireDuration.getValue(level);
    }

    public double getFireRadius(int level) {
        return this.fireRadius.getValue(level);
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (event.getHitEntity() != null) return false;
        if (projectile.getShooter() == null) return false;

        this.createCloud(projectile.getShooter(), projectile.getLocation(), event.getHitEntity(), event.getHitBlock(), event.getHitBlockFace(), level);
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        this.createCloud(shooter, victim.getLocation(), victim, null, null, level);
        return false;
    }

    private void createCloud(@NotNull ProjectileSource shooter, @NotNull Location location,
                             @Nullable Entity hitEntity, @Nullable Block hitBlock, @Nullable BlockFace hitFace,
                             int level) {
        World world = location.getWorld();
        if (world == null) return;

        // There are some tweaks to respect protection plugins by using event call.

        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        ItemUtil.editMeta(item, meta -> {
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 20, 0), true);
            }
        });

        ThrownPotion potion = shooter.launchProjectile(ThrownPotion.class);
        potion.setItem(item);
        potion.teleport(location);

        AreaEffectCloud cloud = world.spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius((float) this.getFireRadius(level));
        cloud.setDuration(this.getFireDuration(level));
        cloud.setRadiusPerTick((7.0F - cloud.getRadius()) / (float) cloud.getDuration());
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.HARM, 1, 1), true);

        LingeringPotionSplashEvent splashEvent;

        if (Version.isAtLeast(Version.V1_20_R3)) {
            splashEvent = new LingeringPotionSplashEvent(potion, hitEntity, hitBlock, hitFace, cloud);
        }
        else {
            splashEvent = new LingeringPotionSplashEvent(potion, cloud);
        }

        plugin.getPluginManager().callEvent(splashEvent);
        if (splashEvent.isCancelled()) {
            cloud.remove();
        }
        potion.remove();
    }
}
