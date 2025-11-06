package su.nightexpress.excellentenchants.bridge.paper;

import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.tag.PostFlattenTagRegistrar;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.bridge.ItemTagLookup;

import java.util.Set;
import java.util.stream.Collectors;

public class PaperItemTagLookup implements ItemTagLookup {

    private final PostFlattenTagRegistrar<ItemType> registrar;

    public PaperItemTagLookup(@NotNull PostFlattenTagRegistrar<ItemType> registrar) {
        this.registrar = registrar;
    }

    @Override
    @NotNull
    public Set<String> getBreakable() {
        return this.fromRegistry(ItemTypeTagKeys.ENCHANTABLE_DURABILITY);
    }

    @Override
    @NotNull
    public Set<String> getHelmets() {
        return this.fromRegistry(ItemTypeTagKeys.HEAD_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getChestplates() {
        return this.fromRegistry(ItemTypeTagKeys.CHEST_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getLeggings() {
        return this.fromRegistry(ItemTypeTagKeys.LEG_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getBoots() {
        return this.fromRegistry(ItemTypeTagKeys.FOOT_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getSwords() {
        return this.fromRegistry(ItemTypeTagKeys.SWORDS);
    }

    @Override
    @NotNull
    public Set<String> getAxes() {
        return this.fromRegistry(ItemTypeTagKeys.AXES);
    }

    @Override
    @NotNull
    public Set<String> getHoes() {
        return this.fromRegistry(ItemTypeTagKeys.HOES);
    }

    @Override
    @NotNull
    public Set<String> getPickaxes() {
        return this.fromRegistry(ItemTypeTagKeys.PICKAXES);
    }

    @Override
    @NotNull
    public Set<String> getShovels() {
        return this.fromRegistry(ItemTypeTagKeys.SHOVELS);
    }

    @NotNull
    private Set<String> fromRegistry(@NotNull TagKey<ItemType> key) {
        return this.registrar.getTag(key).stream().map(typedKey -> typedKey.key().value()).collect(Collectors.toSet());
    }
}
