package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface InteractEnchant extends IEnchantment {

    boolean onInteract(@NotNull PlayerInteractEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level);

    @NotNull
    default EventPriority getInteractPriority() {
        return EventPriority.NORMAL;
    }
}
