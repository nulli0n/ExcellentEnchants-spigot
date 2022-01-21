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
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.hook.HookNCP;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantVeinminer extends IEnchantChanceTemplate implements BlockBreakEnchant {

    private final Scaler        blocksLimit;
    private final Set<Material> blocksAffected;

    public static final  String      ID                = "veinminer";

    private static final BlockFace[] AREA                    = {BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH};
    private static final String      META_BLOCK_VEINED = ID + "_block_veined";
    private static final String      PLACEHOLDER_BLOCK_LIMIT = "%enchantment_block_limit%";

    public EnchantVeinminer(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGH);

        this.blocksLimit = new EnchantScaler(this, "Settings.Blocks.Max_At_Once");
        this.blocksAffected = cfg.getStringSet("Settings.Blocks.Affected").stream()
            .map(type -> Material.getMaterial(type.toUpperCase())).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.TUNNEL);
        this.addConflict(EnchantRegister.BLAST_MINING);
    }

    @NotNull
    public Set<Material> getBlocksAffected() {
        return this.blocksAffected;
    }

    public int getBlocksLimit(int level) {
        return (int) this.blocksLimit.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_BLOCK_LIMIT, String.valueOf(this.getBlocksLimit(level)))
        );
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @NotNull
    private Set<Block> getNearby(@NotNull Block block) {
        return Stream.of(AREA).map(block::getRelative)
            .filter(blockAdded -> blockAdded.getType() == block.getType()).collect(Collectors.toSet());
    }

    private void vein(@NotNull Player player, @NotNull Block source, int level) {
        Set<Block> ores = new HashSet<>();
        Set<Block> prepare = new HashSet<>(this.getNearby(source));

        int limit = Math.min(this.getBlocksLimit(level), 30);
        if (limit < 0) return;

        while (ores.addAll(prepare) && ores.size() < limit) {
            Set<Block> nearby = new HashSet<>();
            prepare.forEach(prepared -> nearby.addAll(this.getNearby(prepared)));
            prepare.clear();
            prepare.addAll(nearby);
        }
        ores.remove(source);
        ores.forEach(ore -> {
            // Play block break particles before the block broken.
            EffectUtil.playEffect(LocationUtil.getCenter(ore.getLocation()), Particle.BLOCK_CRACK.name(), ore.getType().name(), 0.2, 0.2, 0.2, 0.1, 20);

            ore.setMetadata(META_BLOCK_VEINED, new FixedMetadataValue(plugin, true));
            plugin.getNMS().breakBlock(player, ore);
            ore.removeMetadata(META_BLOCK_VEINED, plugin);
        });
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack tool, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (EnchantRegister.TUNNEL != null && tool.containsEnchantment(EnchantRegister.TUNNEL)) return false;
        if (EnchantRegister.BLAST_MINING != null && tool.containsEnchantment(EnchantRegister.BLAST_MINING)) return false;

        Block block = e.getBlock();
        if (block.hasMetadata(META_BLOCK_VEINED)) return false;
        if (block.getDrops(tool).isEmpty()) return false;

        if (!this.getBlocksAffected().contains(block.getType())) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        HookNCP.exemptBlocks(player);
        this.vein(player, block, level);
        HookNCP.unexemptBlocks(player);

        return true;
    }
}
