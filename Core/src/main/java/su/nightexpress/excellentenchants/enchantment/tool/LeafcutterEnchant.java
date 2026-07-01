package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.MiningEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;
import java.util.*;

@NullMarked
public class LeafcutterEnchant extends GameEnchantment implements MiningEnchant {

    private static final BlockFace[] BLOCK_SIDES = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH,
        BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST
    };

    private boolean disableOnCrouch;
    private double durabilityChance;
    private int blockLimit;

    private final Map<UUID, Set<Location>> pendingLogs = new HashMap<>();

    public LeafcutterEnchant(EnchantsPlugin plugin, EnchantManager manager, Path file, EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(FileConfig config) {
        this.disableOnCrouch = ConfigValue.create("Leafcutter.Disable_On_Crouch",
            true,
            "Sets whether or not enchantment will have no effect when crouching."
        ).read(config);

        this.durabilityChance = ConfigValue.create("Leafcutter.Durability_Chance",
            0.10,
            "Chance (between 0.0 and 1.0) to damage the tool for each leaf block destroyed."
        ).read(config);

        this.blockLimit = ConfigValue.create("Leafcutter.Block_Limit",
            500,
            "Max amount of leaf blocks to be checked/broken at the same time to prevent lag."
        ).read(config);
    }

    private static boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private static boolean isLeaves(Material material) {
        return Tag.LEAVES.isTagged(material);
    }

    @Override
    public EnchantPriority getBreakPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onBreak(BlockBreakEvent event, LivingEntity entity, ItemStack tool, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantsUtils.isBusy()) return false;
        if (this.disableOnCrouch && player.isSneaking()) return false;

        Block block = event.getBlock();
        if (!isLog(block.getType())) return false;

        UUID playerUUID = player.getUniqueId();
        synchronized (pendingLogs) {
            Set<Location> playerLogs = pendingLogs.computeIfAbsent(playerUUID, k -> new HashSet<>());
            if (playerLogs.isEmpty()) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    Set<Location> logsToProcess;
                    synchronized (pendingLogs) {
                        logsToProcess = pendingLogs.remove(playerUUID);
                    }
                    if (logsToProcess != null && !logsToProcess.isEmpty()) {
                        this.processLeaves(player, tool, logsToProcess);
                    }
                }, 1L);
            }
            playerLogs.add(block.getLocation());
        }

        return true;
    }

    private void processLeaves(Player player, ItemStack tool, Set<Location> logLocations) {
        if (tool.getAmount() <= 0) return;

        Queue<Block> queue = new LinkedList<>();
        Set<Block> visited = new HashSet<>();
        Set<Block> candidateLeaves = new HashSet<>();

        for (Location loc : logLocations) {
            World world = loc.getWorld();
            if (world == null) continue;
            Block logBlock = world.getBlockAt(loc);
            for (BlockFace face : BLOCK_SIDES) {
                Block neighbor = logBlock.getRelative(face);
                if (isLeaves(neighbor.getType()) && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        while (!queue.isEmpty() && visited.size() < this.blockLimit) {
            Block current = queue.poll();
            candidateLeaves.add(current);

            for (BlockFace face : BLOCK_SIDES) {
                Block neighbor = current.getRelative(face);
                if (isLeaves(neighbor.getType()) && visited.add(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        Set<Block> leavesToBreak = new HashSet<>();
        Set<Block> knownConnected = new HashSet<>();
        Set<Block> knownOrphans = new HashSet<>();

        for (Block leaf : candidateLeaves) {
            if (knownConnected.contains(leaf)) continue;
            if (knownOrphans.contains(leaf)) {
                leavesToBreak.add(leaf);
                continue;
            }

            Set<Block> localPath = new HashSet<>();
            boolean connected = hasActiveLogConnection(leaf, logLocations, localPath, knownConnected);
            if (connected) {
                knownConnected.addAll(localPath);
            } else {
                knownOrphans.addAll(localPath);
                leavesToBreak.add(leaf);
            }
        }

        if (leavesToBreak.isEmpty()) return;

        ItemMeta meta = tool.getItemMeta();
        Damageable damageable = (meta instanceof Damageable) ? (Damageable) meta : null;
        int startDamage = (damageable != null) ? damageable.getDamage() : 0;
        boolean trackDurability = damageable != null && player.getGameMode() != GameMode.CREATIVE;

        int brokenCount = 0;

        for (Block leaf : leavesToBreak) {
            if (tool.getType().isAir() || tool.getAmount() <= 0) break;

            if (trackDurability && damageable != null) {
                damageable.setDamage(0);
                tool.setItemMeta(meta);
            }

            Material oldType = leaf.getType();
            EnchantsUtils.safeBusyBreak(player, leaf);

            if (leaf.getType() != oldType) {
                brokenCount++;
            }
        }

        if (trackDurability && brokenCount > 0 && damageable != null && !tool.getType().isAir() && tool.getAmount() > 0) {
            ItemMeta currentMeta = tool.getItemMeta();
            if (currentMeta instanceof Damageable currentDamageable) {
                int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
                double finalChance = this.durabilityChance / (unbreakingLevel + 1);

                int calculatedDamage = 0;
                for (int i = 0; i < brokenCount; i++) {
                    if (Math.random() < finalChance) {
                        calculatedDamage++;
                    }
                }

                int maxDamage = tool.getType().getMaxDurability();
                int newDamage = startDamage + calculatedDamage;

                if (newDamage >= maxDamage) {
                    tool.setAmount(0);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                } else {
                    currentDamageable.setDamage(newDamage);
                    tool.setItemMeta(currentMeta);
                }
            }
        }
    }


    private boolean hasActiveLogConnection(Block startLeaf, Set<Location> brokenLogs, Set<Block> localVisited, Set<Block> knownConnected) {
        Queue<Block> queue = new LinkedList<>();
        queue.add(startLeaf);
        localVisited.add(startLeaf);

        int depth = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            if (depth > 6) {
                break;
            }
            for (int i = 0; i < size; i++) {
                Block current = queue.poll();

                if (isLog(current.getType()) && !brokenLogs.contains(current.getLocation())) {
                    return true;
                }

                if (knownConnected.contains(current)) {
                    return true;
                }

                if (isLeaves(current.getType())) {
                    for (BlockFace face : BLOCK_SIDES) {
                        Block neighbor = current.getRelative(face);
                        if ((isLeaves(neighbor.getType()) || isLog(neighbor.getType())) && localVisited.add(neighbor)) {
                            queue.add(neighbor);
                        }
                    }
                }
            }
            depth++;
        }
        return false;
    }
}
