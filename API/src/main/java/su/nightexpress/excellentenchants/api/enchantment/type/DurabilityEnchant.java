package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface DurabilityEnchant extends CustomEnchantment {

    boolean onItemDamage(@NotNull PlayerItemDamageEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level);

    @NotNull EnchantPriority getItemDamagePriority();
}
