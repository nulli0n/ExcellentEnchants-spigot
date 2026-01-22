package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface BlockChangeEnchant extends CustomEnchantment {

    boolean onBlockChange(@NotNull EntityChangeBlockEvent event, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level);
}
