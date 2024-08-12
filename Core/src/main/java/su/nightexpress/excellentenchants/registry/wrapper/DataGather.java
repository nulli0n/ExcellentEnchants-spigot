package su.nightexpress.excellentenchants.registry.wrapper;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;

import java.util.Map;

public abstract class DataGather<E extends Event, T extends CustomEnchantment> {

    @Nullable
    public abstract LivingEntity getEntity(@NotNull E event);

    @NotNull
    public abstract EquipmentSlot[] getEnchantSlots(@NotNull E event);

    public abstract boolean checkPriority(@NotNull T enchant, @NotNull EventPriority priority);

    @NotNull
    public Map<ItemStack, Map<T, Integer>> getEnchants(@NotNull E event, @NotNull Class<T> enchantClass, @NotNull LivingEntity entity) {
        return EnchantUtils.getEquipped(entity, enchantClass, this.getEnchantSlots(event));
    }

    public abstract boolean useEnchant(@NotNull E event, @NotNull LivingEntity entity, @NotNull ItemStack item, @NotNull T enchant, int level);
}
