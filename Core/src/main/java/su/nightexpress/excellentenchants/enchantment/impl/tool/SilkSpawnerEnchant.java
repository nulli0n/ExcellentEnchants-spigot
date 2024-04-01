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
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class SilkSpawnerEnchant extends AbstractEnchantmentData implements ChanceData, BlockBreakEnchant, BlockDropEnchant, SimpeListener {

    public static final String ID = "divine_touch";

    private String             spawnerName;
    private ChanceSettingsImpl chanceSettings;

    private Location handleSpawner;

    private final NamespacedKey spawnerKey;

    public SilkSpawnerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.spawnerKey = new NamespacedKey(plugin, "divine_spawner");

        this.setDescription(ENCHANTMENT_CHANCE + "% chance to mine spawner.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.VERY_RARE);
        this.setConflicts(SmelterEnchant.ID);
    }

    @Override
    public boolean checkServerRequirements() {
        if (Plugins.isSpigot()) {
            this.warn("Enchantment can only be used in PaperMC or Paper based forks.");
            return false;
        }
        return true;
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.multiply(10, 1, 1, 100));

        this.spawnerName = ConfigValue.create("Settings.Spawner_Item.Name",
            YELLOW.enclose("Mob Spawner " + GRAY.enclose("(" + WHITE.enclose(GENERIC_TYPE) + ")")),
            "Spawner item display name.",
            "Use '" + GENERIC_TYPE + "' for the mob name."
        ).read(config);
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    @Override
    @NotNull
    public ItemCategory[] getItemCategories() {
        return new ItemCategory[]{ItemCategory.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
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
        stateItem.setDisplayName(this.spawnerName.replace(GENERIC_TYPE, LangAssets.get(spawnerBlock.getSpawnedType())));
        itemSpawner.setItemMeta(stateItem);

        PDCUtil.set(itemSpawner, this.spawnerKey, true);
        return itemSpawner;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        BlockState state = event.getBlockState();
        Block block = state.getBlock();
        if (this.handleSpawner == null || !this.handleSpawner.equals(block.getLocation())) return false;
        this.handleSpawner = null;

        if (!(state instanceof CreatureSpawner spawnerBlock)) return false;

        this.plugin.populateResource(event, this.getSpawner(spawnerBlock));

        if (this.hasVisualEffects()) {
            Location location = LocationUtil.getCenter(block.getLocation());
            UniParticle.of(Particle.VILLAGER_HAPPY).play(location, 0.3, 0.15, 30);
        }
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        Block block = event.getBlock();
        if (!(block.getState() instanceof CreatureSpawner)) return false;
        if (!this.checkTriggerChance(level)) return false;

        event.setExpToDrop(0);
        event.setDropItems(true);
        this.handleSpawner = block.getLocation();
        return false; // Do not consume charges
    }

    // Update spawner type of the placed spawner mined by Divine Touch.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawnerPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) return;

        Player player = event.getPlayer();
        ItemStack spawner = player.getInventory().getItem(event.getHand());
        if (spawner == null || spawner.getType() != Material.SPAWNER || !(spawner.getItemMeta() instanceof BlockStateMeta meta)) return;
        if (PDCUtil.getBoolean(spawner, this.spawnerKey).isEmpty()) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) meta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        spawnerBlock.setSpawnedType(spawnerItem.getSpawnedType());
        spawnerBlock.update();
    }
}
