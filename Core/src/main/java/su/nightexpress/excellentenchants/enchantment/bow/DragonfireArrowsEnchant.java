package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class DragonfireArrowsEnchant extends GameEnchantment implements ArrowEnchant, Listener {

    // 插件引用，用来注册事件
    private final EnchantsPlugin plugin;

    // 云标记 key
    private final NamespacedKey cloudKey;
    // 存箭伤害的 key
    private final NamespacedKey cloudDamageKey;
    // 存附魔等级的 key
    private final NamespacedKey cloudLevelKey;

    private Modifier duration;
    private Modifier radius;
    // 龙息云伤害倍率
    private Modifier damageMultiplier;

    public DragonfireArrowsEnchant(@NotNull EnchantsPlugin plugin,
                                   @NotNull File file,
                                   @NotNull EnchantData data) {
        super(plugin, file, data);
        this.plugin = plugin;

        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.DRAGON_BREATH));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(4, 3));

        // 给这个附魔自己的云打个标签，用来在事件里识别
        this.cloudKey = new NamespacedKey(plugin, "dragonfire_cloud");
        // 额外数据 key
        this.cloudDamageKey = new NamespacedKey(plugin, "dragonfire_cloud_damage");
        this.cloudLevelKey = new NamespacedKey(plugin, "dragonfire_cloud_level");
        // 事件注册放到 loadAdditional() 里做，方便重载时重新注册
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

        // 从 Dragonfire.Damage_Multiplier 读取配置
        this.damageMultiplier = Modifier.load(config, "Dragonfire.Damage_Multiplier",
                Modifier.addictive(0.25).perLevel(0).capacity(1.0),
                "Extra damage = ArrowDamage * Damage_Multiplier."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DURATION,
                level -> NumberUtil.format(this.getFireDuration(level) / 20D));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS,
                level -> NumberUtil.format(this.getFireRadius(level)));

        // 每次加载 / 重载配置时，重新注册事件监听
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    public int getFireDuration(int level) {
        return (int) this.duration.getValue(level);
    }

    public double getFireRadius(int level) {
        return this.radius.getValue(level);
    }

    public double getDamageMultiplier(int level) {
        double value = this.damageMultiplier.getValue(level);
        // 强制限制在 0.0 ~ 1.0 之间，防止配置写炸
        if (value < 0.0) value = 0.0;
        if (value > 1.0) value = 1.0;
        return value;
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
            // 击中实体的情况在 onDamage 里处理云
            return;
        }

        // 击中方块时没有实体伤害，用箭当前 damage 作为基准
        double arrowDamage = arrow.getDamage();

        this.createCloud(
                shooter,
                arrow.getLocation(),
                event.getHitEntity(),
                event.getHitBlock(),
                event.getHitBlockFace(),
                level,
                arrowDamage
        );
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event,
                         @NotNull LivingEntity shooter,
                         @NotNull LivingEntity victim,
                         @NotNull Arrow arrow,
                         int level) {

        // 用这次箭对目标造成的最终伤害作为基准伤害
        double arrowDamage = event.getFinalDamage();

        this.createCloud(
                shooter,
                victim.getLocation(),
                victim,
                null,
                null,
                level,
                arrowDamage
        );
    }

    private void createCloud(@NotNull ProjectileSource shooter,
                             @NotNull Location location,
                             @Nullable Entity hitEntity,
                             @Nullable Block hitBlock,
                             @Nullable BlockFace hitFace,
                             int level,
                             double arrowDamage) {

        World world = location.getWorld();
        if (world == null) {
            return;
        }

        double radius = this.getFireRadius(level);
        int duration = this.getFireDuration(level);

        // 直接生成龙息云
        AreaEffectCloud cloud = world.spawn(location, AreaEffectCloud.class);
        cloud.clearCustomEffects();
        cloud.setSource(shooter instanceof LivingEntity ? (LivingEntity) shooter : null);
        cloud.setParticle(Particle.DRAGON_BREATH);

        // 半径 & 持续时间
        cloud.setRadius((float) radius);
        cloud.setDuration(duration);
        // 从当前半径线性变化到 5 格
        cloud.setRadiusPerTick((5.0F - cloud.getRadius()) / (float) cloud.getDuration());

        // 保留对生物的基础 instant_damage 效果
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true);

        // PDC 里打标签 + 存箭伤害 & 附魔等级
        PersistentDataContainer data = cloud.getPersistentDataContainer();
        data.set(this.cloudKey, PersistentDataType.BYTE, (byte) 1);
        data.set(this.cloudDamageKey, PersistentDataType.DOUBLE, arrowDamage);
        data.set(this.cloudLevelKey, PersistentDataType.INTEGER, level);
    }

    /**
     * 只针对“龙息附魔生成的云”：不让玩家吃到效果（包括 instant_damage 和额外伤害）。
     */
    @EventHandler
    public void onCloudApply(AreaEffectCloudApplyEvent event) {
        AreaEffectCloud cloud = event.getEntity();

        if (!cloud.getPersistentDataContainer().has(this.cloudKey, PersistentDataType.BYTE)) {
            return; // 不是这个附魔生成的云，忽略
        }

        // 移除所有玩家，玩家就不会吃到 INSTANT_DAMAGE 效果
        event.getAffectedEntities().removeIf(entity -> entity instanceof Player);
    }

    /**
     * 额外伤害逻辑：
     * 如果伤害来源是“龙息云”，则：
     *   FinalDamage += ArrowDamage * Damage_Multiplier(level)
     */
    @EventHandler
    public void onDragonCloudDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof AreaEffectCloud cloud)) return;

        PersistentDataContainer data = cloud.getPersistentDataContainer();
        if (!data.has(this.cloudKey, PersistentDataType.BYTE)) {
            // 不是龙息附魔的云
            return;
        }

        // 这里理论上玩家已经不会被云命中（onCloudApply 里已经移除了）
        // 保险一点：如果目标是玩家就直接返回
        if (event.getEntity() instanceof Player) {
            return;
        }

        Integer level = data.get(this.cloudLevelKey, PersistentDataType.INTEGER);
        Double baseArrowDamage = data.get(this.cloudDamageKey, PersistentDataType.DOUBLE);

        if (level == null || baseArrowDamage == null) {
            return;
        }

        double multiplier = this.getDamageMultiplier(level);
        if (multiplier <= 0) {
            return;
        }

        double extra = baseArrowDamage * multiplier;
        if (extra <= 0) {
            return;
        }

        double before = event.getDamage();
        event.setDamage(before + extra);
    }
}
