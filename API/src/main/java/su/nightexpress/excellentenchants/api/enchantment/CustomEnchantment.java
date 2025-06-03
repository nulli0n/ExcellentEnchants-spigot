package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.List;
import java.util.function.UnaryOperator;

public interface CustomEnchantment {

    boolean load();

    void onRegister(@NotNull Enchantment enchantment);

    <T> boolean hasComponent(@NotNull EnchantComponent<T> type);

    @NotNull <T> T getComponent(@NotNull EnchantComponent<T> type);

    @NotNull UnaryOperator<String> replacePlaceholders(int level);

    @NotNull FileConfig getConfig();

    @NotNull Enchantment getBukkitEnchantment();

    @NotNull EnchantDefinition getDefinition();

    @NotNull EnchantDistribution getDistribution();

    @NotNull Charges getCharges();

    boolean testTriggerChance(int level);

    boolean isTriggerTime(@NotNull LivingEntity entity);

    boolean isAvailableToUse(@NotNull LivingEntity entity);

    boolean isAvailableToUse(@NotNull World world);

    @NotNull String getId();

    @NotNull String getDisplayName();

    @NotNull List<String> getDescription();

    @NotNull List<String> getDescription(int level);

    @NotNull List<String> getDescription(int level, int charges);

    @NotNull ItemSet getPrimaryItems();

    @NotNull ItemSet getSupportedItems();

    boolean isCurse();

    boolean isHiddenFromList();

    boolean hasVisualEffects();

    boolean isChargeable();

    boolean isChargesFuel(@NotNull ItemStack item);

    @NotNull ItemStack getFuel();

    int getCharges(@NotNull ItemStack item);

    int getCharges(@NotNull ItemMeta meta);

    int getMaxCharges(int level);

    void setCharges(@NotNull ItemStack item, int level, int amount);

    boolean isFullOfCharges(@NotNull ItemStack item);

    boolean isOutOfCharges(@NotNull ItemStack item);

    void restoreCharges(@NotNull ItemStack item, int level);

    void fuelCharges(@NotNull ItemStack item, int level);

    void consumeCharges(@NotNull ItemStack item, int level);
}
