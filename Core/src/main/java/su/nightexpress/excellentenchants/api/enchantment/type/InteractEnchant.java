package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface InteractEnchant extends IEnchantment {

    boolean onInteract(@NotNull PlayerInteractEvent e, @NotNull Player player, @NotNull ItemStack item, int level);
}
