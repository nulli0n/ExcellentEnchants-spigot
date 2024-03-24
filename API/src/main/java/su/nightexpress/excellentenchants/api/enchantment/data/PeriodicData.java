package su.nightexpress.excellentenchants.api.enchantment.data;

import org.jetbrains.annotations.NotNull;

public interface PeriodicData {

    @NotNull PeriodicSettings getPeriodSettings();

    default long getInterval() {
        return this.getPeriodSettings().getInterval();
    }

    default long getNextTriggerTime() {
        return this.getPeriodSettings().getNextTriggerTime();
    }

    default boolean isTriggerTime() {
        return this.getPeriodSettings().isTriggerTime();
    }

    default void updateTriggerTime() {
        this.getPeriodSettings().updateTriggerTime();
    }
}
