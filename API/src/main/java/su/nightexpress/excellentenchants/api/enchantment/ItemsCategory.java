package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.Tag;
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
    private final String                  localized;

    public ItemsCategory(@NotNull Supplier<Set<Material>> supplier, EquipmentSlot[] slots, @Nullable String localized) {
        this.supplier = supplier;
        this.slots = slots;
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

    public String getLocalized() {
        return localized;
    }

    public static class Builder {

        private Supplier<Set<Material>> supplier;
        private EquipmentSlot[] slots;
        private String localized;

        public Builder() {
            this.supplier = HashSet::new;
            this.slots = new EquipmentSlot[0];
        }

        @NotNull
        public ItemsCategory build() {
            return new ItemsCategory(this.supplier, this.slots, this.localized);
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
