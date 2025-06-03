package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.util.*;
import java.util.function.Function;

public class EnchantHolder<T extends CustomEnchantment> {

    private final Function<CustomEnchantment, T> accepter;
    private final Function<T, EnchantPriority>   priority;
    private final Map<String, T>                 enchants;

    public EnchantHolder(@NotNull Function<CustomEnchantment, T> accepter, @NotNull Function<T, EnchantPriority> priority) {
        this.accepter = accepter;
        this.priority = priority;
        this.enchants = new LinkedHashMap<>();
    }

    @NotNull
    public static <T extends CustomEnchantment> EnchantHolder<T> create(@NotNull Class<T> clazz, @NotNull Function<T, EnchantPriority> priority) {
        return new EnchantHolder<>(enchantment -> clazz.isAssignableFrom(enchantment.getClass()) ? clazz.cast(enchantment) : null, priority);
    }

    public void clear() {
        this.enchants.clear();
    }

    public boolean isEmpty() {
        return this.enchants.isEmpty();
    }

    public boolean accept(@NotNull CustomEnchantment enchantment) {
        T enchant = this.accepter.apply(enchantment);
        if (enchant == null) return false;

        this.enchants.put(enchant.getId(), enchant);
        return true;
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
        return this.enchants.get(id.toLowerCase());
    }

    public boolean contains(@NotNull CustomEnchantment enchantment) {
        return this.enchants.containsKey(enchantment.getId());
    }
}
