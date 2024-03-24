package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;

public interface FishingEnchant extends EnchantmentData {

    boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level);

    @NotNull
    default EventPriority getFishingPriority() {
        return EventPriority.NORMAL;
    }
}
