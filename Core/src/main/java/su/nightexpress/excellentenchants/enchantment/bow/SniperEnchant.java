package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class SniperEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier speedModifier;

    public SniperEnchant(@NotNull EnchantsPlugin plugin,
                         @NotNull File file,
                         @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.speedModifier = Modifier.load(config, "Sniper.Speed_Modifier",
                Modifier.addictive(1D).perLevel(0.2D).capacity(3D),
                "Projectile's speed modifier."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT,
                level -> NumberUtil.format(this.getSpeedModifier(level) * 100D));
    }

    public double getSpeedModifier(int level) {
        return this.speedModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event,
                           @NotNull LivingEntity shooter,
                           @NotNull ItemStack bow,
                           int level) {

        if (!(event.getProjectile() instanceof Arrow arrow)) return false;

        double modifier = this.getSpeedModifier(level);

        Vector vector = arrow.getVelocity();
        arrow.setVelocity(vector.multiply(modifier));

        // 关掉原版暴击粒子
        arrow.setCritical(false);

        // 轨迹：白色烟花火花（不能改色，但是好看）
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || !arrow.isValid() || arrow.isOnGround() || arrow.isInBlock()) {
                    cancel();
                    return;
                }

                UniParticle.of(Particle.FIREWORK)
                        .play(arrow.getLocation(), 0.0, 0.0, 8);
            }
        }.runTaskTimer(plugin, 0L, 1L);

        return true;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event,
                      @NotNull LivingEntity shooter,
                      @NotNull Arrow arrow,
                      int level) {
        // 命中方块暂时不做特效
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event,
                         @NotNull LivingEntity shooter,
                         @NotNull LivingEntity victim,
                         @NotNull Arrow arrow,
                         int level) {

        if (!this.hasVisualEffects()) return;

        // 命中实体时：彩色 DUST 粒子（可自定义颜色）
        // 例如紫色：Color.fromRGB(170, 0, 255)
        UniParticle.redstone(Color.fromRGB(170, 0, 255), 1.2F)
                .play(victim.getEyeLocation(), 0.4, 0.25, 40);
    }
}
