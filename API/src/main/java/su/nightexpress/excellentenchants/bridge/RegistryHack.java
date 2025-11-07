package su.nightexpress.excellentenchants.bridge;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.IEnchantData;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSet;

public interface RegistryHack {

    void unfreezeRegistry();

    void freezeRegistry();

    void addExclusives(@NotNull CustomEnchantment enchantment);

    void createItemsSet(@NotNull String id, @NotNull ItemSet itemSet);

    @Nullable Enchantment registerEnchantment(@NotNull String id, @NotNull IEnchantData data, @NotNull EquipmentSlot[] slots);
}
