package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.meta.MetaHolder;
import su.nightexpress.nightcore.config.FileConfig;

import java.util.List;
import java.util.function.UnaryOperator;

public interface CustomEnchantment extends MetaHolder {

//    default void clear() {
//
//    }

    @NotNull UnaryOperator<String> replacePlaceholders(int level);

    @NotNull FileConfig getConfig();

    @NotNull Enchantment getBukkitEnchantment();

    @NotNull Definition getDefinition();

    @NotNull Distribution getDistribution();

    @NotNull Charges getCharges();

    boolean load();

    boolean checkServerRequirements();

    boolean isAvailableToUse(@NotNull LivingEntity entity);

    boolean isAvailableToUse(@NotNull World world);

    default boolean isSupportedItem(@NotNull ItemStack itemStack) {
        return this.getDefinition().getSupportedItems().is(itemStack);
    }

    default boolean isPrimaryItem(@NotNull ItemStack itemStack) {
        return this.getDefinition().getPrimaryItems().is(itemStack);
    }

    @NotNull String getId();

    @NotNull String getDisplayName();

    @NotNull String getFormattedName();

    @NotNull List<String> getDescription();

    @NotNull List<String> getDescription(int level);

    @NotNull List<String> getDescription(int level, int charges);

    boolean isCurse();

    boolean isHiddenFromList();

    boolean hasVisualEffects();

    boolean hasCharges();

    boolean isChargesFuel(@NotNull ItemStack item);

    int getCharges(@NotNull ItemStack item);

    int getCharges(@NotNull ItemMeta meta);

    void setCharges(@NotNull ItemStack item, int level, int amount);

    boolean isFullOfCharges(@NotNull ItemStack item);

    boolean isOutOfCharges(@NotNull ItemStack item);

    void restoreCharges(@NotNull ItemStack item, int level);

    void fuelCharges(@NotNull ItemStack item, int level);

    void consumeCharges(@NotNull ItemStack item, int level);
}
