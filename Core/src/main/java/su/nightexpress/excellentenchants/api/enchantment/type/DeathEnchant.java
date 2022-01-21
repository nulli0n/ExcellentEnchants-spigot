package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

public interface DeathEnchant {

    boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level);
}
