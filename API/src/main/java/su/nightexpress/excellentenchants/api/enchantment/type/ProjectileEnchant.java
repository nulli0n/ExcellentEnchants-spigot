package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface ProjectileEnchant<T extends AbstractArrow> extends CustomEnchantment {

    void onHit(@NotNull ProjectileHitEvent event, @NotNull LivingEntity shooter, @NotNull T projectile, int level);

    void onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull LivingEntity shooter, @NotNull LivingEntity victim, @NotNull T projectile, int level);
}
