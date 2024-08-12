package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.PeriodMeta;

public interface PassiveEnchant extends CustomEnchantment, PeriodMeta {

    boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level);
}
