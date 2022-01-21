package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.EnchantRegister;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Collections;
import java.util.List;

public class EnchantDivineTouch extends IEnchantChanceTemplate implements BlockBreakEnchant, CustomDropEnchant {

    private final String particleName;
    private final String particleData;
    private final String spawnerName;

    public static final String ID = "divine_touch";

    public EnchantDivineTouch(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);

        this.particleName = cfg.getString("Settings.Particle.Name", Particle.VILLAGER_HAPPY.name());
        this.particleData = cfg.getString("Settings.Particle.Data", "");
        this.spawnerName = StringUtil.color(cfg.getString("Settings.Spawner_Item.Name", "&aMob Spawner &7(%type%)"));
    }

    @Override
    protected void addConflicts() {
        super.addConflicts();
        this.addConflict(EnchantRegister.SMELTER);
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
        stateItem.setDisplayName(this.spawnerName.replace("%type%", plugin.lang().getEnum(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        return itemSpawner;
    }

    @Override
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return Collections.emptyList();

        return Collections.singletonList(this.getSpawner(spawnerBlock));
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return false;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        Block block = e.getBlock();
        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        Location location = LocationUtil.getCenter(block.getLocation());
        World world = block.getWorld();

        this.getCustomDrops(player, item, block, level).forEach(itemSpawner -> world.dropItemNaturally(location, itemSpawner));
        EffectUtil.playEffect(location, this.particleName, this.particleData, 0.3f, 0.3f, 0.3f, 0.15f, 30);

        e.setExpToDrop(0);
        e.setDropItems(false);
        return true;
    }

    // Update spawner type of the placed spawner mined by Divine Touch.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = e.getPlayer();
        ItemStack spawner = player.getInventory().getItemInMainHand();
        if (spawner.getType().isAir() || spawner.getType() != Material.SPAWNER) {
            spawner = player.getInventory().getItemInOffHand();
        }
        if (spawner.getType().isAir() || spawner.getType() != Material.SPAWNER) {
            return;
        }

        BlockStateMeta meta = (BlockStateMeta) spawner.getItemMeta();
        if (meta == null) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
