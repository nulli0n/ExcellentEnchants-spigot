package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
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
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class ExplosiveArrowsEnchant extends GameEnchantment implements ArrowEnchant {

    private boolean fireSpread;
    private boolean damageItems;
    private boolean damageBlocks;
    private Modifier power;

    public ExplosiveArrowsEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.SMOKE));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(3, 2));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fireSpread = ConfigValue.create("Explosion.Fire_Spread",
            true,
            "Controls whether explosion set nearby blocks on fire.").read(config);

        this.damageItems = ConfigValue.create("Explosion.Damage_Items",
            false,
            "Controls whether explosion can destroy ground items.").read(config);

        this.damageBlocks = ConfigValue.create("Explosion.Damage_Blocks",
            false,
            "Controls whether explosion can break blocks.").read(config);

        this.power = Modifier.load(config, "Explosion.Power",
            Modifier.addictive(1).perLevel(1).capacity(5),
            "Explosion power.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_RADIUS, level -> NumberUtil.format(this.getPower(level)));
    }

    public final double getPower(int level) {
        return this.power.getValue(level);
    }

    public final boolean isFireSpread() {
        return this.fireSpread;
    }

    public final boolean isDamageBlocks() {
        return this.damageBlocks;
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
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Arrow projectile, int level) {
        Location location = projectile.getLocation();
        float power = (float) this.getPower(level);

        this.plugin.getEnchantManager().createExplosion(shooter, location, power, this.fireSpread, this.damageBlocks, explosion -> {
            if (!this.damageItems) explosion.setOnDamage(damageEvent -> {
                if (damageEvent.getEntity() instanceof Item || damageEvent.getEntity() instanceof ItemFrame) {
                    damageEvent.setCancelled(true);
                }
            });
        });
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {

    }
}
