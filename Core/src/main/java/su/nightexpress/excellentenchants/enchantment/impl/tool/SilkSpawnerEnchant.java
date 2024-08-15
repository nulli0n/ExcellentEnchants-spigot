package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.spawner.Spawner;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.Plugins;
import su.nightexpress.nightcore.util.text.NightMessage;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;
import java.util.HashSet;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_TYPE;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class SilkSpawnerEnchant extends GameEnchantment implements ChanceMeta, BlockBreakEnchant, BlockDropEnchant, SimpeListener {

    public static final String ID = "divine_touch";

    private String spawnerName;

    private Location handleSpawner;

    private final NamespacedKey spawnerKey;

    public SilkSpawnerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));
        this.spawnerKey = new NamespacedKey(plugin, "divine_spawner");
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            Lists.newList(ENCHANTMENT_CHANCE + "% chance to mine spawner."),
            EnchantRarity.MYTHIC,
            5,
            ItemCategories.TOOL,
            ItemCategories.PICKAXE,
            Lists.newSet(SmelterEnchant.ID)
        );
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
        this.meta.setProbability(Probability.create(config, Modifier.multiply(10, 1, 1, 100)));

        this.spawnerName = ConfigValue.create("Settings.Spawner_Item.Name",
            YELLOW.enclose("Mob Spawner " + GRAY.enclose("(" + WHITE.enclose(GENERIC_TYPE) + ")")),
            "Spawner item display name.",
            "Use '" + GENERIC_TYPE + "' for the mob name."
        ).read(config);
    }

    @NotNull
    public ItemStack getSpawner(@NotNull CreatureSpawner spawnerBlock) {
        ItemStack itemSpawner = new ItemStack(Material.SPAWNER);
        BlockStateMeta stateItem = (BlockStateMeta) itemSpawner.getItemMeta();
        if (stateItem == null || spawnerBlock.getSpawnedType() == null) return itemSpawner;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateItem.getBlockState();

        this.transferSettings(spawnerBlock, spawnerItem);
        spawnerItem.update(true);

        stateItem.setBlockState(spawnerItem);
        stateItem.setDisplayName(NightMessage.asLegacy(this.spawnerName.replace(GENERIC_TYPE, LangAssets.get(spawnerBlock.getSpawnedType()))));
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
            Location location = LocationUtil.setCenter3D(block.getLocation());
            UniParticle.of(Particle.HAPPY_VILLAGER).play(location, 0.3, 0.15, 30);
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
        if (spawner == null || spawner.getType() != Material.SPAWNER || !(spawner.getItemMeta() instanceof BlockStateMeta stateMeta)) return;
        if (PDCUtil.getBoolean(spawner, this.spawnerKey).isEmpty()) return;

        CreatureSpawner spawnerItem = (CreatureSpawner) stateMeta.getBlockState();
        CreatureSpawner spawnerBlock = (CreatureSpawner) block.getState();

        this.transferSettings(spawnerItem, spawnerBlock);
        spawnerBlock.update(true);
    }

    private void transferSettings(@NotNull Spawner from, @NotNull Spawner to) {
        to.setPotentialSpawns(new HashSet<>());

        if (from.getPotentialSpawns().isEmpty()) {
            to.setSpawnedType(from.getSpawnedType());
        }
        else {
            from.getPotentialSpawns().forEach(to::addPotentialSpawn);
        }
        to.setDelay(from.getDelay());
        to.setMinSpawnDelay(from.getMinSpawnDelay());
        to.setMaxSpawnDelay(from.getMaxSpawnDelay());
        to.setMaxNearbyEntities(from.getMaxNearbyEntities());
        to.setRequiredPlayerRange(from.getRequiredPlayerRange());
        to.setSpawnCount(from.getSpawnCount());
        to.setSpawnRange(from.getSpawnRange());
    }
}
