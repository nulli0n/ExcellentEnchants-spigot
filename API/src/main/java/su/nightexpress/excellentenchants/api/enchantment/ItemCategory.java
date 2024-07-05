package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.ItemUtil;

import java.util.function.Predicate;

@Deprecated
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
    SHIELD(itemStack -> itemStack.getType() == Material.SHIELD),

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

    public boolean isIncluded(@NotNull ItemStack item) {
        return this.getPredicate().test(item);
    }
}
