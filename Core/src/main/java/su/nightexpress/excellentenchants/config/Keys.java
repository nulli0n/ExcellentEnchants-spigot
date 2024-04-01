package su.nightexpress.excellentenchants.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;

public class Keys {

    public static NamespacedKey loreSize;

    public static NamespacedKey itemRecharged;

    public static NamespacedKey entitySpawnReason;

    public static void loadKeys(@NotNull EnchantsPlugin plugin) {
        loreSize = new NamespacedKey(plugin, "lore_size");
        itemRecharged = new NamespacedKey(plugin, "item.recharged");
        entitySpawnReason = new NamespacedKey(plugin, "entity.spawn_reason");
    }
}
