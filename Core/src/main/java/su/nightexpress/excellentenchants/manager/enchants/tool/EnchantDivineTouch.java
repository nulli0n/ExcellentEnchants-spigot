package su.nightexpress.excellentenchants.manager.enchants.tool;

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
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

public class EnchantDivineTouch extends IEnchantChanceTemplate implements BlockBreakEnchant, CustomDropEnchant {

    public static final String  ID          = "divine_touch";
    private static final String META_HANDLE = ID + "_handle";

    private String particleName;
    private String particleData;
    private String spawnerName;

    public EnchantDivineTouch(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.particleName = cfg.getString("Settings.Particle.Name", Particle.VILLAGER_HAPPY.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.spawnerName = StringUtil.color(cfg.getString("Settings.Spawner_Item.Name", "&aMob Spawner &7(%type%)"));
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        cfg.remove("Settings.Particle_Effect");
        cfg.addMissing("Settings.Particle.Name", Particle.VILLAGER_HAPPY.name());
        cfg.addMissing("Settings.Particle.Data", "");
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
        stateItem.setDisplayName(this.spawnerName.replace("%type%", plugin.getLangManager().getEnum(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        return itemSpawner;
    }

    @Override
    public void handleDrop(@NotNull EnchantDropContainer e, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockDropItemEvent parent = e.getParent();
        BlockState state = parent.getBlockState();
        Block block = state.getBlock();
        if (!block.hasMetadata(META_HANDLE)) return;
        if (!(state instanceof CreatureSpawner spawnerBlock)) return;

        e.getDrop().add(this.getSpawner(spawnerBlock));

        Location location = LocationUtil.getCenter(block.getLocation());
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.3f, 0.3f, 0.3f, 0.15f, 30);
        block.removeMetadata(META_HANDLE, this.plugin);
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

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
