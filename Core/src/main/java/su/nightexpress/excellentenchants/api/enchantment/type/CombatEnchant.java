package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface CombatEnchant extends IEnchantment {

    boolean onAttack(@NotNull EntityDamageByEntityEvent e,
                     @NotNull LivingEntity damager, @NotNull LivingEntity victim,
                     @NotNull ItemStack weapon, int level);

    boolean onProtect(@NotNull EntityDamageByEntityEvent e,
                      @NotNull LivingEntity damager, @NotNull LivingEntity victim,
                      @NotNull ItemStack weapon, int level);
}
