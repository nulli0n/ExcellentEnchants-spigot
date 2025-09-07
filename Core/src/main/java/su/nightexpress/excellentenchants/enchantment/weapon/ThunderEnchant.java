package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.AttackEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class ThunderEnchant extends GameEnchantment implements AttackEnchant {

    private boolean  thunderstormOnly;
    private Modifier damageModifier;

    public ThunderEnchant(@NotNull EnchantsPlugin plugin, File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(5, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.thunderstormOnly = ConfigValue.create("Thunder.During_Thunderstorm_Only",
            false,
            "Sets whether or not enchantment will have effect only during thunderstorm in the world."
        ).read(config);

        this.damageModifier = Modifier.load(config, "Thunder.Damage_Modifier",
            Modifier.addictive(1.25).perLevel(0.25).capacity(1000D),
            "Sets additional damage caused by enchantment's effect."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.getDamage(level)));
    }

    public boolean isDuringThunderstormOnly() {
        return thunderstormOnly;
    }

    public double getDamage(int level) {
        return this.damageModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getAttackPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean onAttack(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (this.isDuringThunderstormOnly() && !victim.getWorld().isThundering()) return false;
        if (victim.getLocation().getBlock().getLightFromSky() != 15) return false;

        Location location = victim.getLocation();
        this.plugin.runTask(location, () -> victim.getWorld().strikeLightningEffect(location));

        if (this.hasVisualEffects()) {
            Block block = location.getBlock().getRelative(BlockFace.DOWN);
            Location center = LocationUtil.getCenter(location);
            this.plugin.runTask(location, () -> {
                UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
                UniParticle.of(Particle.ELECTRIC_SPARK).play(center, 0.75, 0.05, 120);
            });
        }

        event.setDamage(event.getDamage() + this.getDamage(level));

        return true;
    }
}
