package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockDropEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantDropContainer;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

public class EnchantSilkChest extends ExcellentEnchant implements BlockDropEnchant {

    public static final String ID = "silk_chest";

    private       String                      chestName;
    private       List<String>                chestLore;
    private final NamespacedKey               keyChest;
    @Deprecated private final Map<Integer, NamespacedKey> keyItems;

    public EnchantSilkChest(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.HIGH);
        this.getDefaults().setDescription("Drop chests and saves all its content.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.5);

        this.keyChest = new NamespacedKey(plugin, ID + ".item");
        this.keyItems = new TreeMap<>();
        for (int pos = 0; pos < 27; pos++) {
            this.getItemKey(pos);
        }
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chestName = JOption.create("Settings.Chest_Item.Name", "Chest &7(" + Placeholders.GENERIC_AMOUNT + " items)",
            "Chest item display name.",
            "Use '" + Placeholders.GENERIC_AMOUNT + "' for items amount.").mapReader(Colorizer::apply).read(cfg);
        this.chestLore = JOption.create("Settings.Chest_Item.Lore", new ArrayList<>(),
            "Chest item lore.",
            "Use '" + Placeholders.GENERIC_AMOUNT + "' for items amount.").mapReader(Colorizer::apply).read(cfg);
    }

    @Deprecated
    private NamespacedKey getItemKey(int pos) {
        return this.keyItems.computeIfAbsent(pos, key -> new NamespacedKey(plugin, "silkchest_item_" + pos));
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.AXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    public boolean isSilkChest(@NotNull ItemStack item) {
        return PDCUtil.getBoolean(item, this.keyChest).orElse(false) || PDCUtil.getString(item, this.getItemKey(0)).orElse(null) != null;
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

        stateMeta.setBlockState(chestItem);
        stateMeta.setDisplayName(this.chestName);
        stateMeta.setLore(this.chestLore);
        chestStack.setItemMeta(stateMeta);

        ItemUtil.replace(chestStack, str -> str.replace(Placeholders.GENERIC_AMOUNT, String.valueOf(amount)));
        PDCUtil.set(chestStack, this.keyChest, true);
        return chestStack;

        // Store and count chest items.
        /*int amount = 0;
        int count = 0;
        for (ItemStack itemInv : chest.getBlockInventory().getContents()) {
            if (itemInv == null) itemInv = new ItemStack(Material.AIR);
            else amount++;

            String base64 = ItemUtil.toBase64(itemInv);
            if (base64 == null) continue;
            if (base64.length() >= Short.MAX_VALUE) {
                chest.getWorld().dropItemNaturally(chest.getLocation(), itemInv);
                continue;
            }
            PDCUtil.setData(chestItem, this.getItemKey(count++), base64);
        }

        // Apply item meta name and items data string.
        ItemMeta meta = chestItem.getItemMeta();
        if (meta != null) {
            String nameOrig = ItemUtil.getItemName(chestItem);
            String nameChest = this.chestName.replace("%name%", nameOrig).replace("%items%", String.valueOf(amount));
            meta.setDisplayName(nameChest);
            chestItem.setItemMeta(meta);
        }

        return chestItem;*/
    }

    @Override
    public boolean onDrop(@NotNull BlockDropItemEvent e, @NotNull EnchantDropContainer dropContainer, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockState state = e.getBlockState();
        Block block = state.getBlock();

        if (!this.isAvailableToUse(player)) return false;
        if (!(state instanceof Chest chest)) return false;

        // Добавляем в сундук обратно предметы из дроп листа, кроме самого сундука.
        e.getItems().removeIf(drop -> drop.getItemStack().getType() == state.getType() && drop.getItemStack().getAmount() == 1);
        chest.getBlockInventory().addItem(e.getItems().stream().map(Item::getItemStack).toList().toArray(new ItemStack[0]));

        // Добавляем кастомный сундук в кастомный дроп лист.
        dropContainer.getDrop().add(this.getSilkChest(chest));

        // Очищаем инвентарь сундука и дефолтный дроп лист.
        chest.getBlockInventory().clear();
        e.getItems().clear();

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSilkChestPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (item.getType().isAir()) return;

        Block block = e.getBlockPlaced();
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;

        chest.setCustomName(null);
        chest.update(true);

        Inventory inventory = chest.getBlockInventory();
        for (int pos = 0; pos < inventory.getSize(); pos++) {
            String data = PDCUtil.getString(item, this.getItemKey(pos)).orElse(null);
            if (data == null) continue;

            ItemStack itemInv = ItemUtil.fromBase64(data);
            inventory.setItem(pos, itemInv);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestStore(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() == InventoryType.CRAFTING || inventory.getType() == InventoryType.CREATIVE) return;

        Player player = (Player) e.getWhoClicked();
        ItemStack item;
        if (e.getHotbarButton() >= 0) {
            item = player.getInventory().getItem(e.getHotbarButton());
        }
        else item = e.getCurrentItem();

        if (item == null || item.getType().isAir() || !this.isSilkChest(item)) return;

        Inventory clicked = e.getClickedInventory();
        if (e.getClick() != ClickType.NUMBER_KEY) {
            if (clicked != null && clicked.equals(e.getView().getTopInventory())) return;
        }

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestHopper(InventoryPickupItemEvent e) {
        e.setCancelled(this.isSilkChest(e.getItem().getItemStack()));
    }
}
