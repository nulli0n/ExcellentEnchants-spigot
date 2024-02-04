package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.manager.EventListener;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.Colors;
import su.nexmedia.engine.utils.LocationUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.values.UniParticle;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class DivineTouchEnchant extends ExcellentEnchant implements Chanced, BlockBreakEnchant, BlockDropEnchant, EventListener {

    public static final String  ID          = "divine_touch";

    private final NamespacedKey key;

    private String spawnerName;
    private ChanceImplementation chanceImplementation;

    private Location handleSpawner;

    public DivineTouchEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.key = new NamespacedKey(plugin, "divine_spawner");

        this.getDefaults().setDescription(Placeholders.ENCHANTMENT_CHANCE + "% chance to mine spawner.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(1.0);
        this.getDefaults().setConflicts(SmelterEnchant.ID);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();

        this.chanceImplementation = ChanceImplementation.create(this,
            "15.0 * " + Placeholders.ENCHANTMENT_LEVEL);

        this.spawnerName = JOption.create("Settings.Spawner_Item.Name",
            Colors.GREEN + "Mob Spawner " + Colors.GRAY + "(" + Placeholders.GENERIC_TYPE + ")",
            "Spawner item display name.",
            "Placeholder '" + Placeholders.GENERIC_TYPE + "' for the mob type."
        ).mapReader(Colorizer::apply).read(cfg);
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    @Override
    @NotNull
    public ItemCategory[] getFitItemTypes() {
        return new ItemCategory[]{ItemCategory.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
    }

    @NotNull
    @Override
    public EventPriority getDropPriority() {
        return EventPriority.NORMAL;
    }

    @NotNull
    @Override
    public EventPriority getBreakPriority() {
        return EventPriority.HIGH;
    }

    @NotNull
    public ItemStack getSpawner(@NotNull CreatureSpawner spawnerBlock) {
        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateItem = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateItem == null || spawnerBlock.getSpawnedType() == null) return itemSpawner;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateItem.getBlockState();
        spawnerItem.setSpawnedType(spawnerBlock.getSpawnedType());
        spawnerItem.update(true);
        stateItem.setBlockState(spawnerItem);
        stateItem.setDisplayName(this.spawnerName.replace(Placeholders.GENERIC_TYPE, LangManager.getEntityType(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        PDCUtil.set(itemSpawner, this.key, true);
        return itemSpawner;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        BlockState state = event.getBlockState();
        Block block = state.getBlock();
        if (this.handleSpawner == null || !this.handleSpawner.equals(block.getLocation())) return false;
        this.handleSpawner = null;

        if (!(state instanceof CreatureSpawner spawnerBlock)) return false;

        EnchantUtils.popResource(event, this.getSpawner(spawnerBlock));

        if (this.hasVisualEffects()) {
            Location location = LocationUtil.getCenter(block.getLocation());
            UniParticle.of(Particle.VILLAGER_HAPPY).play(location, 0.3, 0.15, 30);
        }
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner spawnerBlock)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.setExpToDrop(0);
        event.setDropItems(true);
        this.handleSpawner = block.getLocation();
        return false; // Do not consume charges
    }

    // Update spawner type of the placed spawner mined by Divine Touch.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = e.getPlayer();
        ItemStack spawner = player.getInventory().getItem(e.getHand());
        if (spawner == null || spawner.getType() != Material.SPAWNER || !(spawner.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (PDCUtil.getBoolean(spawner, this.key).isEmpty()) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
