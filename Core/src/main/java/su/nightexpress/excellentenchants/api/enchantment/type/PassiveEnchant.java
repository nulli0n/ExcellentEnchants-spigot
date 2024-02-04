package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Periodic;

public interface PassiveEnchant extends IEnchantment, Periodic {

    boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level);
}
