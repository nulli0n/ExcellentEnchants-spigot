package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public interface MoveEnchant {

    boolean use(@NotNull PlayerMoveEvent e, @NotNull LivingEntity entity, int level);
}
