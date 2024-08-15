package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;

public class LingeringEnchant extends GameEnchantment implements BowEnchant, ChanceMeta {

    public LingeringEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.TAIGA_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList(ENCHANTMENT_CHANCE + "% chance for tipped arrows to generate a lingering effect."),
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOWS,
            null,
            Lists.newSet(BomberEnchant.ID, EnderBowEnchant.ID, GhastEnchant.ID)
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(5, 5, 1, 100)));
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (event.getHitEntity() != null) return false;
        if (projectile.getShooter() == null) return false;
        if (!(projectile instanceof Arrow arrow)) return false;

        this.createCloud(arrow, arrow.getShooter(), arrow.getLocation(), event.getHitEntity(), event.getHitBlock(), event.getHitBlockFace(), level);
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }

    @Override
    @NotNull
    public EventPriority getHitPriority() {
        return EventPriority.HIGHEST;
    }

    private void createCloud(@NotNull Arrow arrow,
                                @NotNull ProjectileSource shooter,
                                @NotNull Location location,
                                @Nullable Entity hitEntity,
                                @Nullable Block hitBlock,
                                @Nullable BlockFace hitFace,
                                int level) {

        Set<PotionEffect> effects = new HashSet<>();
        if (arrow.hasCustomEffects()) {
            effects.addAll(arrow.getCustomEffects());
        }
        if (arrow.getBasePotionType() != null) {
            effects.addAll(arrow.getBasePotionType().getPotionEffects());
        }
        if (effects.isEmpty()) return;

        // There are some tweaks to respect protection plugins by using event call.
        ItemStack item = new ItemStack(Material.LINGERING_POTION);
        ItemUtil.editMeta(item, meta -> {
            if (meta instanceof PotionMeta potionMeta) {
                effects.forEach(potionEffect -> potionMeta.addCustomEffect(potionEffect, true));
            }
        });

        ThrownPotion potion = shooter.launchProjectile(ThrownPotion.class);
        potion.setItem(item);
        potion.teleport(location);

        AreaEffectCloud cloud = potion.getWorld().spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter);
        cloud.setWaitTime(10);
        cloud.setRadius(3F); // 3.0
        cloud.setRadiusOnUse(-0.5F);
        cloud.setDuration(600); // 600
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        cloud.setBasePotionType(arrow.getBasePotionType());
        effects.forEach(potionEffect -> cloud.addCustomEffect(potionEffect, false));

        LingeringPotionSplashEvent splashEvent = new LingeringPotionSplashEvent(potion, hitEntity, hitBlock, hitFace, cloud);
        plugin.getPluginManager().callEvent(splashEvent);
        if (splashEvent.isCancelled()) {
            cloud.remove();
        }
        potion.remove();
    }
}
