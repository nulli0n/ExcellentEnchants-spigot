package su.nightexpress.excellentenchants.util;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.ItemsCategory;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.nightcore.util.BukkitThing;

import java.util.HashSet;
import java.util.Set;

public class ItemCategories {

    public static final ItemsCategory HELMET = ItemsCategory.buildDirect(getItemsBySlot(EquipmentSlot.HEAD)).slots(EquipmentSlot.HEAD)
        .localized(Lang.ITEM_CATEGORY_HELMET)
        /*.target(EnchantmentTarget.ARMOR_HEAD)*/.build();

    public static final ItemsCategory CHESTPLATE = ItemsCategory.buildDirect(getItemsBySlot(EquipmentSlot.CHEST)).slots(EquipmentSlot.CHEST)
        .localized(Lang.ITEM_CATEGORY_CHESTPLATE)
        /*.target(EnchantmentTarget.ARMOR_TORSO)*/.build();

    public static final ItemsCategory LEGGINGS = ItemsCategory.buildDirect(getItemsBySlot(EquipmentSlot.LEGS)).slots(EquipmentSlot.LEGS)
        .localized(Lang.ITEM_CATEGORY_LEGGINGS)
        //.target(EnchantmentTarget.ARMOR_LEGS)
        .build();

    public static final ItemsCategory BOOTS = ItemsCategory.buildDirect(getItemsBySlot(EquipmentSlot.FEET)).slots(EquipmentSlot.FEET)
        .localized(Lang.ITEM_CATEGORY_BOOTS)
        //.target(EnchantmentTarget.ARMOR_FEET)
        .build();

    public static final ItemsCategory ELYTRA = ItemsCategory.buildDirect(Material.ELYTRA).slots(EquipmentSlot.CHEST)
        .localized(Lang.ITEM_CATEGORY_ELYTRA)
        //.target(EnchantmentTarget.ARMOR_TORSO)
        .build();

    public static final ItemsCategory SWORD = ItemsCategory.buildDirect(Tag.ITEMS_SWORDS).slots(EquipmentSlot.HAND)
        .localized(Lang.ITEM_CATEGORY_SWORD)
        //.target(EnchantmentTarget.WEAPON)
        .build();

    public static final ItemsCategory AXE = ItemsCategory.buildDirect(Tag.ITEMS_AXES).slots(EquipmentSlot.HAND)
        .localized(Lang.ITEM_CATEGORY_AXE)
        //.target(EnchantmentTarget.TOOL)
        .build();

    public static final ItemsCategory HOE = ItemsCategory.buildDirect(Tag.ITEMS_HOES).slots(EquipmentSlot.HAND)
        .localized(Lang.ITEM_CATEGORY_HOE)
        //.target(EnchantmentTarget.TOOL)
        .build();

    public static final ItemsCategory PICKAXE = ItemsCategory.buildDirect(Tag.ITEMS_PICKAXES).slots(EquipmentSlot.HAND)
        .localized(Lang.ITEM_CATEGORY_PICKAXE)
        //.target(EnchantmentTarget.TOOL)
        .build();

    public static final ItemsCategory SHOVEL = ItemsCategory.buildDirect(Tag.ITEMS_SHOVELS).slots(EquipmentSlot.HAND)
        .localized(Lang.ITEM_CATEGORY_SHOVEL)
        //.target(EnchantmentTarget.TOOL)
        .build();

    public static final ItemsCategory TRIDENT = ItemsCategory.buildDirect(Material.TRIDENT).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        .localized(Lang.ITEM_CATEGORY_TRIDENT)
        //.target(EnchantmentTarget.TRIDENT)
        .build();

    public static final ItemsCategory BOW = ItemsCategory.buildDirect(Material.BOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        .localized(Lang.ITEM_CATEGORY_BOW)
        //.target(EnchantmentTarget.BOW)
        .build();

    public static final ItemsCategory CROSSBOW = ItemsCategory.buildDirect(Material.CROSSBOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        .localized(Lang.ITEM_CATEGORY_CROSSBOW)
        //.target(EnchantmentTarget.CROSSBOW)
        .build();

    public static final ItemsCategory FISHING_ROD = ItemsCategory.buildDirect(Material.FISHING_ROD).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)
        .localized(Lang.ITEM_CATEGORY_FISHING_ROD)
        //.target(EnchantmentTarget.FISHING_ROD)
        .build();

    public static final ItemsCategory SHIELD = ItemsCategory.buildDirect(Material.SHIELD).slots(EquipmentSlot.OFF_HAND)
        .localized(Lang.ITEM_CATEGORY_SHIELD)
        //.target(EnchantmentTarget.BREAKABLE)
        .build();

    public static final ItemsCategory BREAKABLE = ItemsCategory.buildDirect(getItemsWithDurability()).slots(EquipmentSlot.values())
        .localized(Lang.ITEM_CATEGORY_BREAKABLE)
        //.target(EnchantmentTarget.BREAKABLE)
        .build();

    public static final ItemsCategory ARMOR = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>();
        materials.addAll(getItemsBySlot(EquipmentSlot.HEAD));
        materials.addAll(getItemsBySlot(EquipmentSlot.CHEST));
        materials.addAll(getItemsBySlot(EquipmentSlot.LEGS));
        materials.addAll(getItemsBySlot(EquipmentSlot.FEET));
        return materials;
    }).slots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)./*target(EnchantmentTarget.ARMOR).*/localized(Lang.ITEM_CATEGORY_ARMOR).build();

    public static final ItemsCategory TOOL = ItemsCategory.buildDirect(
        Tag.ITEMS_AXES,
        Tag.ITEMS_HOES,
        Tag.ITEMS_PICKAXES,
        Tag.ITEMS_SHOVELS
    ).slots(EquipmentSlot.HAND)/*.target(EnchantmentTarget.TOOL)*/.localized(Lang.ITEM_CATEGORY_TOOL).build();


    public static final ItemsCategory WEAPON = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>(SWORD.getMaterials());

        if (Config.CORE_SWORD_ENCHANTS_TO_AXES.get()) {
            materials.addAll(AXE.getMaterials());
        }

        return materials;
    }).slots(EquipmentSlot.HAND)./*target(EnchantmentTarget.WEAPON).*/localized(Lang.ITEM_CATEGORY_WEAPON).build();


    public static final ItemsCategory BOWS = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>(BOW.getMaterials());

        if (Config.CORE_BOW_ENCHANTS_TO_CROSSBOW.get()) {
            materials.addAll(CROSSBOW.getMaterials());
        }

        return materials;
    }).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)./*target(EnchantmentTarget.BOW).*/localized(Lang.ITEM_CATEGORY_BOWS).build();


    public static final ItemsCategory TORSO = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>(CHESTPLATE.getMaterials());

        if (Config.CORE_CHESTPLATE_ENCHANTS_TO_ELYTRA.get()) {
            materials.addAll(ELYTRA.getMaterials());
        }

        return materials;
    }).slots(EquipmentSlot.CHEST)./*target(EnchantmentTarget.ARMOR_TORSO).*/localized(Lang.ITEM_CATEGORY_TORSO).build();


    public static final ItemsCategory ALL_RANGE_WEAPON = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>();
        materials.addAll(WEAPON.getMaterials());
        materials.addAll(BOWS.getMaterials());
        materials.addAll(TRIDENT.getMaterials());
        materials.addAll(TOOL.getMaterials());
        return materials;
    }).slots(EquipmentSlot.HAND)./*target(EnchantmentTarget.BREAKABLE).*/localized(Lang.ITEM_CATEGORY_ALL_WEAPON).build();

    public static final ItemsCategory MINING_TOOLS = ItemsCategory.buildRef(() -> {
        Set<Material> materials = new HashSet<>();
        materials.addAll(AXE.getMaterials());
        materials.addAll(PICKAXE.getMaterials());
        materials.addAll(SHOVEL.getMaterials());
        return materials;
    }).slots(EquipmentSlot.HAND)./*target(EnchantmentTarget.TOOL).*/localized(Lang.ITEM_CATEGORY_MINING_TOOLS).build();


    @NotNull
    private static Set<Material> getItemsBySlot(@NotNull EquipmentSlot slot) {
        Set<Material> materials = new HashSet<>();
        BukkitThing.getMaterials().forEach(material -> {
            if (material.isItem() && material.getEquipmentSlot() == slot) {
                materials.add(material);
            }
        });
        return materials;
    }

    @NotNull
    private static Set<Material> getItemsWithDurability() {
        Set<Material> materials = new HashSet<>();
        BukkitThing.getMaterials().forEach(material -> {
            if (material.isItem() && material.getMaxDurability() > 0) {
                materials.add(material);
            }
        });
        return materials;
    }
}
