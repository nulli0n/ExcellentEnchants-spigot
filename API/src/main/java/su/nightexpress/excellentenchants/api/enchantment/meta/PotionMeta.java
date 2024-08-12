package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public interface PotionMeta extends MetaHolder {

    default boolean isPermanent() {
        return this.getMeta().getPotionEffects().isPermanent();
    }

    default PotionEffectType getEffectType() {
        return this.getMeta().getPotionEffects().getEffectType();
    }

    default int getEffectAmplifier(int level) {
        return this.getMeta().getPotionEffects().getEffectAmplifier(level);
    }

    default int getEffectDuration(int level) {
        return this.getMeta().getPotionEffects().getEffectDuration(level);
    }

    default PotionEffect createEffect(int level) {
        return this.getMeta().getPotionEffects().createEffect(level);
    }

    default boolean addEffect(@NotNull LivingEntity target, int level) {
        return this.getMeta().getPotionEffects().addEffect(target, level);
    }
}
