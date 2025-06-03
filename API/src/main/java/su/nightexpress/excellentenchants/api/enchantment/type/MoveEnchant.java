package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface MoveEnchant extends CustomEnchantment {

    boolean onMove(@NotNull PlayerMoveEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level);

    @NotNull EnchantPriority getMovePriority();
}
