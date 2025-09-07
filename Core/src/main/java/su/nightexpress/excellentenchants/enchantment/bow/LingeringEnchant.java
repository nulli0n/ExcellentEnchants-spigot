package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
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
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class LingeringEnchant extends GameEnchantment implements ArrowEnchant {

    public LingeringEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 5));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return true;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Arrow arrow, int level) {
        if (event.getHitEntity() != null) return;

        this.createCloud(arrow, shooter, arrow.getLocation(), event.getHitEntity(), event.getHitBlock(), event.getHitBlockFace());
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {

    }

    private void createCloud(@NotNull Arrow arrow,
                                @NotNull ProjectileSource shooter,
                                @NotNull Location location,
                                @Nullable Entity hitEntity,
                                @Nullable Block hitBlock,
                                @Nullable BlockFace hitFace) {

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
        potion.teleportAsync(location);

        this.plugin.runTask(location, () -> {
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
        });
    }
}
