package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
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
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class VampiricArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    // 伤害比例（或固定值），跟“嗜血”的 Amount 一样
    private Modifier amount;
    // 是否按造成伤害的百分比来吸血
    private boolean multiplier;

    public VampiricArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, new ArrowEffects(UniParticle.redstone(Color.RED, 1F)));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 5));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        // 跟“嗜血”一样：Vampire.Amount + Vampire.Multiplier
        this.amount = Modifier.load(config, "Vampire.Amount",
                Modifier.addictive(0.02D).perLevel(0.01D).capacity(10D),
                "Amount of health to be restored for attacker.",
                "If 'Multiplier' is true, works as a fraction of inflicted damage."
        );

        this.multiplier = config.getBoolean("Vampire.Multiplier", true);

        // 如果是百分比，就显示为「xx%」，否则显示为具体数值
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> {
            double value = this.amount.getValue(level);
            if (this.multiplier) {
                // 0.02 -> "2"
                return NumberUtil.format(value * 100D);
            }
            else {
                return NumberUtil.format(value);
            }
        });
    }

    public double getAmount(int level) {
        return this.amount.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return event.getProjectile() instanceof Arrow;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Arrow arrow, int level) {
        // 不用在这里做事，回血在 onDamage 里处理
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event,
                         @NotNull LivingEntity shooter,
                         @NotNull LivingEntity victim,
                         @NotNull Arrow arrow,
                         int level) {

        if (shooter.isDead() || shooter.getHealth() <= 0D) return;

        double amount = this.getAmount(level);
        if (amount <= 0D) return;

        // 这次箭真正造成的伤害（带上护甲等计算后的）
        double inflicted = event.getFinalDamage();

        // 如果 Multiplier = true：按伤害比例回血；否则就是固定回血
        double healAmount = this.multiplier ? inflicted * amount : amount;
        if (healAmount <= 0D) return;

        double health = shooter.getHealth();
        double maxHealth = EntityUtil.getAttribute(shooter, Attribute.MAX_HEALTH);
        if (health >= maxHealth) return;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(shooter, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return;

        shooter.setHealth(Math.min(maxHealth, health + healAmount));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(shooter.getEyeLocation(), 0.25f, 0.15f, 5);
        }
    }
}
