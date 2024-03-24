package su.nightexpress.excellentenchants.enchantment.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.data.PeriodicSettings;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.nightcore.config.FileConfig;

public class PeriodSettingsImpl implements PeriodicSettings {

    private final Modifier triggerInterval;

    private long nextTriggerTime;

    public PeriodSettingsImpl(@NotNull Modifier triggerInterval) {
        this.triggerInterval = triggerInterval;
        this.updateTriggerTime();
    }

    @NotNull
    public static PeriodSettingsImpl create(@NotNull FileConfig config) {
        long baseTick = Config.CORE_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get();

        return create(config, Modifier.add(baseTick, 0, 1));
    }

    @NotNull
    public static PeriodSettingsImpl create(@NotNull FileConfig config, @NotNull Modifier def) {
        Modifier intervalMod = Modifier.read(config, "Settings.Trigger_Interval", def,
            "Sets how often (in ticks) this enchantment will be triggered.",
            "20 ticks = 1 second.");

        return new PeriodSettingsImpl(intervalMod);
    }

    @Override
    public long getInterval() {
        return (long) this.triggerInterval.getValue(1);
    }

    @Override
    public long getNextTriggerTime() {
        return nextTriggerTime;
    }

    @Override
    public boolean isTriggerTime() {
        return System.currentTimeMillis() >= this.getNextTriggerTime();
    }

    @Override
    public void updateTriggerTime() {
        this.nextTriggerTime = System.currentTimeMillis() + this.getInterval() * 50L - 100L;
    }
}
