package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;

public interface Chanced {

    @NotNull Chanced getChanceImplementation();

    /*@NotNull
    default UnaryOperator<String> replacePlaceholders(int level) {
        return this.getChanceImplementation().replacePlaceholders(level);
    }*/

    default double getTriggerChance(int level) {
        return this.getChanceImplementation().getTriggerChance(level);
    }

    default boolean checkTriggerChance(int level) {
        return getChanceImplementation().checkTriggerChance(level);
    }
}
