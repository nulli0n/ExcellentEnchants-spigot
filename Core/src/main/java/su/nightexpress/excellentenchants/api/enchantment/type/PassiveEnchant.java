package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface PassiveEnchant extends IEnchantment {

    boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level);
}
