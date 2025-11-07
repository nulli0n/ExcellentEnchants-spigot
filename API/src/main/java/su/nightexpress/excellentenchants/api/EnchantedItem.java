package su.nightexpress.excellentenchants.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.util.Map;

public class EnchantedItem<T extends CustomEnchantment> {

    private final ItemStack       itemStack;
    private final Map<T, Integer> enchants;

    public EnchantedItem(@NotNull ItemStack itemStack, @NotNull Map<T, Integer> enchants) {
        this.itemStack = itemStack;
        this.enchants = enchants;
    }

    @NotNull
    public ItemStack getItemStack() {
        return this.itemStack;
    }

    @NotNull
    public Map<T, Integer> getEnchants() {
        return this.enchants;
    }
}
