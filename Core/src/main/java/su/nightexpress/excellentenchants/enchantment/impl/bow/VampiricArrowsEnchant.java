package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.EnchantmentTarget;
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
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ArrowImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;

public class VampiricArrowsEnchant extends ExcellentEnchant implements BowEnchant, Arrowed, Chanced {

    public static final String ID = "vampiric_arrows";

    public static final String PLACEHOLDER_HEAL_AMOUNT = "%heal_amount%";

    private ArrowImplementation arrowImplementation;
    private ChanceImplementation chanceImplementation;
    private EnchantScaler healAmount;

    public VampiricArrowsEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to restore " + PLACEHOLDER_HEAL_AMOUNT + "❤ on arrow hit.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
        this.getDefaults().setConflicts(EnchantEnderBow.ID, EnchantGhast.ID, EnchantBomber.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.arrowImplementation = ArrowImplementation.create(this, UniParticle.redstone(Color.RED, 1f));

        this.chanceImplementation = ChanceImplementation.create(this, "20.0 * " + Placeholders.ENCHANTMENT_LEVEL);

        this.healAmount = EnchantScaler.read(this, "Settings.Heal_Amount",
            Placeholders.ENCHANTMENT_LEVEL,
            "Amount of health to be restored on hit.");

        this.addPlaceholder(PLACEHOLDER_HEAL_AMOUNT, level -> NumberUtil.format(this.getHealAmount(level)));
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    @NotNull
    @Override
    public Arrowed getArrowImplementation() {
        return this.arrowImplementation;
    }

    @NotNull
    @Override
    public Chanced getChanceImplementation() {
        return this.chanceImplementation;
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
        if (!(event.getProjectile() instanceof Arrow arrow)) return false;
        if (!this.checkTriggerChance(level)) return false;

        this.addData(arrow);
        return true;
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
