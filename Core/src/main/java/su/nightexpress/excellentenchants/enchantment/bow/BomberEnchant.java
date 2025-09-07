package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.io.File;

public class BomberEnchant extends GameEnchantment implements BowEnchant {

    private Modifier fuseTicks;

    public BomberEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.CHARGES, Charges.custom(Modifier.addictive(50).perLevel(10).build(), 1, 1, NightItem.fromType(Material.TNT)));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.fuseTicks = Modifier.load(config, "Bomber.Fuse_Ticks",
            Modifier.addictive(40).perLevel(10).capacity(200),
            "Sets TNT fuse ticks.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_TIME, level -> NumberUtil.format((double) this.getFuseTicks(level) / 20D));
    }

    public int getFuseTicks(int level) {
        return (int) this.fuseTicks.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Projectile projectile)) return false;

        int fuseTicks = Math.max(1, this.getFuseTicks(level));

        this.plugin.runTask(projectile.getLocation(), () -> {
            TNTPrimed primed = projectile.getWorld().spawn(projectile.getLocation(), TNTPrimed.class);
            primed.setVelocity(projectile.getVelocity().multiply(event.getForce() * 1.25));
            primed.setFuseTicks(fuseTicks);
            primed.setSource(shooter);
            event.setProjectile(primed);
        });
        return true;
    }
}
