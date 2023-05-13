package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface FishingEnchant extends IEnchantment {

    boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level);
}
