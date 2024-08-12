package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.ConfigBridge;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

public class Period {

    private final long interval;

    private long waitTicks;

    public Period(long interval) {
        this.interval = interval;
        this.updateTriggerTime();
    }

    @NotNull
    public static Period create(@NotNull FileConfig config) {
        long baseTick = ConfigBridge.getEnchantsTickInterval();
        long interval = ConfigValue.create("Settings.Trigger-Interval",
            baseTick,
            "Sets how often (in ticks) this enchantment will be triggered.",
            "20 ticks = 1 second."
        ).read(config);

        return new Period(interval);
    }

    public long getInterval() {
        return this.interval;
    }

    public void consumeTicks() {
        this.waitTicks -= ConfigBridge.getEnchantsTickInterval();
    }

    public boolean isTriggerTime() {
        return this.waitTicks <= 0;
    }

    public void updateTriggerTime() {
        if (this.waitTicks <= 0) {
            this.waitTicks = this.getInterval();
        }
    }
}
