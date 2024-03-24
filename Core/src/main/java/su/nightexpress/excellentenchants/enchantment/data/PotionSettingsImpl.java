package su.nightexpress.excellentenchants.enchantment.data;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionSettings;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.nightcore.config.FileConfig;

public class PotionSettingsImpl implements PotionSettings {

    private final PotionEffectType effectType;
    private final Modifier         duration;
    private final Modifier         amplifier;
    private final boolean          isPermanent;
    private final boolean particles;

    private PotionSettingsImpl(@NotNull PotionEffectType effectType,
                               boolean isPermanent,
                               boolean particles,
                               @NotNull Modifier duration,
                               @NotNull Modifier amplifier) {
        this.effectType = effectType;
        this.duration = duration;
        this.amplifier = amplifier;
        this.isPermanent = isPermanent;
        this.particles = particles;
    }

    @NotNull
    public static PotionSettingsImpl create(@NotNull EnchantmentData data, @NotNull FileConfig config, @NotNull PotionEffectType type, boolean isPermanent) {
        return create(data, config, type, isPermanent,
            Modifier.multiply(5, 1, 1),
            Modifier.add(0, 1, 1, 5)
        );
    }

    @NotNull
    public static PotionSettingsImpl create(@NotNull EnchantmentData data,
                                            @NotNull FileConfig config,
                                            @NotNull PotionEffectType type,
                                            boolean isPermanent,
                                            @NotNull Modifier duration,
                                            @NotNull Modifier amplifier) {

        Modifier durationScale = Modifier.read(config, "Settings.Potion_Effect.Duration", duration,
            "Potion effect duration (in seconds). This setting is useless for 'permanent' effects.");

        Modifier amplifierScale = Modifier.read(config, "Settings.Potion_Effect.Level", amplifier,
            "Potion effect level.");

        return new PotionSettingsImpl(type, isPermanent, data.hasVisualEffects(), durationScale, amplifierScale);
    }

    @Override
    public boolean isPermanent() {
        return this.isPermanent;
    }

    @Override
    @NotNull
    public PotionEffectType getEffectType() {
        return this.effectType;
    }

    @Override
    public int getEffectDuration(int level) {
        if (this.isPermanent()) {
            int duration = Config.CORE_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get().intValue() * 2;
            if (this.getEffectType().getKey().equals(PotionEffectType.NIGHT_VISION.getKey()) && duration < 600) {
                duration += 30 * 20;
            }
            return duration;
        }
        return (int) (this.duration.getValue(level) * 20);
    }

    @Override
    public int getEffectAmplifier(int level) {
        return (int) this.amplifier.getValue(level);
    }

    @Override
    @NotNull
    public PotionEffect createEffect(int level) {
        int duration = this.getEffectDuration(level);
        int amplifier = Math.max(0, this.getEffectAmplifier(level) - 1);

        return new PotionEffect(this.getEffectType(), duration, amplifier, false, this.particles);
    }

    @Override
    public boolean addEffect(@NotNull LivingEntity target, int level) {
        target.addPotionEffect(this.createEffect(level));
        return true;
    }
}
