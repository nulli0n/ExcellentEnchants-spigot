package su.nightexpress.excellentenchants.api.enchantment.data;

public interface PeriodicSettings {

    long getInterval();

    long getNextTriggerTime();

    boolean isTriggerTime();

    void updateTriggerTime();
}
