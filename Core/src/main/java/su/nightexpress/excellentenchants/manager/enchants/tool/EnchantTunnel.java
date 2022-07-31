package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.hook.HookNCP;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.HashSet;
import java.util.Set;

public class EnchantTunnel extends IEnchantChanceTemplate implements BlockBreakEnchant {

    private boolean disableOnSneak;

    public static final String   ID                   = "tunnel";
    private static final String  META_BLOCK_TUNNEL    = ID + "_block_tunneled";
    // X and Z offsets for each block AoE mined
    private static final int[][] MINING_COORD_OFFSETS = new int[][]{{0, 0}, {0, -1}, {-1, 0}, {0, 1}, {1, 0}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1},};
    private static final Set<Material> INTERACTABLE_BLOCKS = new HashSet<>();

    static {
        INTERACTABLE_BLOCKS.add(Material.REDSTONE_ORE);
        INTERACTABLE_BLOCKS.add(Material.DEEPSLATE_REDSTONE_ORE);
    }

    public EnchantTunnel(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGH);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.disableOnSneak = cfg.getBoolean("Settings.Ignore_When_Sneaking");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE, FitItemType.SHOVEL};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isEnchantmentAvailable(player)) return false;
        if (this.disableOnSneak && player.isSneaking()) return false;
        if (EnchantRegister.VEINMINER != null && item.containsEnchantment(EnchantRegister.VEINMINER)) return false;
        if (EnchantRegister.BLAST_MINING != null && item.containsEnchantment(EnchantRegister.BLAST_MINING)) return false;
        if (block.hasMetadata(META_BLOCK_TUNNEL)) return false;
        if (block.getType().isInteractable() && !INTERACTABLE_BLOCKS.contains(block.getType())) return false;
        if (block.getDrops(item).isEmpty()) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        BlockFace dir = LocationUtil.getDirection(player);
        boolean isY = dir != null && block.getRelative(dir.getOppositeFace()).isEmpty();
        boolean isZ = dir == BlockFace.EAST || dir == BlockFace.WEST;

        // Mine + shape if Tunnel I, 3x3 if Tunnel II
        int blocksBroken = 1;
        if (level == 1) blocksBroken = 2;
        else if (level == 2) blocksBroken = 5;
        else if (level == 3) blocksBroken = 9;

        HookNCP.exemptBlocks(player);

        for (int i = 0; i < blocksBroken; i++) {
            if (item.getType().isAir()) break;

            int xAdd = MINING_COORD_OFFSETS[i][0];
            int zAdd = MINING_COORD_OFFSETS[i][1];

            Block blockAdd;
            if (isY) {
                blockAdd = block.getLocation().clone().add(isZ ? 0 : xAdd, zAdd, isZ ? xAdd : 0).getBlock();
            }
            else {
                blockAdd = block.getLocation().clone().add(xAdd, 0, zAdd).getBlock();
            }

            // Skip blocks that should not be mined
            if (blockAdd.equals(block)) continue;
            if (blockAdd.getDrops(item).isEmpty()) continue;
            if (blockAdd.isLiquid()) continue;

            Material addType = blockAdd.getType();

            // Some extra block checks.
            if (addType.isInteractable() && !INTERACTABLE_BLOCKS.contains(addType)) continue;
            if (addType == Material.BEDROCK || addType == Material.END_PORTAL || addType == Material.END_PORTAL_FRAME) continue;
            if (addType == Material.OBSIDIAN && addType != block.getType()) continue;

            // Play block break particles before it's broken.
            EffectUtil.playEffect(LocationUtil.getCenter(blockAdd.getLocation()), Particle.BLOCK_CRACK.name(), blockAdd.getType().name(), 0.2, 0.2, 0.2, 0.1, 20);

            // Add metadata to prevent enchantment triggering in a loop.
            blockAdd.setMetadata(META_BLOCK_TUNNEL, new FixedMetadataValue(plugin, true));
            plugin.getNMS().breakBlock(player, blockAdd);
            blockAdd.removeMetadata(META_BLOCK_TUNNEL, plugin);
        }

        HookNCP.unexemptBlocks(player);
        return true;
    }
}
