package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class DragonfireArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier duration;
    private Modifier radius;

    public DragonfireArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.DRAGON_BREATH));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(4, 3));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.duration = Modifier.load(config, "Dragonfire.Duration",
            Modifier.addictive(40).perLevel(20).capacity(60 * 20),
            "Dragonfire cloud effect duration (in ticks). 20 ticks = 1 second."
        );

        this.radius = Modifier.load(config, "Dragonfire.Radius",
            Modifier.addictive(0).perLevel(1).capacity(5),
            "Dragonfire cloud effect radius."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION, level -> NumberUtil.format(this.getFireDuration(level) / 20D));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.getFireRadius(level)));
    }

    public int getFireDuration(int level) {
        return (int) this.duration.getValue(level);
    }

    public double getFireRadius(int level) {
        return this.radius.getValue(level);
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

        this.createCloud(shooter, arrow.getLocation(), event.getHitEntity(), event.getHitBlock(), event.getHitBlockFace(), level);
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {
        this.createCloud(shooter, victim.getLocation(), victim, null, null, level);
    }

    private void createCloud(@NotNull ProjectileSource shooter,
                             @NotNull Location location,
                             @Nullable Entity hitEntity,
                             @Nullable Block hitBlock,
                             @Nullable BlockFace hitFace,
                             int level) {

        // There are some tweaks to respect protection plugins by using event call.
        ItemStack itemStack = new ItemStack(Material.LINGERING_POTION);
        ItemUtil.editMeta(itemStack, PotionMeta.class, potionMeta -> {
            potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 20, 0), true);
        });

        ThrownPotion potion = shooter.launchProjectile(ThrownPotion.class);
        potion.setItem(itemStack);
        potion.teleportAsync(location);

        AreaEffectCloud cloud = potion.getWorld().spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter);
        cloud.setParticle(Particle.DRAGON_BREATH);
        cloud.setRadius((float) this.getFireRadius(level));
        cloud.setDuration(this.getFireDuration(level));
        cloud.setRadiusPerTick((7.0F - cloud.getRadius()) / (float) cloud.getDuration());
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);

        LingeringPotionSplashEvent splashEvent = new LingeringPotionSplashEvent(potion, hitEntity, hitBlock, hitFace, cloud);
        plugin.getPluginManager().callEvent(splashEvent);
        if (splashEvent.isCancelled()) {
            cloud.remove();
        }
        potion.remove();
    }
}
