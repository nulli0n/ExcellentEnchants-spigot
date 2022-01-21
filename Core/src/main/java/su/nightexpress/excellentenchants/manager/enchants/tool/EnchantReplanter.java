package su.nightexpress.excellentenchants.manager.enchants.tool;

import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.InteractEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Set;

public class EnchantReplanter extends IEnchantChanceTemplate implements InteractEnchant, BlockBreakEnchant {

    private final boolean replantOnRightClick;
    private final boolean replantOnPlantBreak;

    public static final String ID = "replanter";

    private static final Set<Material> CROPS = Sets.newHashSet(
        Material.WHEAT_SEEDS, Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS,
        Material.POTATO, Material.CARROT, Material.NETHER_WART);

    public EnchantReplanter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGH);

        this.replantOnRightClick = cfg.getBoolean("Settings.Replant.On_Right_Click");
        this.replantOnPlantBreak = cfg.getBoolean("Settings.Replant.On_Plant_Break");
    }

    public boolean isReplantOnPlantBreak() {
        return replantOnPlantBreak;
    }

    public boolean isReplantOnRightClick() {
        return replantOnRightClick;
    }

    @NotNull
    private Material fineSeedsToBlock(@NotNull Material material) {
        if (material == Material.POTATO) return Material.POTATOES;
        if (material == Material.CARROT) return Material.CARROTS;
        if (material == Material.BEETROOT_SEEDS) return Material.BEETROOTS;
        if (material == Material.WHEAT_SEEDS) return Material.WHEAT;
        if (material == Material.PUMPKIN_SEEDS) return Material.PUMPKIN_STEM;
        if (material == Material.MELON_SEEDS) return Material.MELON_STEM;
        return material;
    }

    @NotNull
    private Material fineBlockToSeeds(@NotNull Material material) {
        if (material == Material.POTATOES) return Material.POTATO;
        if (material == Material.CARROTS) return Material.CARROT;
        if (material == Material.BEETROOTS) return Material.BEETROOT_SEEDS;
        if (material == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (material == Material.MELON_STEM) return Material.MELON_SEEDS;
        if (material == Material.PUMPKIN_STEM) return Material.PUMPKIN_SEEDS;
        return material;
    }

    private boolean takeSeeds(@NotNull Player player, @NotNull Material material) {
        material = this.fineBlockToSeeds(material);

        int slot = player.getInventory().first(material);
        if (slot < 0) return false;

        ItemStack seed = player.getInventory().getItem(slot);
        if (seed == null || seed.getType().isAir()) return false;

        seed.setAmount(seed.getAmount() - 1);
        return true;
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.HOE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean use(@NotNull PlayerInteractEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!this.isReplantOnRightClick()) return false;

        // Check for a event hand. We dont want to trigger it twice.
        if (e.getHand() != EquipmentSlot.HAND) return false;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return false;

        // Check if player holds seeds to plant them by offhand interaction.
        ItemStack off = player.getInventory().getItemInOffHand();
        if (!off.getType().isAir() && CROPS.contains(off.getType())) return false;

        // Check if clicked block is a farmland.
        Block blockGround = e.getClickedBlock();
        if (blockGround == null) return false;
        if (blockGround.getType() != Material.FARMLAND && blockGround.getType() != Material.SOUL_SAND) return false;

        // Check enchantment trigger chance.
        if (!this.checkTriggerChance(level)) return false;

        // Check if someting is already growing on the farmland.
        Block blockPlant = blockGround.getRelative(BlockFace.UP);
        if (!blockPlant.isEmpty()) return false;

        // Get the first crops from player's inventory and plant them.
        for (Material seed : CROPS) {
            if (seed == Material.NETHER_WART && blockGround.getType() == Material.SOUL_SAND
                || seed != Material.NETHER_WART && blockGround.getType() == Material.FARMLAND) {
                if (this.takeSeeds(player, seed)) {
                    MessageUtil.sound(player, seed == Material.NETHER_WART ? Sound.ITEM_NETHER_WART_PLANT : Sound.ITEM_CROP_PLANT);
                    plugin.getNMS().sendAttackPacket(player, 0);
                    blockPlant.setType(this.fineSeedsToBlock(seed));
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!this.isReplantOnPlantBreak()) return false;

        Block blockPlant = e.getBlock();
        //if (EnchantTelekinesis.isDropHandled(blockPlant)) return false;
        //if (EnchantRegister.TELEKINESIS != null && item.containsEnchantment(EnchantRegister.TELEKINESIS)) return false;

        // Check if broken block is supported crop(s).
        if (!CROPS.contains(this.fineBlockToSeeds(blockPlant.getType()))) return false;

        // Check if broken block is actually can grow.
        BlockData dataPlant = blockPlant.getBlockData();
        if (!(dataPlant instanceof Ageable plant)) return false;

        // Check enchantment trigger chance.
        if (!this.checkTriggerChance(level)) return false;

        // Check if crop is not at its maximal age to prevent accidient replant.
        /*if (plant.getAge() < plant.getMaximumAge()) {
            e.setCancelled(true);
            return false;
        }*/

        // Replant the gathered crops with a new one.
        if (this.takeSeeds(player, plant.getMaterial())) {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                blockPlant.setType(plant.getMaterial());
                plant.setAge(0);
                blockPlant.setBlockData(plant);
            });
        }
        return true;
    }
}
