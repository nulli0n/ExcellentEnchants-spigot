package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface BlockBreakEnchant extends IEnchantment {

    boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level);

    @NotNull
    default EventPriority getBreakPriority() {
        return EventPriority.HIGH;
    }
}
