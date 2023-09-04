package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public interface BowEnchant extends IEnchantment {

    boolean onShoot(@NotNull EntityShootBowEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack bow, int level);

    boolean onHit(@NotNull ProjectileHitEvent event, @NotNull Projectile projectile, @NotNull ItemStack bow, int level);

    boolean onDamage(@NotNull EntityDamageByEntityEvent event, @NotNull Projectile projectile,
                     @NotNull LivingEntity shooter, @NotNull LivingEntity victim,
                     @NotNull ItemStack weapon, int level);
}
