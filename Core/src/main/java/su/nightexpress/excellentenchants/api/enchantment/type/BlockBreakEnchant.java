package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface BlockBreakEnchant {

    boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level);
}
