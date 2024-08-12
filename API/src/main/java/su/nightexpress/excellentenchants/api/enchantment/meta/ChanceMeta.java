package su.nightexpress.excellentenchants.api.enchantment.meta;

public interface ChanceMeta extends MetaHolder {

    default double getTriggerChance(int level) {
        return this.getMeta().getProbability().getTriggerChance(level);
    }

    default boolean checkTriggerChance(int level) {
        return this.getMeta().getProbability().checkTriggerChance(level);
    }
}
