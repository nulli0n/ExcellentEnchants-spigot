package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.enchantment.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class ThunderEnchant extends GameEnchantment implements AttackEnchant {

    private boolean  thunderstormOnly;
    /** 现在是“额外伤害百分比”，比如 25 = 25% */
    private Modifier damagePercent;

    public ThunderEnchant(@NotNull EnchantsPlugin plugin,
                          @NotNull File file,
                          @NotNull EnchantData data) {
        super(plugin, file, data);
        // 对应 YAML 里的 Probability：Base 5, Per_Level 2
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.thunderstormOnly = ConfigValue.create("Thunder.During_Thunderstorm_Only",
                false,
                "Sets whether or not enchantment will have effect only during thunderstorm in the world."
        ).read(config);

        // 读取百分比配置：Thunder.Damage_Modifier（单位：%）
        this.damagePercent = Modifier.load(config, "Thunder.Damage_Modifier",
                // 默认 25% + 每级 5%，你也可以只在 YML 里改
                Modifier.addictive(25D).perLevel(5D).capacity(1000D),
                "Sets additional damage caused by enchantment's effect (in % of base damage)."
        );

        // %damage% 占位符：显示“百分比”本身，例如 25、30、35……
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE,
                level -> NumberUtil.format(this.getDamagePercent(level)));
    }

    public boolean isDuringThunderstormOnly() {
        return thunderstormOnly;
    }

    /** 返回额外伤害百分比，例如 25 = 25% */
    public double getDamagePercent(int level) {
        return this.damagePercent.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event,
                            @NotNull LivingEntity damager,
                            @NotNull LivingEntity victim,
                            @NotNull ItemStack weapon,
                            int level) {

        // 只在雷暴天气生效的开关
        if (this.isDuringThunderstormOnly() && !victim.getWorld().isThundering()) return false;
        // 必须能看到天空（光照来自天空为 15），防止室内乱劈
        if (victim.getLocation().getBlock().getLightFromSky() != 15) return false;

        Location location = victim.getLocation();
        victim.getWorld().strikeLightningEffect(location); // 只播放雷击效果，不用原版雷伤

        // 粒子特效
        if (this.hasVisualEffects()) {
            Block block = location.getBlock().getRelative(BlockFace.DOWN);
            Location center = LocationUtil.getCenter(location);
            UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
            UniParticle.of(Particle.ELECTRIC_SPARK).play(center, 0.75, 0.05, 120);
        }

        double baseDamage = event.getDamage();           // 当前这一刀原始伤害
        double percent    = this.getDamagePercent(level); // 比如 25 = 25%

        if (percent <= 0) return false;

        // 额外伤害 = 原伤害 * (percent / 100)
        double extraDamage = baseDamage * (percent / 100D);
        event.setDamage(baseDamage + extraDamage);

        return true;
    }
}
