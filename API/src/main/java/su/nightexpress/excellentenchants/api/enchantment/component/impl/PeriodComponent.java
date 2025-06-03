package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

public class PeriodComponent implements EnchantComponent<Period> {

    @Override
    @NotNull
    public String getName() {
        return "periodic";
    }

    @Override
    @NotNull
    public Period read(@NotNull FileConfig config, @NotNull Period defaultValue) {
        long interval = ConfigValue.create("Period.Tick_Interval",
            defaultValue.getInterval(),
            "Only triggers if the age (ticks lived) of the entity is divisible by the given number.",
            "Should be greater than and divisible by the global 'Tick_Interval' in the main plugin config.",
            "[20 ticks = 1 second]"
        ).read(config);

        return new Period(interval);
    }
}
