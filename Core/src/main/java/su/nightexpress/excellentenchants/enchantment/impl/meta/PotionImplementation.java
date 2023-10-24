package su.nightexpress.excellentenchants.enchantment.impl.meta;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

public final class PotionImplementation implements Potioned {

    private final ExcellentEnchant enchant;
    private final PotionEffectType effectType;
    private final Scaler  duration;
    private final Scaler  amplifier;
    private final boolean isPermanent;

    private PotionImplementation(@NotNull ExcellentEnchant enchant,
                                 @NotNull PotionEffectType effectType, boolean isPermanent,
                                 @NotNull EnchantScaler duration, @NotNull EnchantScaler amplifier) {
        this.enchant = enchant;
        this.effectType = effectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.isPermanent = isPermanent;
    }

    @Override
    @NotNull
    public Potioned getPotionImplementation() {
        return this;
    }

    public static PotionImplementation create(@NotNull ExcellentEnchant enchant, @NotNull PotionEffectType type, boolean isPermanent) {
        return create(enchant, type, isPermanent, "5 * " + Placeholders.ENCHANTMENT_LEVEL, Placeholders.ENCHANTMENT_LEVEL);
    }

    public static PotionImplementation create(@NotNull ExcellentEnchant enchant,
                                              @NotNull PotionEffectType type, boolean isPermanent,
                                              @NotNull String duration, @NotNull String amplifier) {

        EnchantScaler durationScale = EnchantScaler.read(enchant, "Settings.Potion_Effect.Duration", duration,
            "Potion effect duration (in seconds). This setting is useless for 'permanent' effects.");

        EnchantScaler amplifierScale = EnchantScaler.read(enchant, "Settings.Potion_Effect.Level", amplifier,
            "Potion effect level.");

        return new PotionImplementation(enchant, type, isPermanent, durationScale, amplifierScale);
    }

    @Override
    public boolean isPermanent() {
        return this.isPermanent;
    }

    @NotNull
    public PotionEffectType getEffectType() {
        return this.effectType;
    }

    public int getEffectDuration(int level) {
        if (this.isPermanent()) {
            int duration = Config.TASKS_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get().intValue() + 30;
            if (this.getEffectType().getName().equalsIgnoreCase(PotionEffectType.NIGHT_VISION.getName())) {
                duration += 30 * 20;
            }
            return duration;
        }
        return (int) (this.duration.getValue(level) * 20);
    }

    public int getEffectAmplifier(int level) {
        return (int) this.amplifier.getValue(level);
    }

    @NotNull
    public PotionEffect createEffect(int level) {
        int duration = this.getEffectDuration(level);
        int amplifier = Math.max(0, this.getEffectAmplifier(level) - 1);

        return new PotionEffect(this.getEffectType(), duration, amplifier, false, this.enchant.hasVisualEffects());
    }

    public boolean addEffect(@NotNull LivingEntity target, int level) {
        target.addPotionEffect(this.createEffect(level));
        return true;
    }
}
