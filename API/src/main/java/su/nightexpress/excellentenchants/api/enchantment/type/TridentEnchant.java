package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Trident;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantPriority;

public interface TridentEnchant extends ProjectileEnchant<Trident> {

    boolean onLaunch(@NotNull ProjectileLaunchEvent event, @NotNull LivingEntity shooter, @NotNull ItemStack trident, int level);

    @NotNull EnchantPriority getLaunchPriority();
}
