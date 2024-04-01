package su.nightexpress.excellentenchants.enchantment.impl.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowData;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowSettings;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ArrowSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_DAMAGE;

public class ElectrifiedArrowsEnchant extends AbstractEnchantmentData implements ChanceData, ArrowData, BowEnchant {

    public static final String ID = "electrified_arrows";

    private ArrowSettingsImpl  arrowSettings;
    private ChanceSettingsImpl chanceSettings;
    private Modifier           damageModifier;
    private boolean            thunderstormOnly;

    public ElectrifiedArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance for an arrow to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.UNCOMMON);
        this.setConflicts(EnderBowEnchant.ID, GhastEnchant.ID, BomberEnchant.ID);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.arrowSettings = ArrowSettingsImpl.create(config, UniParticle.of(Particle.FIREWORKS_SPARK));

        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(0, 5, 1, 100));

        this.thunderstormOnly = ConfigValue.create("Settings.During_Thunderstorm_Only",
            false,
            "Sets whether or not enchantment will have effect only during thunderstorm in the world."
        ).read(config);

        this.damageModifier = Modifier.read(config, "Settings.Damage_Modifier",
            Modifier.add(1.25, 0.25, 1, 10000),
            "Sets additional damage caused by enchantment's effect."
        );

        this.addPlaceholder(GENERIC_DAMAGE, level -> NumberUtil.format(this.getDamage(level)));
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

    public boolean isDuringThunderstormOnly() {
        return thunderstormOnly;
    }

    public double getDamage(int level) {
        return this.damageModifier.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BOW;
    }

    private void summonLightning(@NotNull Block block) {
        Location location = block.getLocation();
        block.getWorld().strikeLightningEffect(location);

        if (this.hasVisualEffects()) {
            Location center = LocationUtil.getCenter(location.add(0, 1, 0), false);
            UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
            UniParticle.of(Particle.FIREWORKS_SPARK).play(center, 0.75, 0.05, 120);
        }
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return this.checkTriggerChance(level);
    }

    @Override
    public boolean onHit(@NotNull ProjectileHitEvent event, LivingEntity user, @NotNull Projectile projectile, @NotNull ItemStack bow, int level) {
        if (event.getHitEntity() != null || event.getHitBlock() == null) return false;

        Block block = event.getHitBlock();
        this.summonLightning(block);
        return false;
    }

    @Override
    public boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        this.summonLightning(victim.getLocation().getBlock().getRelative(BlockFace.DOWN));
        event.setDamage(event.getDamage() + this.getDamage(level));
        return false;
    }
}
