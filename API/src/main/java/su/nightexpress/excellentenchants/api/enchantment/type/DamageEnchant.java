package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface DamageEnchant extends CustomEnchantment {

    boolean onDamage(@NotNull EntityDamageEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level);

    @NotNull
    default EventPriority getDamagePriority() {
        return EventPriority.NORMAL;
    }
}
