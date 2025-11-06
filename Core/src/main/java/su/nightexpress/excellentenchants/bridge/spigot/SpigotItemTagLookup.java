package su.nightexpress.excellentenchants.bridge.spigot;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.bridge.ItemTagLookup;

import java.util.Set;
import java.util.stream.Collectors;

public class SpigotItemTagLookup implements ItemTagLookup {

    @Override
    @NotNull
    public Set<String> getBreakable() {
        return fromTag(Tag.ITEMS_ENCHANTABLE_DURABILITY);
    }

    @Override
    @NotNull
    public Set<String> getHelmets() {
        return fromTag(Tag.ITEMS_HEAD_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getChestplates() {
        return fromTag(Tag.ITEMS_CHEST_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getLeggings() {
        return fromTag(Tag.ITEMS_LEG_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getBoots() {
        return fromTag(Tag.ITEMS_FOOT_ARMOR);
    }

    @Override
    @NotNull
    public Set<String> getSwords() {
        return fromTag(Tag.ITEMS_SWORDS);
    }

    @Override
    @NotNull
    public Set<String> getAxes() {
        return fromTag(Tag.ITEMS_AXES);
    }

    @Override
    @NotNull
    public Set<String> getHoes() {
        return fromTag(Tag.ITEMS_HOES);
    }

    @Override
    @NotNull
    public Set<String> getPickaxes() {
        return fromTag(Tag.ITEMS_PICKAXES);
    }

    @Override
    @NotNull
    public Set<String> getShovels() {
        return fromTag(Tag.ITEMS_SHOVELS);
    }

    @NotNull
    private Set<String> fromTag(@NotNull Tag<Material> tag) {
        return tag.getValues().stream().map(Enum::name).collect(Collectors.toSet());
    }
}
