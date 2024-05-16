package su.nightexpress.excellentenchants;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R4.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R4.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantingBridge;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.distribution.VanillaOptions;

public class CustomEnchantment extends Enchantment {

    private final EnchantmentData enchantmentData;

    private VanillaOptions vanillaOptions;

    @NotNull
    public static CustomEnchantment from(@NotNull EnchantmentData enchantmentData) {
        TagKey<Item> category = nmsCategory(enchantmentData);
        int weight = enchantmentData.getRarity().getWeight();
        int maxLevel = enchantmentData.getMaxLevel();
        Cost minCost = new Cost(enchantmentData.getMinCost().base(), enchantmentData.getMinCost().perLevel());
        Cost maxCost = new Cost(enchantmentData.getMaxCost().base(), enchantmentData.getMaxCost().perLevel());
        int anvilCost = enchantmentData.getAnvilCost();
        EquipmentSlot[] slots = nmsSlots(enchantmentData);

        EnchantmentDefinition definition = Enchantment.definition(category, weight, maxLevel, minCost, maxCost, anvilCost, slots);
        return new CustomEnchantment(enchantmentData, definition);
    }

    public CustomEnchantment(@NotNull EnchantmentData enchantmentData, @NotNull EnchantmentDefinition definition) {
        super(definition);
        this.enchantmentData = enchantmentData;

        if (enchantmentData.getDistributionOptions() instanceof VanillaOptions options) {
            this.vanillaOptions = options;
        }
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        ResourceLocation location = BuiltInRegistries.ENCHANTMENT.getKey(other);
        if (location == null) return false;

        NamespacedKey key = CraftNamespacedKey.fromMinecraft(location);
        String id = key.getKey();

        // Expensive probably.
        //org.bukkit.enchantments.Enchantment bukkit = CraftEnchantment.minecraftToBukkit(other);
        //String id = bukkit.getKey().getKey();

        return !this.enchantmentData.getConflicts().contains(id);
    }

    @Override
    public boolean canEnchant(ItemStack item) {
        if (!super.canEnchant(item)) return false;

        return this.canEnchant(CraftItemStack.asBukkitCopy(item));
    }

    public boolean canEnchant(@NotNull org.bukkit.inventory.ItemStack bukkitItem) {
        if (!this.enchantmentData.checkEnchantLimit(bukkitItem)) return false;

        if (!this.enchantmentData.hasItemCategory()) {
            if (this.enchantmentData.checkEnchantCategory(bukkitItem)) return true;
        }

        return this.enchantmentData.checkItemCategory(bukkitItem);
    }

    @Override
    public boolean isTreasureOnly() {
        return this.enchantmentData.isTreasure() || this.isCurse();
    }

    @Override
    public boolean isCurse() {
        return this.enchantmentData.isCurse();
    }

    @Override
    public boolean isTradeable() {
        return this.vanillaOptions != null && this.vanillaOptions.isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        org.bukkit.inventory.ItemStack bukkitItem = EnchantingBridge.getEnchantingItem();
        if (bukkitItem != null && !this.canEnchant(bukkitItem)) {
            return false;
        }

        return this.vanillaOptions != null && this.vanillaOptions.isDiscoverable();
    }

    public static Rarity nmsRarity(@NotNull EnchantmentData data) {
        return switch (data.getRarity()) {
            case RARE -> Rarity.RARE;
            case COMMON -> Rarity.COMMON;
            case UNCOMMON -> Rarity.UNCOMMON;
            case VERY_RARE -> Rarity.VERY_RARE;
        };
    }

    @NotNull
    public static TagKey<Item> nmsCategory(@NotNull EnchantmentData data) {
        return switch (data.getCategory()) {
            case WEAPON -> ItemTags.WEAPON_ENCHANTABLE;
            case TOOL -> ItemTags.MINING_ENCHANTABLE;
            case ARMOR -> ItemTags.ARMOR_ENCHANTABLE;
            case BOW -> ItemTags.BOW_ENCHANTABLE;
            case TRIDENT -> ItemTags.TRIDENT_ENCHANTABLE;
            case CROSSBOW -> ItemTags.CROSSBOW_ENCHANTABLE;
            case WEARABLE -> ItemTags.EQUIPPABLE_ENCHANTABLE;
            case BREAKABLE -> ItemTags.DURABILITY_ENCHANTABLE;
            case ARMOR_FEET -> ItemTags.FOOT_ARMOR_ENCHANTABLE;
            case ARMOR_HEAD -> ItemTags.HEAD_ARMOR_ENCHANTABLE;
            case ARMOR_LEGS -> ItemTags.LEG_ARMOR_ENCHANTABLE;
            case ARMOR_TORSO -> ItemTags.CHEST_ARMOR_ENCHANTABLE;
            case VANISHABLE -> ItemTags.VANISHING_ENCHANTABLE;
            case FISHING_ROD -> ItemTags.FISHING_ENCHANTABLE;
            default -> throw new IllegalStateException("Unexpected value: " + data.getCategory());
        };
    }

    public static EquipmentSlot[] nmsSlots(@NotNull EnchantmentData data) {
        org.bukkit.inventory.EquipmentSlot[] slots = data.getSlots();
        EquipmentSlot[] nmsSlots = new EquipmentSlot[slots.length];

        for (int index = 0; index < nmsSlots.length; index++) {
            org.bukkit.inventory.EquipmentSlot bukkitSlot = slots[index];
            nmsSlots[index] = CraftEquipmentSlot.getNMS(bukkitSlot);
        }

        return nmsSlots;
    }
}
