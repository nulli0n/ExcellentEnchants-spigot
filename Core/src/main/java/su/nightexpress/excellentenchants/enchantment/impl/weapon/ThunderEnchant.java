package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class ThunderEnchant extends AbstractEnchantmentData implements ChanceData, CombatEnchant {

    public static final String ID = "thunder";

    private boolean            thunderstormOnly;
    private ChanceSettingsImpl chanceSettings;
    private Modifier           damageModifier;

    public ThunderEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.UNCOMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(5, 2, 1, 100));

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
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public boolean isDuringThunderstormOnly() {
        return thunderstormOnly;
    }

    public double getDamage(int level) {
        return this.damageModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (this.isDuringThunderstormOnly() && !victim.getWorld().isThundering()) return false;
        if (victim.getLocation().getBlock().getLightFromSky() != 15) return false;
        if (!this.checkTriggerChance(level)) return false;

        Location location = victim.getLocation();
        victim.getWorld().strikeLightningEffect(location);

        if (this.hasVisualEffects()) {
            Block block = location.getBlock().getRelative(BlockFace.DOWN);
            Location center = LocationUtil.getCenter(location);
            UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
            UniParticle.of(Particle.FIREWORKS_SPARK).play(center, 0.75, 0.05, 120);
        }

        event.setDamage(event.getDamage() + this.getDamage(level));

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
