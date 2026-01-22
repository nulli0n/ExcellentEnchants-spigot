package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockChangeEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

public class LightweightEnchant extends GameEnchantment implements BlockChangeEnchant, InteractEnchant {

    public LightweightEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    public boolean onBlockChange(@NotNull EntityChangeBlockEvent event, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level) {
        Block block = event.getBlock();
        Material origin = block.getType();
        Material target = event.getTo();

        if (origin == Material.FARMLAND && target == Material.DIRT) {
            event.setCancelled(true);
            return true;
        }

        if (origin ==  Material.BIG_DRIPLEAF && target == Material.BIG_DRIPLEAF && !entity.isSneaking()) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean onInteract(@NotNull PlayerInteractEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (event.getAction() != Action.PHYSICAL) return false;

        Block block = event.getClickedBlock();
        if (block == null) return false;

        if (block.getType() == Material.TURTLE_EGG) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }

    @Override
    @NotNull
    public EnchantPriority getInteractPriority() {
        return EnchantPriority.NORMAL;
    }
}
