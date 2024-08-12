package su.nightexpress.excellentenchants.api.enchantment.meta;

public interface PeriodMeta extends MetaHolder {

    default long getInterval() {
        return this.getMeta().getPeriod().getInterval();
    }

    default void consumeTicks() {
        this.getMeta().getPeriod().consumeTicks();
    }

    default boolean isTriggerTime() {
        return this.getMeta().getPeriod().isTriggerTime();
    }

    default void updateTriggerTime() {
        this.getMeta().getPeriod().updateTriggerTime();
    }
}
