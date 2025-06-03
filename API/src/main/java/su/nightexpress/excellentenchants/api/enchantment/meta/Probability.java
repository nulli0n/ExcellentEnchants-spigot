package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.nightcore.util.random.Rnd;

public class Probability {

    public static final double CAP = 100D;

    private final Modifier triggerChance;

    public Probability(@NotNull Modifier triggerChance) {
        this.triggerChance = triggerChance;
    }

    @NotNull
    public static Probability oneHundred() {
        return addictive(100, 0);
    }

    @NotNull
    public static Probability addictive(double base, double perLevel) {
        return new Probability(Modifier.addictive(base).perLevel(perLevel).capacity(CAP).build());
    }

    @NotNull
    public static Probability multiplier(double base, double perLevel) {
        return new Probability(Modifier.multiplier(base).perLevel(perLevel).capacity(CAP).build());
    }

    @NotNull
    public Modifier getTriggerChance() {
        return this.triggerChance;
    }

    public double getTriggerChance(int level) {
        return this.triggerChance.getValue(level);
    }

    public boolean checkTriggerChance(int level) {
        return Rnd.chance(this.getTriggerChance(level));
    }
}
