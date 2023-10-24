package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;

public interface Periodic {

    @NotNull Periodic getPeriodImplementation();

    default long getInterval(int level) {
        return this.getPeriodImplementation().getInterval(level);
    }

    default long getNextTriggerTime() {
        return this.getPeriodImplementation().getNextTriggerTime();
    }

    default boolean isTriggerTime() {
        return this.getPeriodImplementation().isTriggerTime();
    }

    default void updateTriggerTime(int level) {
        this.getPeriodImplementation().updateTriggerTime(level);
    }
}
