package su.nightexpress.excellentenchants.api.item;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.EnchantFiles;
import su.nightexpress.excellentenchants.bridge.ItemTagLookup;
import su.nightexpress.nightcore.bridge.registry.NightRegistry;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LowerCase;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ItemSetRegistry {

    private static final NightRegistry<String, ItemSet> REGISTRY = new NightRegistry<>();

    private final ItemTagLookup tagLookup;
    private final Path file;

    private ItemSetRegistry(@NotNull Path dataDir, @NotNull ItemTagLookup tagLookup) {
        this.tagLookup = tagLookup;
        this.file = Path.of(dataDir.toString(), EnchantFiles.FILE_ITEM_TYPES);
    }

    public static void initialize(@NotNull Path dataDir, @NotNull ItemTagLookup tagLookup) {
        if (REGISTRY.isFrozen()) throw new IllegalStateException("Registry is already initialized");

        ItemSetRegistry registry = new ItemSetRegistry(dataDir, tagLookup);
        registry.load();
        REGISTRY.freeze();
    }

    public static void clear() {
        REGISTRY.unfreeze();
        REGISTRY.clear();
    }

    @Nullable
    public static ItemSet getByKey(@NotNull String id) {
        return REGISTRY.byKey(LowerCase.INTERNAL.apply(id));
    }

    @NotNull
    public static Set<ItemSet> getValues() {
        return REGISTRY.values();
    }

    @NotNull
    public static Map<String, ItemSet> getMap() {
        return REGISTRY.map();
    }

    private void load() {
        FileConfig config = FileConfig.load(this.file);

        if (config.getSection("Categories").isEmpty()) {
            this.loadDefaults().forEach((id, itemSet) -> config.set("Categories." + id, itemSet));
        }

        config.getSection("Categories").forEach(sId -> {
            ItemSet category = ItemSet.read(config, "Categories." + sId);
            REGISTRY.register(LowerCase.INTERNAL.apply(sId), category);
        });

        config.saveChanges();
    }

    @NotNull
    private Map<String, ItemSet> loadDefaults() {
        Map<String, ItemSet> map = new LinkedHashMap<>();

        map.put(ItemSetId.HELMET, ItemSet.buildByName(this.tagLookup.getHelmets()).slots(EquipmentSlot.HEAD).name("Helmet").build());
        map.put(ItemSetId.CHESTPLATE, ItemSet.buildByName(this.tagLookup.getChestplates()).slots(EquipmentSlot.CHEST).name("Chestplate").build());
        map.put(ItemSetId.LEGGINGS, ItemSet.buildByName(this.tagLookup.getLeggings()).slots(EquipmentSlot.LEGS).name("Leggings").build());
        map.put(ItemSetId.BOOTS, ItemSet.buildByName(this.tagLookup.getBoots()).slots(EquipmentSlot.FEET).name("Boots").build());
        map.put(ItemSetId.ELYTRA, ItemSet.buildByType(Material.ELYTRA).slots(EquipmentSlot.CHEST).name("Elytra").build());
        map.put(ItemSetId.SWORD, ItemSet.buildByName(this.tagLookup.getSwords()).slots(EquipmentSlot.HAND).name("Sword").build());
        map.put(ItemSetId.AXE, ItemSet.buildByName(this.tagLookup.getAxes()).slots(EquipmentSlot.HAND).name("Axe").build());
        map.put(ItemSetId.HOE, ItemSet.buildByName(this.tagLookup.getHoes()).slots(EquipmentSlot.HAND).name("Hoe").build());
        map.put(ItemSetId.PICKAXE, ItemSet.buildByName(this.tagLookup.getPickaxes()).slots(EquipmentSlot.HAND).name("Pickaxe").build());
        map.put(ItemSetId.SHOVEL, ItemSet.buildByName(this.tagLookup.getShovels()).slots(EquipmentSlot.HAND).name("Shovel").build());
        map.put(ItemSetId.TRIDENT, ItemSet.buildByType(Material.TRIDENT).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Trident").build());
        map.put(ItemSetId.BOW, ItemSet.buildByType(Material.BOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Bow").build());
        map.put(ItemSetId.CROSSBOW, ItemSet.buildByType(Material.CROSSBOW).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Crossbow").build());
        map.put(ItemSetId.FISHING_ROD, ItemSet.buildByType(Material.FISHING_ROD).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Fishing Rod").build());
        map.put(ItemSetId.SHIELD, ItemSet.buildByType(Material.SHIELD).slots(EquipmentSlot.OFF_HAND).name("Shield").build());
        map.put(ItemSetId.BREAKABLE, ItemSet.buildByName(this.tagLookup.getBreakable()).slots(EquipmentSlot.values()).name("Brekable").build());
        map.put(ItemSetId.ARMOR, ItemSet.buildByName(this.getArmors()).slots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET).name("Armor").build());
        map.put(ItemSetId.TOOL, ItemSet.buildByName(getTools()).slots(EquipmentSlot.HAND).name("Tool").build());
        map.put(ItemSetId.SWORDS_AXES, ItemSet.buildByName(this.getSwordsAxes()).slots(EquipmentSlot.HAND).name("Weapon").build());
        map.put(ItemSetId.BOW_CROSSBOW, ItemSet.buildByType(Lists.newSet(Material.BOW, Material.CROSSBOW)).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Bow / Crossbow").build());
        map.put(ItemSetId.CHESTPLATE_ELYTRA, ItemSet.buildByName(this.getTorso()).slots(EquipmentSlot.CHEST).name("Chestplate / Elytra").build());
        map.put(ItemSetId.ALL_WEAPON, ItemSet.buildByName(this.getAllRangeWeapon()).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Weapon").build());
        map.put(ItemSetId.MINING_TOOLS, ItemSet.buildByName(this.getMiningTools()).slots(EquipmentSlot.HAND).name("Mining Tool").build());
        map.put(ItemSetId.TOOLS_WEAPONS, ItemSet.buildByName(this.getToolsAndWeapon()).slots(EquipmentSlot.HAND, EquipmentSlot.OFF_HAND).name("Tools & Weapons").build());

        return map;
    }

    @NotNull
    private Set<String> getArmors() {
        Set<String> materials = new HashSet<>();
        materials.addAll(this.tagLookup.getHelmets());
        materials.addAll(this.tagLookup.getChestplates());
        materials.addAll(this.tagLookup.getLeggings());
        materials.addAll(this.tagLookup.getBoots());
        return materials;
    }

    @NotNull
    private Set<String> getTools() {
        Set<String> materials = new HashSet<>();
        materials.addAll(this.tagLookup.getAxes());
        materials.addAll(this.tagLookup.getHoes());
        materials.addAll(this.tagLookup.getPickaxes());
        materials.addAll(this.tagLookup.getShovels());
        materials.add(Material.SHEARS.name());
        return materials;
    }

    @NotNull
    private Set<String> getMiningTools() {
        Set<String> materials = new HashSet<>();
        materials.addAll(this.tagLookup.getAxes());
        materials.addAll(this.tagLookup.getPickaxes());
        materials.addAll(this.tagLookup.getShovels());
        return materials;
    }

    @NotNull
    private Set<String> getToolsAndWeapon() {
        Set<String> materials = new HashSet<>();
        materials.addAll(getTools());
        materials.addAll(getAllRangeWeapon());
        return materials;
    }

    @NotNull
    private Set<String> getSwordsAxes() {
        Set<String> materials = new HashSet<>();
        materials.addAll(this.tagLookup.getAxes());
        materials.addAll(this.tagLookup.getSwords());
        return materials;
    }

    @NotNull
    private Set<String> getAllRangeWeapon() {
        Set<String> materials = new HashSet<>();
        materials.addAll(this.tagLookup.getAxes());
        materials.addAll(this.tagLookup.getSwords());
        materials.add(Material.BOW.name());
        materials.add(Material.CROSSBOW.name());
        materials.add(Material.TRIDENT.name());
        materials.add(Material.MACE.name());
        return materials;
    }

    @NotNull
    private Set<String> getTorso() {
        Set<String> materials = new HashSet<>(this.tagLookup.getChestplates());
        materials.add(Material.ELYTRA.name());
        return materials;
    }
}
