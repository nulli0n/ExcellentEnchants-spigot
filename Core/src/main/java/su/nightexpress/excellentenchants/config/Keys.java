package su.nightexpress.excellentenchants.config;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;

public class Keys {

    public static NamespacedKey itemRecharged;
    public static NamespacedKey keyLevel;
    public static NamespacedKey entitySpawnReason;
    public static NamespacedKey blockEnchant;

    public static void loadKeys(@NotNull EnchantsPlugin plugin) {
        itemRecharged = new NamespacedKey(plugin, "item.recharged");
        keyLevel = new NamespacedKey(plugin, "list_display_level");
        entitySpawnReason = new NamespacedKey(plugin, "entity.spawn_reason");
        blockEnchant = new NamespacedKey(plugin, "block.enchant");
    }

    public static void clear() {
        itemRecharged = null;
        keyLevel = null;
        entitySpawnReason = null;
        blockEnchant = null;
    }
}
