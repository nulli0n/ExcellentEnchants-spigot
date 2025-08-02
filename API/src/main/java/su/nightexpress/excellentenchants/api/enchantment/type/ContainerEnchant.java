package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface ContainerEnchant extends CustomEnchantment {

    boolean onClick(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level);

    @NotNull EnchantPriority getClickPriority();
}
