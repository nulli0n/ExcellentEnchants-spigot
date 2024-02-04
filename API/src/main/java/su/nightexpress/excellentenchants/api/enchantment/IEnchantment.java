package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Keyed;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;

import java.util.List;
import java.util.Set;

public interface IEnchantment extends Keyed {

    boolean isAvailableToUse(@NotNull LivingEntity entity);

    @NotNull JYML getConfig();

    @NotNull String getId();

    @NotNull String getDisplayName();

    @NotNull String getNameFormatted(int level);

    @NotNull String getNameFormatted(int level, int charges);

    @NotNull List<String> getDescription();

    @NotNull List<String> getDescription(int level);

    @NotNull Set<String> getConflicts();

    @NotNull ITier getTier();

    @NotNull EnchantmentTarget getCategory();

    ItemCategory[] getFitItemTypes();

    int getMaxLevel();

    int getStartLevel();

    int getLevelByEnchantCost(int expLevel);

    double getObtainChance(@NotNull ObtainType obtainType);

    int getObtainLevelMin(@NotNull ObtainType obtainType);

    int getObtainLevelMax(@NotNull ObtainType obtainType);

    int generateLevel(@NotNull ObtainType obtainType);

    int getAnvilMergeCost(int level);

    //@Deprecated
    //boolean conflictsWith(@NotNull Enchantment enchantment);

    boolean checkEnchantCategory(@NotNull ItemStack item);

    boolean checkItemCategory(@NotNull ItemStack item);

    boolean isCurse();

    boolean isTreasure();

    boolean isTradeable();

    boolean isDiscoverable();

    boolean isChargesEnabled();

    int getChargesMax(int level);

    int getChargesConsumeAmount(int level);

    int getChargesRechargeAmount(int level);

    @NotNull ItemStack getChargesFuel();

    boolean isChargesFuel(@NotNull ItemStack item);

    int getCharges(@NotNull ItemStack item);

    boolean isFullOfCharges(@NotNull ItemStack item);

    boolean isOutOfCharges(@NotNull ItemStack item);

    void consumeCharges(@NotNull ItemStack item, int level);

    void consumeChargesNoUpdate(@NotNull ItemStack item, int level);

    EquipmentSlot[] getSlots();
}
