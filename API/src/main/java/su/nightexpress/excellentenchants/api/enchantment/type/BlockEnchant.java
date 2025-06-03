package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface BlockEnchant extends CustomEnchantment {

    void onPlace(@NotNull BlockPlaceEvent event, @NotNull Player player, @NotNull Block block, @NotNull ItemStack itemStack);

    boolean canPlaceInContainers();
}
