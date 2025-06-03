package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface FishingEnchant extends CustomEnchantment {

    boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level);

    @NotNull EnchantPriority getFishingPriority();
}
