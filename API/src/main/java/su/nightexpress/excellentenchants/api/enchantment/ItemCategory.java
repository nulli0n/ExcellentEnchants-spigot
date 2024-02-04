package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.ItemUtil;

import java.util.function.Predicate;

public enum ItemCategory {

    HELMET(ItemUtil::isHelmet),
    CHESTPLATE(ItemUtil::isChestplate),
    LEGGINGS(ItemUtil::isLeggings),
    BOOTS(ItemUtil::isBoots),
    ELYTRA(item -> item.getType() == Material.ELYTRA),

    SWORD(ItemUtil::isSword),
    TRIDENT(ItemUtil::isTrident),
    AXE(ItemUtil::isAxe),
    BOW(item -> item.getType() == Material.BOW),
    CROSSBOW(item -> item.getType() == Material.CROSSBOW),
    HOE(ItemUtil::isHoe),
    PICKAXE(ItemUtil::isPickaxe),
    SHOVEL(ItemUtil::isShovel),
    FISHING_ROD(ItemUtil::isFishingRod),

    //@Deprecated WEAPON(item -> SWORD.isIncluded(item) || TRIDENT.isIncluded(item)),
    TOOL(ItemUtil::isTool),
    //@Deprecated ARMOR(ItemUtil::isArmor),
    //UNIVERSAL(item -> WEAPON.isIncluded(item) || TOOL.isIncluded(item) || ARMOR.isIncluded(item)),
    ;

    private Predicate<ItemStack> predicate;

    ItemCategory(@NotNull Predicate<ItemStack> predicate) {
        this.setPredicate(predicate);
    }

    @NotNull
    public Predicate<ItemStack> getPredicate() {
        return predicate;
    }

    public void setPredicate(@NotNull Predicate<ItemStack> predicate) {
        this.predicate = predicate;
    }

    @Deprecated
    public void patchPredicate(@NotNull Predicate<ItemStack> extra) {
        //this.setPredicate(item -> this.getPredicate().test(item) || (extra.test(item)));
    }

    /*public EquipmentSlot[] getSlots() {
        return switch (this) {
            case BOW, CROSSBOW, TRIDENT, FISHING_ROD, WEAPON, TOOL, HOE, PICKAXE, AXE, SWORD, SHOVEL ->
                new EquipmentSlot[]{EquipmentSlot.HAND};
            case HELMET -> new EquipmentSlot[]{EquipmentSlot.HEAD};
            case CHESTPLATE, ELYTRA -> new EquipmentSlot[]{EquipmentSlot.CHEST};
            case LEGGINGS -> new EquipmentSlot[]{EquipmentSlot.LEGS};
            case BOOTS -> new EquipmentSlot[]{EquipmentSlot.FEET};
            case ARMOR -> new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            case UNIVERSAL -> new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        };
    }

    @NotNull
    public EnchantmentTarget getEnchantmentTarget() {
        return switch (this) {
            case ARMOR -> EnchantmentTarget.ARMOR;
            case BOOTS -> EnchantmentTarget.ARMOR_FEET;
            case LEGGINGS -> EnchantmentTarget.ARMOR_LEGS;
            case CHESTPLATE, ELYTRA -> EnchantmentTarget.ARMOR_TORSO;
            case HELMET -> EnchantmentTarget.ARMOR_HEAD;
            case WEAPON, SWORD -> EnchantmentTarget.WEAPON;
            case TOOL, AXE, HOE, SHOVEL, PICKAXE -> EnchantmentTarget.TOOL;
            case BOW -> EnchantmentTarget.BOW;
            case FISHING_ROD -> EnchantmentTarget.FISHING_ROD;
            case TRIDENT -> EnchantmentTarget.TRIDENT;
            case CROSSBOW -> EnchantmentTarget.CROSSBOW;
            case UNIVERSAL -> EnchantmentTarget.WEARABLE;
        };
    }

    @NotNull
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
            default -> throw new IllegalStateException("Unexpected value: " + target);
        };
    }*/

    public boolean isIncluded(@NotNull ItemStack item) {
        return this.getPredicate().test(item);

        /*return switch (this) {
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
        };*/
    }
}
