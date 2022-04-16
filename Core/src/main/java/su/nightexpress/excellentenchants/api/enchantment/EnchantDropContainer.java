package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EnchantDropContainer {

    private final BlockDropItemEvent parent;
    private final List<ItemStack>    drop;

    public EnchantDropContainer(@NotNull BlockDropItemEvent parent) {
        this.parent = parent;
        this.drop = new ArrayList<>();
    }

    @NotNull
    public BlockDropItemEvent getParent() {
        return parent;
    }

    @NotNull
    public List<ItemStack> getDrop() {
        return drop;
    }
}
