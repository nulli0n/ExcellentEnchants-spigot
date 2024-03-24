package su.nightexpress.excellentenchants.api.enchantment.data;

import org.jetbrains.annotations.NotNull;

public interface ChanceData {

    @NotNull ChanceSettings getChanceSettings();

    /*@NotNull
    default UnaryOperator<String> replacePlaceholders(int level) {
        return this.getChanceImplementation().replacePlaceholders(level);
    }*/

    default double getTriggerChance(int level) {
        return this.getChanceSettings().getTriggerChance(level);
    }

    default boolean checkTriggerChance(int level) {
        return getChanceSettings().checkTriggerChance(level);
    }
}
