package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class GhastEnchant extends GameEnchantment implements BowEnchant {

    private boolean  fireSpread;
    private Modifier yield;

    public GhastEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fireSpread = ConfigValue.create("Fireball.Fire_Spread",
            true,
            "Controls whether fireball explosion sets nearby blocks on fire.").read(config);

        this.yield = Modifier.load(config, "Fireball.Yield",
            Modifier.addictive(2).perLevel(0).capacity(5),
            "Fireball explosion power.");
    }

    public boolean isFireSpread() {
        return this.fireSpread;
    }

    public float getYield(int level) {
        return (float) this.yield.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Projectile projectile)) return false;

        Fireball fireball;

        // Shoot small fireballs for the Multishot enchantment,
        // as large ones has a slow speed and punches each other on shoot.
        if (EnchantUtils.contains(bow, Enchantment.MULTISHOT)) {
            fireball = shooter.launchProjectile(SmallFireball.class);
            this.plugin.runTask(fireball, () -> fireball.setVelocity(projectile.getVelocity().normalize().multiply(0.5f)));
        }
        else {
            fireball = shooter.launchProjectile(Fireball.class);
            this.plugin.runTask(fireball, () -> fireball.setDirection(projectile.getVelocity()));
        }
        fireball.setIsIncendiary(this.fireSpread);
        fireball.setYield(this.getYield(level));

        event.setProjectile(fireball);
        return true;
    }
}
