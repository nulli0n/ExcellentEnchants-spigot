package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniSound;

import java.io.File;
import java.util.Set;

public class ReplanterEnchant extends AbstractEnchantmentData implements ChanceData, InteractEnchant, BlockBreakEnchant {

    public static final String ID = "replanter";

    private boolean replantOnRightClick;
    private boolean replantOnPlantBreak;

    private ChanceSettingsImpl chanceSettings;

    private static final Set<Material> CROPS = Set.of(
        Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS,
        Material.MELON_SEEDS, Material.PUMPKIN_SEEDS,
        Material.POTATO, Material.CARROT, Material.NETHER_WART);

    public ReplanterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Automatically replant crops on right click and when harvest.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config);

        this.replantOnRightClick = ConfigValue.create("Settings.Replant.On_Right_Click",
            true,
            "When 'true', player will be able to replant crops when right-clicking farmland blocks."
        ).read(config);

        this.replantOnPlantBreak = ConfigValue.create("Settings.Replant.On_Plant_Break",
            true,
            "When 'true', crops will be automatically replanted when player break plants with enchanted tool in hand."
        ).read(config);
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public boolean isReplantOnPlantBreak() {
        return replantOnPlantBreak;
    }

    public boolean isReplantOnRightClick() {
        return replantOnRightClick;
    }

    @NotNull
    private Material fineSeedsToBlock(@NotNull Material material) {
        return plugin.getEnchantNMS().getItemBlockVariant(material);
    }

    private boolean takeSeeds(@NotNull Player player, @NotNull Material material) {
        int slot = player.getInventory().first(material);
        if (slot < 0) return false;

        ItemStack seed = player.getInventory().getItem(slot);
        if (seed == null || seed.getType().isAir()) return false;

        seed.setAmount(seed.getAmount() - 1);
        return true;
    }

    @Override
    @NotNull
    public ItemCategory[] getItemCategories() {
        return new ItemCategory[]{ItemCategory.HOE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
    }

    @NotNull
    @Override
    public EventPriority getInteractPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public boolean onInteract(@NotNull PlayerInteractEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (!this.isReplantOnRightClick()) return false;
        if (!this.checkTriggerChance(level)) return false;

        // Check for a event hand. We dont want to trigger it twice.
        if (event.getHand() != EquipmentSlot.HAND) return false;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return false;

        // Check if player holds seeds to plant them by offhand interaction.
        ItemStack off = player.getInventory().getItemInOffHand();
        if (!off.getType().isAir() && CROPS.contains(off.getType())) return false;

        // Check if clicked block is a farmland.
        Block blockGround = event.getClickedBlock();
        if (blockGround == null) return false;
        if (blockGround.getType() != Material.FARMLAND && blockGround.getType() != Material.SOUL_SAND) return false;

        // Check if someting is already growing on the farmland.
        Block blockPlant = blockGround.getRelative(BlockFace.UP);
        if (!blockPlant.isEmpty()) return false;

        // Get the first crops from player's inventory and plant them.
        for (Material seed : CROPS) {
            if (seed == Material.NETHER_WART && blockGround.getType() == Material.SOUL_SAND
                || seed != Material.NETHER_WART && blockGround.getType() == Material.FARMLAND) {
                if (this.takeSeeds(player, seed)) {
                    UniSound.of(seed == Material.NETHER_WART ? Sound.ITEM_NETHER_WART_PLANT : Sound.ITEM_CROP_PLANT).play(player);
                    plugin.getEnchantNMS().sendAttackPacket(player, 0);
                    blockPlant.setType(this.fineSeedsToBlock(seed));
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (!this.isReplantOnPlantBreak()) return false;
        if (!this.checkTriggerChance(level)) return false;

        Block blockPlant = event.getBlock();

        // Check if broken block is supported crop(s).
        if (!CROPS.contains(blockPlant.getBlockData().getPlacementMaterial())) return false;

        // Check if broken block is actually can grow.
        BlockData dataPlant = blockPlant.getBlockData();
        if (!(dataPlant instanceof Ageable plant)) return false;

        // Replant the gathered crops with a new one.
        if (this.takeSeeds(player, dataPlant.getPlacementMaterial())) {
            plugin.runTask(task -> {
                blockPlant.setType(plant.getMaterial());
                plant.setAge(0);
                blockPlant.setBlockData(plant);
            });
        }
        return true;
    }
}
