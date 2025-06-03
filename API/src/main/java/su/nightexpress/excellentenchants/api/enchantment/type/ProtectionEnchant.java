package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface ProtectionEnchant extends CustomEnchantment {

    boolean onProtection(@NotNull EntityDamageEvent event, @NotNull DamageBonus damageBonus, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level);

    @NotNull DamageBonus getDamageBonus();

    @NotNull EnchantPriority getProtectionPriority();
}
