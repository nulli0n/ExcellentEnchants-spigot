package su.nightexpress.excellentenchants.api.enchantment.bridge;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.nightcore.util.Pair;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface FlameWalker {

    Map<Location, Pair<Long, Integer>> MAGMA_BLOCKS = new ConcurrentHashMap<>();

    static void addBlock(@NotNull Block block, double seconds) {
        MAGMA_BLOCKS.put(block.getLocation(), Pair.of(System.currentTimeMillis() + (long) seconds * 1000L, Rnd.get(1000)));
    }

    @NotNull Modifier getRadius();

    double getBlockDecayTime(int level);
}
