package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.placeholder.Replacer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class SilkChestEnchant extends GameEnchantment implements BlockDropEnchant, BlockEnchant {

    private static final String NO_PICKUP = "NO_PICKUP";

    private String       chestName;
    private List<String> chestLore;

    public SilkChestEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chestName = ConfigValue.create("SilkChest.Name", "Chest (" + EnchantsPlaceholders.GENERIC_AMOUNT + " items)",
            "Chest item display name.",
            "Use '" + EnchantsPlaceholders.GENERIC_AMOUNT + "' for items amount."
        ).read(config);

        this.chestLore = ConfigValue.create("SilkChest.Lore", new ArrayList<>(),
            "Chest item lore.",
            "Use '" + EnchantsPlaceholders.GENERIC_AMOUNT + "' for items amount."
        ).read(config);
    }

    @NotNull
    public ItemStack getSilkChest(@NotNull Chest chest) {
        ItemStack chestStack = new ItemStack(chest.getType());

        BlockStateMeta stateMeta = (BlockStateMeta) chestStack.getItemMeta();
        if (stateMeta == null) return chestStack;

        Chest chestItem = (Chest) stateMeta.getBlockState();
        chestItem.getBlockInventory().setContents(chest.getBlockInventory().getContents());
        chestItem.update(true);

        int amount = (int) Stream.of(chestItem.getBlockInventory().getContents()).filter(i -> i != null && !i.getType().isAir()).count();
        Replacer replacer = Replacer.create().replace(EnchantsPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(amount));

        stateMeta.setBlockState(chestItem);
        ItemUtil.setCustomName(stateMeta, replacer.apply(this.chestName));
        ItemUtil.setLore(stateMeta, replacer.apply(this.chestLore));
        chestStack.setItemMeta(stateMeta);

        EnchantUtils.setBlockEnchant(chestStack, this);
        return chestStack;
    }

    @Override
    @NotNull
    public EnchantPriority getDropPriority() {
        return EnchantPriority.LOW;
    }

    @Override
    public boolean canPlaceInContainers() {
        return false;
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        BlockState state = event.getBlockState();

        if (!(state instanceof Chest chest)) return false;

        // Remove original chest from drops to prevent duplication.
        AtomicBoolean originRemoved = new AtomicBoolean(false);
        event.getItems().removeIf(drop -> drop.getItemStack().getType() == state.getType() && drop.getItemStack().getAmount() == 1 && !originRemoved.getAndSet(true));
        // Add chest content back to the chest.
        chest.getBlockInventory().addItem(event.getItems().stream().map(Item::getItemStack).toList().toArray(new ItemStack[0]));
        // Drop nothing of chest content.
        event.getItems().clear();

        if (chest.getBlockInventory().isEmpty()) {
            EnchantUtils.populateResource(event, new ItemStack(chest.getType()));
            return false;
        }

        EnchantUtils.populateResource(event, this.getSilkChest(chest), drop -> drop.setMetadata(NO_PICKUP, new FixedMetadataValue(this.plugin, true)));

        chest.getBlockInventory().clear();

        return true;
    }

    @Override
    public void onPlace(@NotNull BlockPlaceEvent event, @NotNull Player player, @NotNull Block block, @NotNull ItemStack itemStack) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;

        chest.setCustomName(null);
        chest.update(true);
    }
}
