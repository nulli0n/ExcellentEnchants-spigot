package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface InventoryEnchant extends CustomEnchantment {

    boolean onDeath(@NotNull PlayerDeathEvent event, @NotNull Player player, ItemStack itemStack, int level);
}
