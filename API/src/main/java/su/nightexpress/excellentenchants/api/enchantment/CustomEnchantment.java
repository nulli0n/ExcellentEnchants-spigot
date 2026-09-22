package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

@NullMarked
public interface CustomEnchantment {

    void load();

    <T> boolean hasComponent(EnchantComponent<T> type);

    <T> T getComponent(EnchantComponent<T> type);

    UnaryOperator<String> replacePlaceholders(int level);

    Enchantment getBukkitEnchantment();

    EnchantDefinition getDefinition();

    EnchantDistribution getDistribution();

    Charges getCharges();

    boolean testTriggerChance(int level);

    boolean isTriggerTime(LivingEntity entity);

    String getId();

    NamespacedKey getKey();

    String getDisplayName();

    List<String> getDescription();

    List<String> getDescription(int level);

    default String getDisplayName(Locale locale) {
        return this.getDefinition().getDisplayName(locale);
    }

    default List<String> getDescription(Locale locale) {
        return this.getDefinition().getDescription(locale);
    }

    default List<String> getDescription(int level, Locale locale) {
        List<String> description = new ArrayList<>(this.getDescription(locale));
        description.replaceAll(this.replacePlaceholders(level, locale));
        return description;
    }

    UnaryOperator<String> replacePlaceholders(int level, Locale locale);

    ItemSet getPrimaryItems();

    ItemSet getSupportedItems();

    boolean isCurse();

    boolean isHiddenFromList();

    boolean hasVisualEffects();

    boolean isChargeable();

    boolean isChargesFuel(ItemStack item);

    ItemStack getFuel();

    int getCharges(ItemStack item);

    int getCharges(ItemMeta meta);

    int getMaxCharges(int level);

    void setCharges(ItemStack item, int level, int amount);

    boolean isFullOfCharges(ItemStack item);

    boolean isOutOfCharges(ItemStack item);

    void restoreCharges(ItemStack item, int level);

    void fuelCharges(ItemStack item, int level);

    void consumeCharges(ItemStack item, int level);
}
