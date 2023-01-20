package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface DamageEnchant extends IEnchantment {

    boolean onDamage(@NotNull EntityDamageEvent e, @NotNull LivingEntity entity, @NotNull ItemStack item, int level);
}
