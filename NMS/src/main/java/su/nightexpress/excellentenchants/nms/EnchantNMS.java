package su.nightexpress.excellentenchants.nms;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface EnchantNMS {

    @Deprecated
    @NotNull Material getItemBlockVariant(@NotNull Material material);

    @NotNull Set<Block> handleFlameWalker(@NotNull LivingEntity entity, int level, int radius);
}
