package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.random.Rnd;

public class Probability {

    private final Modifier triggerChance;

    private Probability(@NotNull Modifier triggerChance) {
        this.triggerChance = triggerChance;
    }

    @NotNull
    public static Probability create(@NotNull FileConfig config) {
        return create(config, Modifier.add(100, 0, 1, 100));
    }

    @NotNull
    public static Probability create(@NotNull FileConfig config, @NotNull Modifier def) {
        Modifier chanceMod = Modifier.read(config, "Settings.Trigger_Chance",
            def,
            "A chance that this enchantment will be triggered."
        );

        return new Probability(chanceMod);
    }

    public double getTriggerChance(int level) {
        return this.triggerChance.getValue(level);
    }

    public boolean checkTriggerChance(int level) {
        return Rnd.chance(this.getTriggerChance(level));
    }
}
