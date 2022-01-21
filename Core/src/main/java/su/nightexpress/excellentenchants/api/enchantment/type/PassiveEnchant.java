package su.nightexpress.excellentenchants.api.enchantment.type;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public interface PassiveEnchant {

    boolean use(@NotNull LivingEntity entity, int level);
}
