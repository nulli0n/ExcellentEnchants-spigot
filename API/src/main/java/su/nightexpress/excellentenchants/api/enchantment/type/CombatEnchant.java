package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface CombatEnchant extends CustomEnchantment {

    boolean onAttack(@NotNull EntityDamageByEntityEvent event,
                     @NotNull LivingEntity damager, @NotNull LivingEntity victim,
                     @NotNull ItemStack weapon, int level);

    boolean onProtect(@NotNull EntityDamageByEntityEvent event,
                      @NotNull LivingEntity damager, @NotNull LivingEntity victim,
                      @NotNull ItemStack weapon, int level);

    @NotNull
    default EventPriority getAttackPriority() {
        return EventPriority.NORMAL;
    }

    @NotNull
    default EventPriority getProtectPriority() {
        return EventPriority.NORMAL;
    }
}
