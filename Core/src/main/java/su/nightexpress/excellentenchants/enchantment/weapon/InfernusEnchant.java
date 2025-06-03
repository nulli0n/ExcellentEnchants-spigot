package su.nightexpress.excellentenchants.enchantment.weapon;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.type.TridentEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class InfernusEnchant extends GameEnchantment implements TridentEnchant {

    private Modifier fireTicks;

    public InfernusEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.FLAME));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fireTicks = Modifier.load(config, "Infernus.Fire_Ticks",
            Modifier.addictive(60).perLevel(20).capacity(120),
            "Sets for how long (in ticks) entity will be ignited on hit. 20 ticks = 1 second.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format((double) this.getFireTicks(level) / 20D));
    }

    public int getFireTicks(int level) {
        return (int) this.fireTicks.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getLaunchPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onLaunch(@NotNull ProjectileLaunchEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack trident, int level) {
        event.getEntity().setFireTicks(Integer.MAX_VALUE);
        return true;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Trident projectile, int level) {
        Entity entity = event.getHitEntity();
        if (entity == null) return;

        int ticks = this.getFireTicks(level);
        entity.setFireTicks(ticks);
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Trident projectile, int level) {

    }
}
