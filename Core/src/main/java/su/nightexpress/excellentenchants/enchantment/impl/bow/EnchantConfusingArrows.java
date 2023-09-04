package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Particle;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ArrowImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;

public class EnchantConfusingArrows extends ExcellentEnchant implements Chanced, Arrowed, Potioned, BowEnchant {

    public static final String ID = "confusing_arrows";

    private ArrowImplementation arrowImplementation;
    private ChanceImplementation chanceImplementation;
    private PotionImplementation potionImplementation;

    public EnchantConfusingArrows(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to launch an arrow with " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " (" + Placeholders.ENCHANTMENT_POTION_DURATION + "s.)");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.1);
        this.getDefaults().setConflicts(EnchantEnderBow.ID, EnchantGhast.ID, EnchantHover.ID, EnchantBomber.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.arrowImplementation = ArrowImplementation.create(this, SimpleParticle.of(Particle.SPELL_MOB));
        this.chanceImplementation = ChanceImplementation.create(this,
            "20.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 5.0");
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.CONFUSION, false,
            "6.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 3.0",
            Placeholders.ENCHANTMENT_LEVEL);
    }

    @NotNull
    @Override
    public ArrowImplementation getArrowImplementation() {
        return arrowImplementation;
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @NotNull
    @Override
    public PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!this.isAvailableToUse(shooter)) return false;
        if (!(event.getProjectile() instanceof Arrow arrow)) return false;
        if (!this.checkTriggerChance(level)) return false;

        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        return arrow.addCustomEffect(this.createEffect(level), true);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return this.isOurProjectile(projectile);
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return this.isOurProjectile(projectile);
    }
}
