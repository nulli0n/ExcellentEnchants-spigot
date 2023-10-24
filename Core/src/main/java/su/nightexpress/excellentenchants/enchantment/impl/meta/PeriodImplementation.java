package su.nightexpress.excellentenchants.enchantment.impl.meta;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.meta.Periodic;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

public class PeriodImplementation implements Periodic {

    private final EnchantScaler triggerInterval;

    private long nextTriggerTime;

    public PeriodImplementation(@NotNull ExcellentEnchant enchant, @NotNull EnchantScaler triggerInterval) {
        this.triggerInterval = triggerInterval;
    }

    @NotNull
    public static PeriodImplementation create(@NotNull ExcellentEnchant enchant) {
        return create(enchant, "100");
    }

    @NotNull
    public static PeriodImplementation create(@NotNull ExcellentEnchant enchant, @NotNull String def) {
        return new PeriodImplementation(enchant, EnchantScaler.read(enchant, "Settings.Trigger_Interval", def,
            "Sets how often (in ticks) this enchantment will be triggered.",
            "20 ticks = 1 second."));
    }

    @NotNull
    @Override
    public Periodic getPeriodImplementation() {
        return this;
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
