package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class ElectrifiedArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    private Modifier damageAmount;

    public ElectrifiedArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);

        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 5));
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.ELECTRIC_SPARK));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.damageAmount = Modifier.load(config, "Electrified.DamageAmount",
            Modifier.addictive(1.25).perLevel(0.25).capacity(1000D),
            "Sets additional damage caused by enchantment's effect."
        );

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_DAMAGE, level -> NumberUtil.format(this.getDamage(level)));
    }

    public double getDamage(int level) {
        return this.damageAmount.getValue(level);
    }

    private void summonLightning(@NotNull Block block) {
        Location location = block.getLocation();
        this.plugin.runTask(location, () -> {
            block.getWorld().strikeLightningEffect(location);

            if (this.hasVisualEffects()) {
                Location center = LocationUtil.setCenter2D(location.add(0, 1, 0));
                UniParticle.blockCrack(block.getType()).play(center, 0.5, 0.1, 100);
                UniParticle.of(Particle.ELECTRIC_SPARK).play(center, 0.75, 0.05, 120);
            }
        });
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return true;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Arrow arrow, int level) {
        if (event.getHitEntity() != null || event.getHitBlock() == null) return;

        Block block = event.getHitBlock();
        this.summonLightning(block);
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {
        this.summonLightning(victim.getLocation().getBlock().getRelative(BlockFace.DOWN));
        event.setDamage(event.getDamage() + this.getDamage(level));
    }
}
