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
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.*;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ArrowSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.PotionSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class WitheredArrowsEnchant extends AbstractEnchantmentData implements ChanceData, ArrowData, PotionData, BowEnchant {

    public static final String ID = "withered_arrows";

    private ArrowSettingsImpl  arrowSettings;
    private ChanceSettingsImpl chanceSettings;
    private PotionSettingsImpl potionSettings;

    public WitheredArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to launch an arrow with " + ENCHANTMENT_POTION_TYPE + " " + ENCHANTMENT_POTION_LEVEL + " (" + ENCHANTMENT_POTION_DURATION + "s.)");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
        this.setConflicts(EnderBowEnchant.ID, GhastEnchant.ID, BomberEnchant.ID);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.arrowSettings = ArrowSettingsImpl.create(config, UniParticle.of(Particle.SPELL_WITCH));

        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(4, 3, 1, 100));

        this.potionSettings = PotionSettingsImpl.create(this, config, PotionEffectType.WITHER, false,
            Modifier.add(3, 1, 1, 60),
            Modifier.add(0, 1, 1, 5)
        );
    }

    @NotNull
    @Override
    public ArrowSettings getArrowSettings() {
        return arrowSettings;
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @NotNull
    @Override
    public PotionSettings getPotionSettings() {
        return potionSettings;
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return false;
        if (!this.checkTriggerChance(level)) return false;

        arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
        return arrow.addCustomEffect(this.createEffect(level), true);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
