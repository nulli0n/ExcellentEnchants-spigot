package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface BlockDropEnchant {

    boolean use(@NotNull BlockDropItemEvent e, @NotNull Player player, @NotNull ItemStack item, int level);
}
