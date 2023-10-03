package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.hook.impl.NoCheatPlusHook;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VeinminerEnchant extends ExcellentEnchant implements BlockBreakEnchant {

    public static final  String      ID                = "veinminer";

    private static final BlockFace[] AREA = {
        BlockFace.UP, BlockFace.DOWN, BlockFace.EAST,
        BlockFace.WEST, BlockFace.SOUTH, BlockFace.NORTH
    };

    private static final String PLACEHOLDER_BLOCK_LIMIT = "%enchantment_block_limit%";

    private Scaler        blocksLimit;
    private Set<Material> blocksAffected;

    public VeinminerEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);

        this.getDefaults().setDescription("Mines up to " + PLACEHOLDER_BLOCK_LIMIT + " blocks of the ore vein at once.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.3);
        this.getDefaults().setConflicts(BlastMiningEnchant.ID, TunnelEnchant.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.blocksLimit = EnchantScaler.read(this, "Settings.Blocks.Max_At_Once",
            "6 + " + Placeholders.ENCHANTMENT_LEVEL,
            "How much amount of blocks can be destroted at single use?");

        this.blocksAffected = JOption.forSet("Settings.Blocks.Affected",
            str -> Material.getMaterial(str.toUpperCase()),
            () -> {
                Set<Material> set = new HashSet<>();
                set.addAll(Tag.COAL_ORES.getValues());
                set.addAll(Tag.COPPER_ORES.getValues());
                set.addAll(Tag.DIAMOND_ORES.getValues());
                set.addAll(Tag.EMERALD_ORES.getValues());
                set.addAll(Tag.GOLD_ORES.getValues());
                set.addAll(Tag.IRON_ORES.getValues());
                set.addAll(Tag.LAPIS_ORES.getValues());
                set.addAll(Tag.REDSTONE_ORES.getValues());
                set.add(Material.NETHER_GOLD_ORE);
                set.add(Material.NETHER_QUARTZ_ORE);
                return set;
            },
            "List of blocks, that will be affected by this enchantment.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html"
        ).setWriter((cfg, path, set) -> cfg.set(path, set.stream().map(Enum::name).toList())).read(cfg);

        this.addPlaceholder(PLACEHOLDER_BLOCK_LIMIT, level -> String.valueOf(this.getBlocksLimit(level)));
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
        ores.forEach(ore -> EnchantUtils.safeBusyBreak(player, ore));
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack tool, int level) {
        if (!(entity instanceof Player player)) return false;
        if (EnchantUtils.isBusy()) return false;

        Block block = event.getBlock();
        if (block.getDrops(tool, player).isEmpty()) return false;
        if (!this.getBlocksAffected().contains(block.getType())) return false;

        NoCheatPlusHook.exemptBlocks(player);
        this.vein(player, block, level);
        NoCheatPlusHook.unexemptBlocks(player);
        return true;
    }
}
