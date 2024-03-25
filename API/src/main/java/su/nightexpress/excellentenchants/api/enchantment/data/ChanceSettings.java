package su.nightexpress.excellentenchants.api.enchantment.data;

public interface ChanceSettings {

    double getTriggerChance(int level);

    boolean checkTriggerChance(int level);
}
