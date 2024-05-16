package su.nightexpress.excellentenchants.nms.v1_20_R1;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantingBridge;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.distribution.VanillaOptions;

public class CustomEnchantment extends Enchantment {

    private final EnchantmentData enchantmentData;

    private VanillaOptions vanillaOptions;

    public CustomEnchantment(@NotNull EnchantmentData enchantmentData) {
        super(nmsRarity(enchantmentData), nmsCategory(enchantmentData), nmsSlots(enchantmentData));
        this.enchantmentData = enchantmentData;

        if (enchantmentData.getDistributionOptions() instanceof VanillaOptions options) {
            this.vanillaOptions = options;
        }
    }

    @Override
    public int getMaxLevel() {
        return this.enchantmentData.getMaxLevel();
    }

    @Override
    public int getMinCost(int level) {
        return this.enchantmentData.getMinCost(level);
    }

    @Override
    public int getMaxCost(int level) {
        return this.enchantmentData.getMaxCost(level);
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
    public static EnchantmentCategory nmsCategory(@NotNull EnchantmentData data) {
        return switch (data.getCategory()) {
            case WEAPON -> EnchantmentCategory.WEAPON;
            case TOOL -> EnchantmentCategory.DIGGER;
            case ARMOR -> EnchantmentCategory.ARMOR;
            case BOW -> EnchantmentCategory.BOW;
            case TRIDENT -> EnchantmentCategory.TRIDENT;
            case CROSSBOW -> EnchantmentCategory.CROSSBOW;
            case WEARABLE -> EnchantmentCategory.WEARABLE;
            case BREAKABLE -> EnchantmentCategory.BREAKABLE;
            case ARMOR_FEET -> EnchantmentCategory.ARMOR_FEET;
            case ARMOR_HEAD -> EnchantmentCategory.ARMOR_HEAD;
            case ARMOR_LEGS -> EnchantmentCategory.ARMOR_LEGS;
            case ARMOR_TORSO -> EnchantmentCategory.ARMOR_CHEST;
            case VANISHABLE -> EnchantmentCategory.VANISHABLE;
            case FISHING_ROD -> EnchantmentCategory.FISHING_ROD;
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
