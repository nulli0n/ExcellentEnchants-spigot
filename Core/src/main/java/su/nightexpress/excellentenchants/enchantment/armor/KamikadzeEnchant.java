package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.ResurrectEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class KamikadzeEnchant extends GameEnchantment implements DeathEnchant, ResurrectEnchant {

    private Modifier power;
    private boolean  onResurrect;

    public KamikadzeEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(0, 3));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.onResurrect = ConfigValue.create("Kamikadze.Apply_On_Resurrect",
            true,
            "Sets whether or not enchantment will trigger on resurrect (when a totem is used)."
        ).read(config);

        this.power = Modifier.load(config, "Kamikadze.Explosion_Power", Modifier.addictive(1).perLevel(1).capacity(5), "Explosion power.");
    }

    public boolean isOnResurrect() {
        return this.onResurrect;
    }

    public double getExplosionPower(int level) {
        return this.power.getValue(level);
    }

    public boolean createExplosion(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        Location location = entity.getLocation();
        Location eye = entity.getEyeLocation();
        float power = (float) this.getExplosionPower(level);

        return this.plugin.getEnchantManager().createExplosion(entity, location, power, false, false, explosion -> {
            if (this.hasVisualEffects()) explosion.setOnExplode(explodeEvent -> {
                UniParticle.of(Particle.SMOKE).play(eye, 0.5, 0.1, 60);
                UniParticle.of(Particle.LAVA).play(eye, 1.25, 0.1, 100);
            });
            explosion.setOnDamage(damageEvent -> {
                if (!(damageEvent.getEntity() instanceof LivingEntity)) {
                    damageEvent.setCancelled(true);
                }
            });
        });
    }

    @Override
    @NotNull
    public EnchantPriority getDeathPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    @NotNull
    public EnchantPriority getResurrectPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent event, @NotNull LivingEntity entity, ItemStack item, int level) {
        return this.createExplosion(entity, item, level);
    }

    @Override
    public boolean onResurrect(@NotNull EntityResurrectEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return this.onResurrect && this.createExplosion(entity, item, level);
    }
}
