package su.nightexpress.excellentenchants.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class EnchantingBridge {

    private static ItemStack enchantingItem;

    public static ItemStack getEnchantingItem() {
        return enchantingItem;
    }

    public static void setEnchantingItem(@Nullable ItemStack itemStack) {
        enchantingItem = itemStack;
    }

    public static void clear() {
        enchantingItem = null;
    }
}
