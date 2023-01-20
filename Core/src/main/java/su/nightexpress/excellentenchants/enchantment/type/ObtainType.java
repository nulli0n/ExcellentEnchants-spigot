package su.nightexpress.excellentenchants.enchantment.type;

import org.jetbrains.annotations.NotNull;

public enum ObtainType {

    ENCHANTING("Enchanting_Table"),
    VILLAGER("Villagers"),
    LOOT_GENERATION("Loot_Generation"),
    FISHING("Fishing"),
    MOB_SPAWNING("Mob_Spawning");

    private final String pathName;

    ObtainType(@NotNull String pathName) {
        this.pathName = pathName;
    }

    @NotNull
    public String getPathName() {
        return pathName;
    }
}
