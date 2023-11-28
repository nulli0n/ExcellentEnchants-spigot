package su.nightexpress.excellentenchants.enchantment.type;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.excellentenchants.config.Config;

public enum FitItemType {

    HELMET, CHESTPLATE, LEGGINGS, BOOTS, ELYTRA,
    WEAPON, TOOL, ARMOR, UNIVERSAL,
    SWORD, TRIDENT, AXE, BOW, CROSSBOW,
    HOE, PICKAXE, SHOVEL, FISHING_ROD;

    @Nullable
    public static FitItemType getByEnchantmentTarget(@NotNull EnchantmentTarget target) {
        return switch (target) {
            case ARMOR -> ARMOR;
            case ARMOR_FEET -> BOOTS;
            case ARMOR_LEGS -> LEGGINGS;
            case ARMOR_TORSO -> CHESTPLATE;
            case ARMOR_HEAD -> HELMET;
            case WEAPON -> WEAPON;
            case TOOL -> TOOL;
            case BOW -> BOW;
            case FISHING_ROD -> FISHING_ROD;
            case TRIDENT -> TRIDENT;
            case CROSSBOW -> CROSSBOW;
            case BREAKABLE, WEARABLE -> UNIVERSAL;
            default -> null;
        };
    }

    public boolean isIncluded(@NotNull ItemStack item) {
        return switch (this) {
            case UNIVERSAL -> ARMOR.isIncluded(item) || WEAPON.isIncluded(item) || TOOL.isIncluded(item) || BOW.isIncluded(item) || FISHING_ROD.isIncluded(item) || ELYTRA.isIncluded(item);
            case HELMET -> ItemUtil.isHelmet(item);
            case CHESTPLATE -> ItemUtil.isChestplate(item) || (Config.ENCHANTMENTS_ITEM_CHESTPLATE_ENCHANTS_TO_ELYTRA.get() && ELYTRA.isIncluded(item));
            case LEGGINGS -> ItemUtil.isLeggings(item);
            case BOOTS -> ItemUtil.isBoots(item);
            case ELYTRA -> item.getType() == Material.ELYTRA;
            case WEAPON -> SWORD.isIncluded(item) || ItemUtil.isTrident(item);
            case TOOL -> ItemUtil.isTool(item);
            case ARMOR -> ItemUtil.isArmor(item);
            case SWORD -> ItemUtil.isSword(item) || (Config.ENCHANTMENTS_ITEM_SWORD_ENCHANTS_TO_AXES.get() && AXE.isIncluded(item));
            case TRIDENT -> ItemUtil.isTrident(item);
            case AXE -> ItemUtil.isAxe(item);
            case BOW -> item.getType() == Material.BOW || (Config.ENCHANTMENTS_ITEM_BOW_ENCHANTS_TO_CROSSBOW.get() && CROSSBOW.isIncluded(item));
            case CROSSBOW -> item.getType() == Material.CROSSBOW;
            case HOE -> ItemUtil.isHoe(item);
            case PICKAXE -> ItemUtil.isPickaxe(item);
            case SHOVEL -> ItemUtil.isShovel(item);
            case FISHING_ROD -> item.getType() == Material.FISHING_ROD;
        };
    }
}
