package su.nightexpress.excellentenchants.api.item;

import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Enums;
import su.nightexpress.nightcore.util.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ItemSet implements Writeable {

    private final Set<String>     materials;
    private final EquipmentSlot[] slots;
    private final String          displayName;

    public ItemSet(@NotNull Set<String> materials, EquipmentSlot[] slots, @NotNull String displayName) {
        this.materials = Lists.modify(materials, String::toLowerCase);
        this.slots = slots;
        this.displayName = displayName;
    }

    @NotNull
    public static ItemSet read(@NotNull FileConfig config, @NotNull String path) {
        String name = config.getString(path + ".Name", "null");
        List<EquipmentSlot> slots = Lists.modify(config.getStringList(path + ".Slots"), raw -> Enums.get(raw, EquipmentSlot.class));
        slots.removeIf(Objects::isNull);

        Set<String> itemNames = config.getStringSet(path + ".Items");

        return new ItemSet(itemNames, slots.toArray(new EquipmentSlot[0]), name);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Name", this.displayName);
        config.set(path + ".Slots", Stream.of(this.slots).map(Enum::name).toList());
        config.set(path + ".Items", this.materials);
    }

//    public boolean is(@NotNull ItemStack itemStack) {
//        return this.materials.contains(BukkitThing.toString(itemStack.getType()));
//    }

    @NotNull
    public static Builder buildByType(@NotNull Set<Material> materials) {
        return builder().materials(materials);
    }

    @NotNull
    public static Builder buildByName(@NotNull Set<String> materials) {
        return builder().materialNames(materials);
    }

    @NotNull
    public static Builder buildByType(Material... materials) {
        return builder().materials(Lists.newSet(materials));
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    @NotNull
    public Set<String> getMaterials() {
        return this.materials;
    }

    public EquipmentSlot[] getSlots() {
        return this.slots;
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    public static class Builder {

        private String          name;
        private Set<String>     materials;
        private EquipmentSlot[] slots;

        public Builder() {
            this.name = "null";
            this.materials = new HashSet<>();
            this.slots = new EquipmentSlot[0];
        }

        @NotNull
        public ItemSet build() {
            return new ItemSet(this.materials, this.slots, this.name);
        }

        @NotNull
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        @NotNull
        public Builder materials(@NotNull Set<Material> materials) {
            return this.materialNames(materials.stream().map(BukkitThing::getValue).collect(Collectors.toSet()));
        }

        @NotNull
        public Builder materialNames(@NotNull Set<String> materials) {
            this.materials = materials;
            return this;
        }

        @NotNull
        public Builder slots(EquipmentSlot... slots) {
            this.slots = slots;
            return this;
        }
    }
}
