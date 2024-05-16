package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.distribution.DistributionOptions;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;

import java.util.List;
import java.util.Set;

public interface EnchantmentData {

    default void clear() {

    }

    @NotNull PlaceholderMap getPlaceholders(int level);

    @NotNull FileConfig getConfig();

    boolean load();

    boolean checkServerRequirements();

    boolean isAvailableToUse(@NotNull LivingEntity entity);

    boolean isAvailableToUse(@NotNull World world);

    boolean checkEnchantLimit(@NotNull ItemStack item);

    boolean checkEnchantCategory(@NotNull ItemStack item);

    boolean checkItemCategory(@NotNull ItemStack item);

    default boolean hasItemCategory() {
        return this.getItemCategories().length != 0;
    }

    @NotNull DistributionOptions getDistributionOptions();

    @NotNull Rarity getRarity();

    void setRarity(@NotNull Rarity rarity);

    @NotNull String getId();

    @NotNull String getName();

    @NotNull
    default String getNameFormatted(int level) {
        return this.getNameFormatted(level, -1);
    }

    @NotNull String getNameFormatted(int level, int charges);

    @NotNull List<String> getDescription();

    @NotNull List<String> getDescriptionFormatted();

    @NotNull List<String> getDescriptionReplaced(int level);

    @NotNull EnchantmentTarget getCategory();

    @NotNull Enchantment getEnchantment();

    void setEnchantment(@NotNull Enchantment enchantment);

    ItemCategory[] getItemCategories();

    EquipmentSlot[] getSlots();

    default boolean hasConflicts() {
        return !this.getConflicts().isEmpty();
    }

    @NotNull Set<String> getConflicts();

    int getMaxLevel();

    int getMinCost(int level);

    int getMaxCost(int level);

    default boolean isCurse() {
        return false;
    }

    boolean isTreasure();

    boolean hasVisualEffects();

    boolean isChargesEnabled();

    boolean isChargesCustomFuel();

    int getChargesMax(int level);

    int getChargesConsumeAmount(int level);

    int getChargesRechargeAmount(int level);

    @NotNull ItemStack getChargesFuel();

    boolean isChargesFuel(@NotNull ItemStack item);

    default int getCharges(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : this.getCharges(meta);
    }

    int getCharges(@NotNull ItemMeta meta);

    void setCharges(@NotNull ItemStack item, int level, int amount);

    boolean isFullOfCharges(@NotNull ItemStack item);

    boolean isOutOfCharges(@NotNull ItemStack item);

    void restoreCharges(@NotNull ItemStack item, int level);

    void fuelCharges(@NotNull ItemStack item, int level);

    void consumeCharges(@NotNull ItemStack item, int level);

    void consumeChargesNoUpdate(@NotNull ItemStack item, int level);




    void setDisplayName(@NotNull String displayName);

    default void setDescription(@NotNull String... description) {
        this.setDescription(Lists.newList(description));
    }

    void setDescription(@NotNull List<String> description);

    boolean isHiddenFromList();

    void setHiddenFromList(boolean hiddenFromList);

    void setTreasure(boolean treasure);

    void setMaxLevel(int levelMax);

    @NotNull Cost getMinCost();

    void setMinCost(@NotNull Cost minCost);

    @NotNull Cost getMaxCost();

    void setMaxCost(@NotNull Cost maxCost);

    int getAnvilCost();

    void setAnvilCost(int anvilCost);

    default void setConflicts(@NotNull String... conflicts) {
        this.setConflicts(Lists.newSet(conflicts));
    }

    void setConflicts(@NotNull Set<String> conflicts);

    void setVisualEffects(boolean visualEffects);

    void setChargesEnabled(boolean chargesEnabled);

    void setChargesCustomFuel(boolean chargesCustomFuel);

    @NotNull Modifier getChargesMax();

    void setChargesMax(@NotNull Modifier chargesMax);

    void setChargesFuel(@Nullable ItemStack chargesFuel);

    @NotNull Modifier getChargesConsumeAmount();

    void setChargesConsumeAmount(@NotNull Modifier chargesConsumeAmount);

    @NotNull Modifier getChargesRechargeAmount();

    void setChargesRechargeAmount(@NotNull Modifier chargesRechargeAmount);
}
