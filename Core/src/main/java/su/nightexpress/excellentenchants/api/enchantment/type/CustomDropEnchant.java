package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CustomDropEnchant {

    @NotNull List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level);

    boolean isEventMustHaveDrops();
}
