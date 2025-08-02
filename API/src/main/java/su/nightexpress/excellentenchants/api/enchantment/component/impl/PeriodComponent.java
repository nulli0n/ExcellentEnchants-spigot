package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.TimeUtil;

public class PeriodComponent implements EnchantComponent<Period> {

    @Override
    @NotNull
    public String getName() {
        return "periodic";
    }

    @Override
    @NotNull
    public Period read(@NotNull FileConfig config, @NotNull Period defaultValue) {
        if (config.contains("Period.Tick_Interval")) {
            long oldValue = config.getLong("Period.Tick_Interval");
            config.set("Period.Interval", (int) Math.max(1, TimeUtil.ticksToSeconds(oldValue)));
            config.remove("Period.Tick_Interval");
        }

        long interval = ConfigValue.create("Period.Interval",
            defaultValue.getInterval(),
            "Only triggers if the age (seconds lived) of the entity is divisible by the given number.",
            "Should be greater than and divisible by the global 'Tick_Interval' in the main plugin config."
        ).read(config);

        return new Period(interval);
    }
}
