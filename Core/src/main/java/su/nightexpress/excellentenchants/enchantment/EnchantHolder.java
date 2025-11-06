package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantedItem;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.nightcore.util.LowerCase;

import java.util.*;
import java.util.function.Function;

public class EnchantHolder<T extends CustomEnchantment> {

    private final Class<T>                     type;
    private final Function<T, EnchantPriority> priority;
    private final boolean                      cacheable;
    private final Map<String, T>               enchants;

    private final Map<UUID, Map<EquipmentSlot, EnchantedItem<T>>> cachedEnchants;

    private EnchantHolder(@NotNull Class<T> type, @NotNull Function<T, EnchantPriority> priority, boolean cacheable) {
        this.type = type;
        this.priority = priority;
        this.cacheable = cacheable;

        this.enchants = new LinkedHashMap<>();
        this.cachedEnchants = new HashMap<>();
    }

    @NotNull
    public static <T extends CustomEnchantment> EnchantHolder<T> withNoCache(@NotNull Class<T> type, @NotNull Function<T, EnchantPriority> priority) {
        return new EnchantHolder<>(type, priority, false);
    }

    @NotNull
    public static <T extends CustomEnchantment> EnchantHolder<T> cached(@NotNull Class<T> type, @NotNull Function<T, EnchantPriority> priority) {
        return new EnchantHolder<>(type, priority, true);
    }

    public void clear() {
        this.enchants.clear();
    }

    public boolean isEmpty() {
        return this.enchants.isEmpty();
    }

    public boolean accept(@NotNull CustomEnchantment enchantment) {
        T enchant = this.adapt(enchantment);
        if (enchant == null) return false;

        this.enchants.put(enchant.getId(), enchant);
        return true;
    }

    @NotNull
    public Map<EquipmentSlot, EnchantedItem<T>> getCached(@NotNull LivingEntity entity) {
        return this.cachedEnchants.getOrDefault(entity.getUniqueId(), Collections.emptyMap());
    }

    @Nullable
    public EnchantedItem<T> getCached(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot) {
        return this.getCached(entity).get(slot);
    }

    public void updateCache(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot, @NotNull ItemStack itemStack, @NotNull Map<CustomEnchantment, Integer> allEnchants) {
        if (allEnchants.isEmpty()) {
            this.removeCache(entity, slot);
            return;
        }

        Map<T, Integer> adaptedEnchants = new HashMap<>();
        allEnchants.forEach((enchantment, level) -> {
            T adapted = this.getEnchant(enchantment.getId());
            if (adapted == null) return;

            //EquipmentSlot[] enchantSlots = adapted.getSupportedItems().getSlots();
            //if (!Lists.contains(enchantSlots, slot)) return;

            adaptedEnchants.put(adapted, level);
        });

        if (adaptedEnchants.isEmpty()) {
            this.removeCache(entity, slot);
            return;
        }

        this.cachedEnchants.computeIfAbsent(entity.getUniqueId(), k -> new HashMap<>()).put(slot, new EnchantedItem<>(itemStack, adaptedEnchants));
    }

    public void removeCache(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot) {
        this.getCached(entity).remove(slot);
    }

    public void clearCache(@NotNull LivingEntity entity) {
        this.cachedEnchants.remove(entity.getUniqueId());
    }

    public boolean isCacheable() {
        return this.cacheable;
    }

    @NotNull
    public EnchantPriority getPriority(@NotNull T enchant) {
        return this.priority.apply(enchant);
    }

    @NotNull
    public Set<T> getEnchants() {
        return new HashSet<>(this.enchants.values());
    }

    @Nullable
    public T getEnchant(@NotNull String id) {
        return this.enchants.get(LowerCase.INTERNAL.apply(id));
    }

    @Nullable
    private T adapt(@NotNull CustomEnchantment enchantment) {
        return this.type.isAssignableFrom(enchantment.getClass()) ? this.type.cast(enchantment) : null;
    }

    public boolean contains(@NotNull CustomEnchantment enchantment) {
        return this.enchants.containsKey(enchantment.getId());
    }
}
