package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;

public interface Periodic {

    @NotNull Periodic getPeriodImplementation();

    default long getInterval() {
        return this.getPeriodImplementation().getInterval();
    }

    default long getNextTriggerTime() {
        return this.getPeriodImplementation().getNextTriggerTime();
    }

    default boolean isTriggerTime() {
        return this.getPeriodImplementation().isTriggerTime();
    }

    default void updateTriggerTime() {
        this.getPeriodImplementation().updateTriggerTime();
    }
}
