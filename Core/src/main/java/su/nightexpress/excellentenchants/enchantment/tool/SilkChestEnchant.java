package su.nightexpress.excellentenchants.enchantment.tool;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SilkChestEnchant extends GameEnchantment implements BlockDropEnchant, BlockEnchant {

    private final NamespacedKey chestNameKey;

    private String       chestName;
    private List<String> chestLore;

    public SilkChestEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
        this.chestNameKey = new NamespacedKey(plugin, "silkchest.original_name");
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chestName = ConfigValue.create("SilkChest.Name", EnchantsPlaceholders.GENERIC_NAME + " (" + EnchantsPlaceholders.GENERIC_AMOUNT + " items)",
            "Chest item display name.",
            "Use '" + EnchantsPlaceholders.GENERIC_AMOUNT + "' for items amount."
        ).read(config);

        this.chestLore = ConfigValue.create("SilkChest.Lore", new ArrayList<>(),
            "Chest item lore.",
            "Use '" + EnchantsPlaceholders.GENERIC_AMOUNT + "' for items amount."
        ).read(config);
    }

    @NotNull
    public ItemStack getSilkChest(@NotNull Chest originChest) {
        ItemStack chestStack = new ItemStack(originChest.getType());

        ItemUtil.editMeta(chestStack, BlockStateMeta.class, stateMeta -> {
            String originName = EntityUtil.getNameSerialized(originChest);

            Chest chestItem = (Chest) stateMeta.getBlockState();
            chestItem.getBlockInventory().setContents(originChest.getBlockInventory().getContents());
            chestItem.update(true);

            stateMeta.setBlockState(chestItem);

            PlaceholderContext placeholderContext = PlaceholderContext.builder()
                .with(EnchantsPlaceholders.GENERIC_NAME, () -> EntityUtil.getNameSerialized(originChest))
                .with(EnchantsPlaceholders.GENERIC_AMOUNT, () -> String.valueOf(Stream.of(chestItem.getBlockInventory().getContents()).filter(i -> i != null && !i.getType().isAir()).count()))
                .build();

            PDCUtil.set(stateMeta, this.chestNameKey, originName);

            ItemUtil.setCustomName(stateMeta, placeholderContext.apply(this.chestName));
            ItemUtil.setLore(stateMeta, placeholderContext.apply(this.chestLore));
        });

        this.manager.setBlockEnchant(chestStack, this);

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
    public boolean onDrop(@NotNull BlockDropItemEvent event, @NotNull LivingEntity player, @NotNull ItemStack itemStack, int level) {
        BlockState state = event.getBlockState();
        if (!(state instanceof Chest chest)) return false;

        Material blockType = state.getType();
        List<Item> drops = event.getItems();

        // If no original chest block is being dropped, then ignore.
        Item originalContainerItem = drops.stream().filter(drop -> drop.getItemStack().getType() == blockType && drop.getItemStack().getAmount() == 1).findFirst().orElse(null);

        if (originalContainerItem == null) return false;
        if (drops.size() == 1) return false;

        // Remove original chest from drops to prevent duplication.
        drops.remove(originalContainerItem);

        // Add chest content back to the chest.
        chest.getBlockInventory().addItem(drops.stream().map(Item::getItemStack).toList().toArray(new ItemStack[0]));

        // Drop nothing of chest content.
        drops.clear();

        EnchantsUtils.populateResource(event, this.getSilkChest(chest));

        chest.getBlockInventory().clear();

        return true;
    }

    @Override
    public void onPlace(@NotNull BlockPlaceEvent event, @NotNull Player player, @NotNull Block block, @NotNull ItemStack itemStack) {
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;

        String name = PDCUtil.getString(itemStack, this.chestNameKey).orElse(null);

        EntityUtil.setCustomName(chest, name);
        chest.update(true);
    }
}
