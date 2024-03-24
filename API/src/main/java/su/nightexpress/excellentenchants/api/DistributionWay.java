package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;

public enum DistributionWay {

    ENCHANTING("Enchanting_Table"),
    VILLAGER("Villagers"),
    LOOT_GENERATION("Loot_Generation"),
    FISHING("Fishing"),
    MOB_EQUIPMENT("Mob_Equipment");

    private final String pathName;

    DistributionWay(@NotNull String pathName) {
        this.pathName = pathName;
    }

    @NotNull
    public String getPathName() {
        return pathName;
    }
}
