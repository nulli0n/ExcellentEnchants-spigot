package su.nightexpress.excellentenchants.api.enchantment.bridge;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;

public interface FlameWalker {

    void removeBlocks();

    void tickBlocks();

    void addBlock(@NotNull Block block, int level);

    @NotNull Modifier getRadius();

    double getBlockDecayTime(int level);
}
