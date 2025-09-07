package su.nightexpress.excellentenchants.enchantment.bow;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ArrowEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.io.File;

public class FlareEnchant extends GameEnchantment implements ArrowEnchant {

    public FlareEnchant(@NotNull EnchantsPlugin plugin, File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.ARROW, ArrowEffects.basic(Particle.ELECTRIC_SPARK));
        this.addComponent(EnchantComponent.PROBABILITY, Probability.oneHundred());
        this.addComponent(EnchantComponent.CHARGES, Charges.custom(Modifier.addictive(50), 1, 1, NightItem.fromType(Material.TORCH)));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getShootPriority() {
        return EnchantPriority.HIGH;
    }

    @Override
    public boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level) {
        return event.getProjectile() instanceof Arrow;
    }

    @Override
    public void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull Arrow projectile, int level) {
        Block block = event.getHitBlock();
        if (block == null) return;

        BlockFace face = event.getHitBlockFace();
        if (face == null || face == BlockFace.DOWN) return;

        Block relative = block.getRelative(face);
        if (!relative.getType().isAir()) return;

        if (projectile.getShooter() instanceof Player player) {
            BlockPlaceEvent placeEvent = new BlockPlaceEvent(relative, relative.getState(), block, new ItemStack(Material.TORCH),  player,true, EquipmentSlot.HAND);
            plugin.getPluginManager().callEvent(placeEvent);
            if (placeEvent.isCancelled() || !placeEvent.canBuild()) return;
        }

        this.plugin.runTask(relative.getLocation(), () -> {
            if (face == BlockFace.UP) {
                relative.setType(Material.TORCH);
            }
            else {
                relative.setType(Material.WALL_TORCH);

                Directional directional = (Directional) relative.getBlockData();
                directional.setFacing(face);
                relative.setBlockData(directional, true);
            }
        });
    }

    @Override
    public void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull Arrow arrow, int level) {

    }
}
