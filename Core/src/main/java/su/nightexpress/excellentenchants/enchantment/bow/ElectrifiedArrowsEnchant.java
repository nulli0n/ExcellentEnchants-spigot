package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class ElectrifiedArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    /** 固定额外伤害（❤，对应 Electrified.DamageAmount） */
    private Modifier damageAmount;
    /** 额外伤害百分比（单位：%，对应 Electrified.Damage_Multiplier，例如 30 = 30%） */
    private Modifier damagePercent;

    public ElectrifiedArrowsEnchant(@NotNull EnchantsPlugin plugin,
                                    @NotNull File file,
                                    @NotNull EnchantData data) {
        super(plugin, file, data);

        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 5));
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.ELECTRIC_SPARK));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        // 固定额外伤害（❤）
        this.damageAmount = Modifier.load(config, "Electrified.DamageAmount",
                // 默认给 0，所有数值交给 YAML 控制
                Modifier.addictive(0D).perLevel(0D).capacity(1000D),
                "Sets flat additional damage caused by enchantment's effect."
        );

        // 百分比额外伤害（单位：%）
        this.damagePercent = Modifier.load(config, "Electrified.Damage_Multiplier",
                // 默认 30% 额外伤害，具体由 YAML 覆盖
                Modifier.addictive(30D).perLevel(0D).capacity(1000.0),
                "Extra damage (in % of arrow damage)."
        );

        // %damage%：显示当前等级的「百分比」数值，例如 30、35……
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE,
                level -> NumberUtil.format(this.getDamagePercent(level)));
    }

    /** 固定额外伤害（❤） */
    public double getFlatDamage(int level) {
        if (this.damageAmount == null) return 0D;
        return this.damageAmount.getValue(level);
    }

    /** 百分比额外伤害，例如 30 = 30% */
    public double getDamagePercent(int level) {
        if (this.damagePercent == null) return 0D;
        double value = this.damagePercent.getValue(level);
        if (value < 0.0) value = 0.0; // 防止写成负数
        return value;
    }

    /** 召唤闪电效果（原版 strikeLightningEffect） */
    private void summonLightning(@NotNull Block block) {
        World world = block.getWorld();
        if (world == null) return;

        Location location = block.getLocation().add(0.5, 0, 0.5);
        world.strikeLightningEffect(location);

        if (this.hasVisualEffects()) {
            Location center = LocationUtil.setCenter2D(location.clone().add(0, 1, 0));
            UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
            UniParticle.of(Particle.ELECTRIC_SPARK).play(center, 0.75, 0.05, 120);
        }
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
        return true;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event,
                      @NotNull LivingEntity shooter,
                      @NotNull Arrow arrow,
                      int level) {

        if (event.getHitEntity() != null) {
            // 打到实体时，雷在 onDamage 里处理
            return;
        }

        if (event.getHitBlock() == null) return;

        Block block = event.getHitBlock();
        this.summonLightning(block);
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event,
                         @NotNull LivingEntity shooter,
                         @NotNull LivingEntity victim,
                         @NotNull Arrow arrow,
                         int level) {

        // 在受击实体脚下那一格召唤闪电效果
        Block block = victim.getLocation().getBlock().getRelative(BlockFace.DOWN);
        this.summonLightning(block);

        // 当前这次箭的基础伤害（包含其它附魔 & 暴击等）
        double base = event.getDamage();

        double extraFlat     = this.getFlatDamage(level);         // 固定额外❤
        double percent       = this.getDamagePercent(level);      // 百分比数值，例如 30
        double extraFromPercent = base * (percent / 100D);        // 百分比额外伤害

        double finalDamage = base + extraFlat + extraFromPercent;
        event.setDamage(finalDamage);
    }
}
