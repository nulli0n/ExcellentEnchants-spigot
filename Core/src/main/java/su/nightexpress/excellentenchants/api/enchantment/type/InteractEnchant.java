package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface InteractEnchant {

    boolean use(@NotNull PlayerInteractEvent e, @NotNull Player player, @NotNull ItemStack item, int level);
}
