package su.nightexpress.excellentenchants.api.enchantment.data;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface PotionSettings {

    boolean isPermanent();

    PotionEffectType getEffectType();

    int getEffectAmplifier(int level);

    int getEffectDuration(int level);

    PotionEffect createEffect(int level);

    boolean addEffect(@NotNull LivingEntity target, int level);
}
