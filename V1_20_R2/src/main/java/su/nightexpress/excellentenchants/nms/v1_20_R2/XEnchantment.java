package su.nightexpress.excellentenchants.nms.v1_20_R2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;

public class XEnchantment extends Enchantment {

    private final IEnchantment enchantment;

    public XEnchantment(@NotNull IEnchantment enchantment, @NotNull Rarity rarity) {
        super(rarity, nmsCategory(enchantment), nmsSlots(enchantment));
        this.enchantment = enchantment;
    }

    @Override
    public int getMinLevel() {
        return this.enchantment.getStartLevel();
    }

    public int getMaxLevel() {
        return this.enchantment.getMaxLevel();
    }

    public int getMinCost(int level) {
        return 1 + level * 10; // TODO
    }

    public int getMaxCost(int level) {
        return this.getMinCost(level) + 5; // TODO
    }

    protected boolean checkCompatibility(Enchantment other) {
        ResourceLocation location = BuiltInRegistries.ENCHANTMENT.getKey(other);
        if (location == null) return false;

        NamespacedKey key = CraftNamespacedKey.fromMinecraft(location);
        String id = key.getKey();

        return !this.enchantment.getConflicts().contains(id);

        //return !this.enchantment.conflictsWith(bukkit);
    }

    /*public Component getFullname(int level) {
        return this.enchantment.getNameFormatted(level);

        MutableComponent var1 = Component.translatable(this.getDescriptionId());
        if (this.isCurse()) {
            var1.withStyle(ChatFormatting.RED);
        } else {
            var1.withStyle(ChatFormatting.GRAY);
        }

        if (level != 1 || this.getMaxLevel() != 1) {
            var1.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + level));
        }

        return var1;
    }*/

    public boolean canEnchant(ItemStack item) {
        org.bukkit.inventory.ItemStack bukkitStack = CraftItemStack.asBukkitCopy(item);
        if (this.enchantment.checkEnchantCategory(bukkitStack)) return true;
        if (this.enchantment.checkItemCategory(bukkitStack)) return true;

        return super.canEnchant(item);
    }

    public boolean isTreasureOnly() {
        return this.enchantment.isTreasure();
    }

    public boolean isCurse() {
        return this.enchantment.isCurse();
    }

    public boolean isTradeable() {
        return this.enchantment.isTradeable();
    }

    public boolean isDiscoverable() {
        return this.enchantment.isDiscoverable();
    }

    @NotNull
    public static EnchantmentCategory nmsCategory(@NotNull IEnchantment enchantment) {
        return switch (enchantment.getCategory()) {
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
            default -> throw new IllegalStateException("Unexpected value: " + enchantment.getCategory());
        };
    }

    public static EquipmentSlot[] nmsSlots(@NotNull IEnchantment enchantment) {
        org.bukkit.inventory.EquipmentSlot[] slots = enchantment.getSlots();
        EquipmentSlot[] nmsSlots = new EquipmentSlot[slots.length];

        for (int index = 0; index < nmsSlots.length; index++) {
            org.bukkit.inventory.EquipmentSlot bukkitSlot = slots[index];
            nmsSlots[index] = CraftEquipmentSlot.getNMS(bukkitSlot);
        }

        return nmsSlots;
    }
}
