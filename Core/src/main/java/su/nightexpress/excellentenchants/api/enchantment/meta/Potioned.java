package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface Potioned {

    @NotNull Potioned getPotionImplementation();

    default boolean isPermanent() {
        return this.getPotionImplementation().isPermanent();
    }

    default PotionEffectType getEffectType() {
        return this.getPotionImplementation().getEffectType();
    }

    default int getEffectAmplifier(int level) {
        return this.getPotionImplementation().getEffectAmplifier(level);
    }

    default int getEffectDuration(int level) {
        return this.getPotionImplementation().getEffectDuration(level);
    }

    default PotionEffect createEffect(int level) {
        return this.getPotionImplementation().createEffect(level);
    }

    default boolean addEffect(@NotNull LivingEntity target, int level) {
        return this.getPotionImplementation().addEffect(target, level);
    }
}
