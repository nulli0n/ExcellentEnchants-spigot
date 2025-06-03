package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;

public class PotionEffects {

    private final PotionEffectType type;
    private final Modifier         duration;
    private final Modifier         amplifier;
    private final boolean          permanent;

    public PotionEffects(@NotNull PotionEffectType type, @NotNull Modifier duration, @NotNull Modifier amplifier, boolean permanent) {
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
        this.permanent = permanent;
    }

    @NotNull
    public static Modifier permanentDuration(@NotNull PotionEffectType type) {
        int duration = type == PotionEffectType.NIGHT_VISION ? 45 : 10;
        return Modifier.addictive(duration).capacity(duration).build();
    }

    @NotNull
    public static PotionEffects permanent(@NotNull PotionEffectType type) {
        Modifier amplifier = Modifier.addictive(0).perLevel(1).capacity(5).build();

        return permanent(type, amplifier);
    }

    @NotNull
    public static PotionEffects permanent(@NotNull PotionEffectType type, @NotNull Modifier.Builder amplifier) {
        return permanent(type, amplifier.build());
    }

    @NotNull
    public static PotionEffects permanent(@NotNull PotionEffectType type, @NotNull Modifier amplifier) {
        return new PotionEffects(type, permanentDuration(type), amplifier, true);
    }

//    @NotNull
//    public static PotionEffects temporal(@NotNull PotionEffectType type) {
//        Modifier duration = Modifier.multiplier(5).perLevel(1).capacity(60).build();
//        Modifier amplifier = Modifier.addictive(0).perLevel(1).capacity(5).build();
//
//        return temporal(type, duration, amplifier);
//    }

    @NotNull
    public static PotionEffects temporal(@NotNull PotionEffectType type, @NotNull Modifier.Builder duration) {
        return temporal(type, duration, Modifier.addictive(0).perLevel(1).capacity(5));
    }

    @NotNull
    public static PotionEffects temporal(@NotNull PotionEffectType type, @NotNull Modifier.Builder duration, @NotNull Modifier.Builder amplifier) {
        return temporal(type, duration.build(), amplifier.build());
    }

    @NotNull
    public static PotionEffects temporal(@NotNull PotionEffectType type, @NotNull Modifier duration, @NotNull Modifier amplifier) {
        return new PotionEffects(type, duration, amplifier, false);
    }

    public Modifier getDuration() {
        return this.duration;
    }

    public Modifier getAmplifier() {
        return this.amplifier;
    }

    public boolean isPermanent() {
        return this.permanent;
    }

    @NotNull
    public PotionEffectType getType() {
        return this.type;
    }

    public int getDuration(int level) {
        return (int) (this.duration.getValue(level) * 20);
    }

    public int getAmplifier(int level) {
        return (int) this.amplifier.getValue(level);
    }

    @NotNull
    public PotionEffect createEffect(int level, boolean particles) {
        int duration = this.getDuration(level);
        int amplifier = Math.max(0, this.getAmplifier(level) - 1);

        return new PotionEffect(this.type, duration, amplifier, this.permanent, particles);
    }

    public boolean addEffect(@NotNull LivingEntity target, int level, boolean particles) {
        return target.addPotionEffect(this.createEffect(level, particles));
    }

    public boolean addEffect(@NotNull Arrow arrow, int level, boolean particles) {
        return arrow.addCustomEffect(this.createEffect(level, particles), true);
    }
}
