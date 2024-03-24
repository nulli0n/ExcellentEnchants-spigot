package su.nightexpress.excellentenchants.api.enchantment.data;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface PotionData {

    @NotNull PotionSettings getPotionSettings();

    default boolean isPermanent() {
        return this.getPotionSettings().isPermanent();
    }

    default PotionEffectType getEffectType() {
        return this.getPotionSettings().getEffectType();
    }

    default int getEffectAmplifier(int level) {
        return this.getPotionSettings().getEffectAmplifier(level);
    }

    default int getEffectDuration(int level) {
        return this.getPotionSettings().getEffectDuration(level);
    }

    default PotionEffect createEffect(int level) {
        return this.getPotionSettings().createEffect(level);
    }

    default boolean addEffect(@NotNull LivingEntity target, int level) {
        return this.getPotionSettings().addEffect(target, level);
    }
}
