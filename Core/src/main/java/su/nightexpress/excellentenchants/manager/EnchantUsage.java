package su.nightexpress.excellentenchants.manager;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

public interface EnchantUsage<T extends CustomEnchantment> {

    boolean useEnchant(@NotNull ItemStack item, @NotNull T enchant, int level);
}
