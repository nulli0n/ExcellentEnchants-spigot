package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class VampiricArrowsEnchant extends GameEnchantment implements BowEnchant, ArrowMeta, ChanceMeta {

    public static final String ID = "vampiric_arrows";

    private Modifier healAmount;

    public VampiricArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to restore " + GENERIC_AMOUNT + "❤ on arrow hit.",
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.BOWS,
            Lists.newSet(EnderBowEnchant.ID, GhastEnchant.ID, BomberEnchant.ID)
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setArrowEffects(ArrowEffects.create(config, UniParticle.redstone(Color.RED, 1f)));

        this.meta.setProbability(Probability.create(config, Modifier.add(8, 4, 1, 100)));

        this.healAmount = Modifier.read(config, "Settings.Heal_Amount",
            Modifier.add(0, 1, 1, 10),
            "Amount of health to be restored on hit."
        );

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
    }

    public double getHealAmount(int level) {
        return this.healAmount.getValue(level);
    }

    @NotNull
    @Override
    public EventPriority getDamagePriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Arrow)) return false;

        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (shooter.isDead() || shooter.getHealth() <= 0D) return false;

        double healAmount = this.getHealAmount(level);
        if (healAmount <= 0D) return false;

        double health = shooter.getHealth();
        double maxHealth = EntityUtil.getAttribute(shooter, Attribute.GENERIC_MAX_HEALTH);
        if (health >= maxHealth) return false;

        EntityRegainHealthEvent healthEvent = new EntityRegainHealthEvent(shooter, healAmount, EntityRegainHealthEvent.RegainReason.CUSTOM);
        plugin.getPluginManager().callEvent(healthEvent);
        if (healthEvent.isCancelled()) return false;

        shooter.setHealth(Math.min(maxHealth, health + healAmount));

        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.HEART).play(shooter.getEyeLocation(), 0.25f, 0.15f, 5);
        }
        return false;
    }
}
