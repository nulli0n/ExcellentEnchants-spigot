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
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class VampiricArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier healAmount;

    public VampiricArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, new ArrowEffects(UniParticle.redstone(Color.RED, 1F)));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(20, 5));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.healAmount = Modifier.load(config, "Vampire.Heal_Amount",
            Modifier.addictive(1).perLevel(0.5).capacity(5),
            "Amount of hearts to be restore."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
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

    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {
        if (shooter.isDead() || shooter.getHealth() <= 0D) return;

        double healAmount = this.getHealAmount(level);
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
