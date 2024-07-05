package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.language.entry.LangString;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemsCategory {

    private final Supplier<Set<Material>> supplier;
    private final EquipmentSlot[]         slots;
    private final EnchantmentTarget       target;
    private final String                  localized;

    public ItemsCategory(@NotNull Supplier<Set<Material>> supplier, EquipmentSlot[] slots, @Nullable EnchantmentTarget target, @Nullable String localized) {
        this.supplier = supplier;
        this.slots = slots;
        this.target = target;
        this.localized = localized;
    }

    public boolean is(@NotNull ItemStack itemStack) {
        return this.getMaterials().contains(itemStack.getType());
    }

    @NotNull
    public static Builder buildDirect(@NotNull Set<Material> materials) {
        return buildRef(() -> materials);
    }

    @NotNull
    public static Builder buildDirect(Material... materials) {
        return buildDirect(Stream.of(materials).collect(Collectors.toSet()));
    }

    @SafeVarargs
    @NotNull
    public static Builder buildDirect(Tag<Material>... tags) {
        return buildDirect(Stream.of(tags).flatMap(tag -> tag.getValues().stream()).collect(Collectors.toSet()));
    }

    @NotNull
    public static Builder buildRef(@NotNull Supplier<Set<Material>> supplier) {
        return new Builder().supplier(supplier);
    }

    @NotNull
    public Set<Material> getMaterials() {
        return this.supplier.get();
    }

    public EquipmentSlot[] getSlots() {
        return slots;
    }

    /**
     * Only for compatibility reasons with versions < 1.21
     */
    public EnchantmentTarget getTarget() {
        return target;
    }

    public String getLocalized() {
        return localized;
    }

    /*@NotNull
    public static ItemsCategory create(@NotNull Set<Material> materials, EquipmentSlot... slots) {
        return new ItemsCategory(() -> materials);
    }

    @NotNull
    public static ItemsCategory create(Material... materials) {
        return create(Stream.of(materials).collect(Collectors.toSet()));
    }

    @SafeVarargs
    @NotNull
    public static ItemsCategory create(Tag<Material>... tags) {
        return create(Stream.of(tags).flatMap(tag -> tag.getValues().stream()).collect(Collectors.toSet()));
    }*/

//    @NotNull
//    @Deprecated
//    public static ItemsCategory fusion(ItemsCategory... categories) {
//        Set<Supplier<Set<Material>>> suppliers = Stream.of(categories).map(category -> category.supplier).collect(Collectors.toSet());
//        Supplier<Set<Material>> result = () -> suppliers.stream().flatMap(supplier -> supplier.get().stream()).collect(Collectors.toSet());
//
//        return new ItemsCategory(result, categories[0].getSlots(), categories[0].target, categories[0].localized);
//    }

    public static class Builder {

        private Supplier<Set<Material>> supplier;
        private EquipmentSlot[] slots;
        private EnchantmentTarget target;
        private String localized;

        public Builder() {
            this.supplier = HashSet::new;
            this.slots = new EquipmentSlot[0];
        }

        @NotNull
        public ItemsCategory build() {
            return new ItemsCategory(this.supplier, this.slots, this.target, this.localized);
        }

        @NotNull
        public Builder slots(EquipmentSlot... slots) {
            this.slots = slots;
            return this;
        }

        @NotNull
        public Builder supplier(@NotNull Supplier<Set<Material>> supplier) {
            this.supplier = supplier;
            return this;
        }

        @NotNull
        public Builder target(EnchantmentTarget target) {
            this.target = target;
            return this;
        }

        @NotNull
        public Builder localized(@NotNull LangString localized) {
            return this.localized(localized.getString());
        }

        @NotNull
        public Builder localized(@Nullable String localized) {
            this.localized = localized;
            return this;
        }
    }
}
