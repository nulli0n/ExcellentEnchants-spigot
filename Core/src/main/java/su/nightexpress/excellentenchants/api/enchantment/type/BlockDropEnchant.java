package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface BlockDropEnchant extends IEnchantment {

    boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull Player player, @NotNull ItemStack item, int level);
}
