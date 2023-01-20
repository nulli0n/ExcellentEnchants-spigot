package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

public class EnchantDivineTouch extends ExcellentEnchant implements Chanced, BlockBreakEnchant, BlockDropEnchant {

    public static final String  ID          = "divine_touch";
    private static final String META_HANDLE = ID + "_handle";

    private String spawnerName;
    private ChanceImplementation chanceImplementation;

    public EnchantDivineTouch(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.spawnerName = JOption.create("Settings.Spawner_Item.Name", "&aMob Spawner &7(" + Placeholders.GENERIC_TYPE + ")",
            "Spawner item display name.",
            "Placeholder '" + Placeholders.GENERIC_TYPE + "' for the mob type.").read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @NotNull
    public ItemStack getSpawner(@NotNull CreatureSpawner spawnerBlock) {
        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateItem = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateItem == null) return itemSpawner;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateItem.getBlockState();
        spawnerItem.setSpawnedType(spawnerBlock.getSpawnedType());
        spawnerItem.update(true);
        stateItem.setBlockState(spawnerItem);
        stateItem.setDisplayName(this.spawnerName.replace(Placeholders.GENERIC_TYPE, plugin.getLangManager().getEnum(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        return itemSpawner;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockState state = e.getBlockState();
        Block block = state.getBlock();
        if (!block.hasMetadata(META_HANDLE)) return false;
        if (!(state instanceof CreatureSpawner spawnerBlock)) return false;

        dropContainer.getDrop().add(this.getSpawner(spawnerBlock));

        Location location = LocationUtil.getCenter(block.getLocation());
        if (this.hasVisualEffects()) {
            EffectUtil.playEffect(location, Particle.VILLAGER_HAPPY, "", 0.3f, 0.3f, 0.3f, 0.15f, 30);
        }
        block.removeMetadata(META_HANDLE, this.plugin);
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isAvailableToUse(player)) return false;
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return false;
        if (!this.checkTriggerChance(level)) return false;

        e.setExpToDrop(0);
        e.setDropItems(true);
        block.setMetadata(META_HANDLE, new FixedMetadataValue(this.plugin, true));
        return true;
    }

    // Update spawner type of the placed spawner mined by Divine Touch.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = e.getPlayer();
        ItemStack spawner = player.getInventory().getItem(e.getHand());
        if (spawner == null || spawner.getType() != Material.SPAWNER || !(spawner.getItemMeta() instanceof BlockStateMeta meta)) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
