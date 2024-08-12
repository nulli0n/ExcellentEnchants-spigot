package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_DAMAGE;

public class ThunderEnchant extends GameEnchantment implements ChanceMeta, CombatEnchant {

    public static final String ID = "thunder";

    private boolean  thunderstormOnly;
    private Modifier damageModifier;

    public ThunderEnchant(@NotNull EnchantsPlugin plugin, File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.",
            EnchantRarity.RARE,
            5,
            ItemCategories.WEAPON
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(5, 2, 1, 100)));

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

    public boolean isDuringThunderstormOnly() {
        return thunderstormOnly;
    }

    public double getDamage(int level) {
        return this.damageModifier.getValue(level);
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
            UniParticle.of(Particle.ELECTRIC_SPARK).play(center, 0.75, 0.05, 120);
        }

        event.setDamage(event.getDamage() + this.getDamage(level));

        return true;
    }

    @Override
    public boolean onProtect(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        return false;
    }
}
