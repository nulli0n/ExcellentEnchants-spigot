package su.nightexpress.excellentenchants.api.item;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.bridge.PostFlatten;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemSetRegistry {

    private static final Map<String, ItemSet> BY_ID = new LinkedHashMap<>();

    public static void load(@NotNull File dataDir) {
        FileConfig config = new FileConfig(dataDir.getAbsolutePath(), ConfigBridge.ITEM_TYPES_FILE);

        if (config.getSection("Categories").isEmpty()) {
            loadDefaults();

            BY_ID.forEach((id, category) -> config.set("Categories." + id, category));
            BY_ID.clear();
        }

        config.getSection("Categories").forEach(sId -> {
            ItemSet category = ItemSet.read(config, "Categories." + sId);
            register(sId.toLowerCase(), category);
        });

        config.saveChanges();
    }

    private static void loadDefaults() {
        register(ItemSetId.HELMET, ItemSet.buildByName(getHelmets()).slots(EquipmentSlot.HEAD).name("Helmet"));
        register(ItemSetId.CHESTPLATE, ItemSet.buildByName(getChestplates()).slots(EquipmentSlot.CHEST).name("Chestplate"));
        register(ItemSetId.LEGGINGS, ItemSet.buildByName(getLeggings()).slots(EquipmentSlot.LEGS).name("Leggings"));
        register(ItemSetId.BOOTS, ItemSet.buildByName(getBoots()).slots(EquipmentSlot.FEET).name("Boots"));
        register(ItemSetId.ELYTRA, ItemSet.buildByType(Material.ELYTRA).slots(EquipmentSlot.CHEST).name("Elytra"));
        register(ItemSetId.SWORD, ItemSet.buildByName(getSwords()).slots(EquipmentSlot.HAND).name("Sword"));
        register(ItemSetId.AXE, ItemSet.buildByName(getAxes()).slots(EquipmentSlot.HAND).name("Axe"));
        register(ItemSetId.HOE, ItemSet.buildByName(getHoes()).slots(EquipmentSlot.HAND).name("Hoe"));
        register(ItemSetId.PICKAXE, ItemSet.buildByName(getPickaxes()).slots(EquipmentSlot.HAND).name("Pickaxe"));
        register(ItemSetId.SHOVEL, ItemSet.buildByName(getShovels()).slots(EquipmentSlot.HAND).name("Shovel"));
        register(ItemSetId.TRIDENT, ItemSet.buildByType(Material.TRIDENT).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Trident"));
        register(ItemSetId.BOW, ItemSet.buildByType(Material.BOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Bow"));
        register(ItemSetId.CROSSBOW, ItemSet.buildByType(Material.CROSSBOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Crossbow"));
        register(ItemSetId.FISHING_ROD, ItemSet.buildByType(Material.FISHING_ROD).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Fishing Rod"));
        register(ItemSetId.SHIELD, ItemSet.buildByType(Material.SHIELD).slots(EquipmentSlot.OFF_HAND).name("Shield"));
        register(ItemSetId.BREAKABLE, ItemSet.buildByName(getBreakable()).slots(EquipmentSlot.values()).name("Brekable"));
        register(ItemSetId.ARMOR, ItemSet.buildByName(getArmors()).slots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).name("Armor"));
        register(ItemSetId.TOOL, ItemSet.buildByName(getTools()).slots(EquipmentSlot.HAND).name("Tool"));
        register(ItemSetId.SWORDS_AXES, ItemSet.buildByName(getSwordsAxes()).slots(EquipmentSlot.HAND).name("Weapon"));
        register(ItemSetId.BOW_CROSSBOW, ItemSet.buildByType(Lists.newSet(Material.BOW, Material.CROSSBOW)).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Bow / Crossbow"));
        register(ItemSetId.CHESTPLATE_ELYTRA, ItemSet.buildByName(getTorso()).slots(EquipmentSlot.CHEST).name("Chestplate / Elytra"));
        register(ItemSetId.ALL_WEAPON, ItemSet.buildByName(getAllRangeWeapon()).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Weapon"));
        register(ItemSetId.MINING_TOOLS, ItemSet.buildByName(getMiningTools()).slots(EquipmentSlot.HAND).name("Mining Tool"));
        register(ItemSetId.TOOLS_WEAPONS, ItemSet.buildByName(getToolsAndWeapon()).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Tools & Weapons"));
    }

    public static void register(@NotNull String id, @NotNull ItemSet.Builder category) {
        register(id, category.build());
    }

    public static void register(@NotNull String id, @NotNull ItemSet category) {
        BY_ID.put(id, category);
    }

    @NotNull
    public static Map<String, ItemSet> getByIdMap() {
        return BY_ID;
    }

    @NotNull
    public Set<ItemSet> getAll() {
        return new HashSet<>(BY_ID.values());
    }

    @Nullable
    public static ItemSet getById(@NotNull String id) {
        return BY_ID.get(id.toLowerCase());
    }

    public static boolean isPresent(@NotNull String id) {
        return BY_ID.containsKey(id.toLowerCase());
    }

    @NotNull
    private static Set<String> getBreakable() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.ENCHANTABLE_DURABILITY) : fromTag(Tag.ITEMS_ENCHANTABLE_DURABILITY);
    }

//    @NotNull
//    private static Set<Material> getTraling(@NotNull String trail) {
//        return get(material -> material.name().toLowerCase().endsWith(trail));
//    }

    @NotNull
    private static Set<String> getHelmets() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.HEAD_ARMOR) : fromTag(Tag.ITEMS_HEAD_ARMOR);
    }

    @NotNull
    private static Set<String> getChestplates() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.CHEST_ARMOR) : fromTag(Tag.ITEMS_CHEST_ARMOR);
    }

    @NotNull
    private static Set<String> getLeggings() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.LEG_ARMOR) : fromTag(Tag.ITEMS_LEG_ARMOR);
    }

    @NotNull
    private static Set<String> getBoots() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.FOOT_ARMOR) : fromTag(Tag.ITEMS_FOOT_ARMOR);
    }

    @NotNull
    private static Set<String> getSwords() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.SWORDS) : fromTag(Tag.ITEMS_SWORDS);
    }

    @NotNull
    private static Set<String> getAxes() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.AXES) : fromTag(Tag.ITEMS_AXES);
    }

    @NotNull
    private static Set<String> getHoes() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.HOES) : fromTag(Tag.ITEMS_HOES);
    }

    @NotNull
    private static Set<String> getPickaxes() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.PICKAXES) : fromTag(Tag.ITEMS_PICKAXES);
    }

    @NotNull
    private static Set<String> getShovels() {
        return ConfigBridge.isPaper() ? fromRegistry(ItemTypeTagKeys.SHOVELS) : fromTag(Tag.ITEMS_SHOVELS);
    }

    @NotNull
    private static Set<String> getArmors() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getHelmets());
        materials.addAll(getChestplates());
        materials.addAll(getLeggings());
        materials.addAll(getBoots());
        return materials;
    }

    @NotNull
    private static Set<String> getTools() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getAxes());
        materials.addAll(getHoes());
        materials.addAll(getPickaxes());
        materials.addAll(getShovels());
        materials.add(Material.SHEARS.name());
        return materials;
    }

    @NotNull
    private static Set<String> getMiningTools() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getAxes());
        materials.addAll(getPickaxes());
        materials.addAll(getShovels());
        return materials;
    }

    @NotNull
    private static Set<String> getToolsAndWeapon() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getTools());
        materials.addAll(getAllRangeWeapon());
        return materials;
    }

    @NotNull
    private static Set<String> getSwordsAxes() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getAxes());
        materials.addAll(getSwords());
        return materials;
    }

    @NotNull
    private static Set<String> getAllRangeWeapon() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getAxes());
        materials.addAll(getSwords());
        materials.add(Material.BOW.name());
        materials.add(Material.CROSSBOW.name());
        materials.add(Material.TRIDENT.name());
        materials.add(Material.MACE.name());
        return materials;
    }

    @NotNull
    private static Set<String> getTorso() {
        Set<String> materials = new HashSet<>(getChestplates());
        materials.add(Material.ELYTRA.name());
        return materials;
    }

//    @NotNull
//    private static Set<Material> get(@NotNull Predicate<Material> predicate) {
//        return Stream.of(Material.values()).filter(material -> material.isItem() && predicate.test(material)).collect(Collectors.toSet());
//    }

    @NotNull
    private static Set<String> fromRegistry(@NotNull TagKey<org.bukkit.inventory.ItemType> key) {
        return PostFlatten.registrar.getTag(key).stream().map(t -> t.key().value()).collect(Collectors.toSet());
    }

    @NotNull
    private static Set<String> fromTag(@NotNull Tag<Material> tag) {
        return tag.getValues().stream().map(Enum::name).collect(Collectors.toSet());
    }
}
