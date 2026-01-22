package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TreefellerEnchant extends GameEnchantment implements MiningEnchant {

    private static final BlockFace[] BLOCK_SIDES = {
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.EAST,
        BlockFace.WEST,
        BlockFace.SOUTH,
        BlockFace.NORTH,
        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST,
        BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    private Modifier blocksLimit;
    private boolean  disableOnCrouch;

    public TreefellerEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.disableOnCrouch = ConfigValue.create("Treefeller.Disable_On_Crouch",
            true,
            "Controls whether enchantment effect can be bypassed by crouching."
        ).read(config);

        this.blocksLimit = Modifier.load(config, "Treefeller.Block_Limit",
            Modifier.addictive(16).perLevel(8).capacity(180),
            "Max. blocks to lookup for tree logs (including leaves).");
    }

    @NotNull
    private Set<Block> getRelatives(@NotNull Block block) {
        return Stream.of(BLOCK_SIDES).map(block::getRelative).filter(b -> isLogOrLeaves(b.getType())).collect(Collectors.toSet());
    }

    @NotNull
    private Set<Block> getLogsAndLeaves(@NotNull Block source, int limit) {
        Set<Block> full = new HashSet<>();
        Set<Block> lookupBlocks = Lists.newSet(source);

        this.addConnections(full, lookupBlocks, limit);
        return full;
    }

    private boolean addConnections(@NotNull Set<Block> full, @NotNull Set<Block> lookupBlocks, int limit) {
        if (full.size() >= limit) return false;
        if (!full.addAll(lookupBlocks)) return false;

        Set<Block> connected = new HashSet<>();

        lookupBlocks.forEach(lookup -> {
            Set<Block> connections = this.getRelatives(lookup);

            // Do not lookup leaves too depth.
            if (isLeaves(lookup.getType())) {
                connections.removeIf(connect -> isLeaves(connect.getType()));
            }

            connected.addAll(connections);
        });

        return this.addConnections(full, connected, limit);
    }

    private static boolean isLogOrLeaves(@NotNull Material material) {
        return isLog(material) || isLeaves(material);
    }

    private static boolean isLog(@NotNull Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private static boolean isLeaves(@NotNull Material material) {
        return Tag.LEAVES.isTagged(material);
    }

    private void chopTree(@NotNull Player player, @NotNull Block source, @NotNull ItemStack tool, int level) {
        Set<Block> logsToBreak = this.getLogsAndLeaves(source, this.blocksLimit.getIntValue(level));

        logsToBreak.remove(source);

        for (Block log : logsToBreak) {
            if (tool.getAmount() <= 0) break; // Item broke.
            if (!isLog(log.getType())) continue;

            EnchantsUtils.safeBusyBreak(player, log);
        }
    }

    @Override
    @NotNull
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack tool, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantsUtils.isBusy()) return false;
        if (this.disableOnCrouch && player.isSneaking()) return false;

        Block block = event.getBlock();
        if (!isLog(block.getType())) return false;

        this.chopTree(player, block, tool, level);
        return true;
    }
}
