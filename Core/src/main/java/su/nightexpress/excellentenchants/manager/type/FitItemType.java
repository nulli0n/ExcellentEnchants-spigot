package su.nightexpress.excellentenchants.manager.type;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.ItemUtil;
import su.nightexpress.excellentenchants.config.Config;

public enum FitItemType {

    HELMET, CHESTPLATE, LEGGINGS, BOOTS, ELYTRA,
    WEAPON, TOOL, ARMOR,
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
            default -> null;
        };
    }

    public boolean isIncluded(@NotNull ItemStack item) {
        return switch (this) {
            case HELMET -> ItemUtil.isHelmet(item);
            case CHESTPLATE -> ItemUtil.isChestplate(item) || (Config.ENCHANTMENTS_ITEM_ELYTRA_AS_CHESTPLATE && ELYTRA.isIncluded(item));
            case LEGGINGS -> ItemUtil.isLeggings(item);
            case BOOTS -> ItemUtil.isBoots(item);
            case ELYTRA -> item.getType() == Material.ELYTRA;
            case WEAPON -> ItemUtil.isWeapon(item);
            case TOOL -> ItemUtil.isTool(item);
            case ARMOR -> ItemUtil.isArmor(item);
            case SWORD -> ItemUtil.isSword(item) || (Config.ENCHANTMENTS_ITEM_AXES_AS_SWORDS && AXE.isIncluded(item));
            case TRIDENT -> ItemUtil.isTrident(item);
            case AXE -> ItemUtil.isAxe(item);
            case BOW -> item.getType() == Material.BOW || (Config.ENCHANTMENTS_ITEM_CROSSBOWS_AS_BOWS && CROSSBOW.isIncluded(item));
            case CROSSBOW -> item.getType() == Material.CROSSBOW;
            case HOE -> ItemUtil.isHoe(item);
            case PICKAXE -> ItemUtil.isPickaxe(item);
            case SHOVEL -> ItemUtil.isShovel(item);
            case FISHING_ROD -> item.getType() == Material.FISHING_ROD;
        };
    }
}
