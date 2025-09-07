package su.nightexpress.excellentenchants.enchantment.armor;


import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class ReboundEnchant extends GameEnchantment implements ProtectionEnchant {

    private Modifier modifier;
    private Modifier capacity;

    public ReboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.modifier = Modifier.load(config, "Rebound.Modifier",
            Modifier.addictive(0.2).perLevel(0.1).capacity(0.5),
            "Sets bounce power modifier based on fall distance.",
            "Greater value = greater rebound."
        );

        this.capacity = Modifier.load(config, "Rebound.Capacity",
            Modifier.addictive(0.75).perLevel(0.15).capacity(1.5D),
            "Sets maximal bounce power modifier value.",
            "Greater value = greater rebound."
        );
    }

    @Override
    @NotNull
    public EnchantPriority getProtectionPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    @NotNull
    public DamageBonus getDamageBonus() {
        return new DamageBonus(DamageBonusType.NORMAL);
    }

    @Override
    public boolean onProtection(@NotNull EntityDamageEvent event, @NotNull DamageBonus damageBonus, @NotNull LivingEntity entity, @NotNull ItemStack boots, int level) {
        if (event.getDamageSource().getDamageType() != DamageType.FALL) return false;
        if (entity instanceof Player player && player.isSneaking()) return false;

        event.setCancelled(true);

        this.bounceUp(entity, event.getDamage(), level);
        return true;
    }

    private void bounceUp(@NotNull LivingEntity entity, double damage, int level) {
        double limit = this.capacity.getValue(level);
        double modifier = this.modifier.getValue(level);
        double power = Math.min(limit, damage * modifier);

        if (this.hasVisualEffects()) {
            Location location = LocationUtil.setCenter2D(entity.getLocation());
            UniParticle.of(Particle.ITEM_SLIME).play(location, 0.45, 0.15, 50);
        }

        Vector velocity = entity.getVelocity();
        if (velocity.getY() < 0D) {
            this.plugin.runTask(entity, () -> entity.setVelocity(velocity.setY(power)));
        }
    }
}
