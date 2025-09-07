package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.io.File;

public class EnderBowEnchant extends GameEnchantment implements BowEnchant {

    public EnderBowEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.CHARGES, Charges.custom(Modifier.addictive(50), 1, 1, NightItem.fromType(Material.ENDER_PEARL)));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        if (!(event.getProjectile() instanceof Projectile projectile)) return false;

        EnderPearl pearl = shooter.launchProjectile(EnderPearl.class);
        this.plugin.runTask(pearl, () -> pearl.setVelocity(projectile.getVelocity()));
        event.setProjectile(pearl);
        return true;
    }
}
