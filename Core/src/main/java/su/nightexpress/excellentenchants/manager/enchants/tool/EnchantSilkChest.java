package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantDropContainer;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Map;
import java.util.TreeMap;

public class EnchantSilkChest extends IEnchantChanceTemplate implements CustomDropEnchant {

    private final Map<Integer, NamespacedKey> keyItems;

    private String chestName;

    public static final String ID = "silk_chest";

    public EnchantSilkChest(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.HIGH);
        this.keyItems = new TreeMap<>();

        for (int pos = 0; pos < 27; pos++) {
            this.getItemKey(pos);
        }
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chestName = StringUtil.color(cfg.getString("Settings.Chest_Item.Name", "%name% &7(%items% items)"));
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
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

    private NamespacedKey getItemKey(int pos) {
        return this.keyItems.computeIfAbsent(pos, key -> new NamespacedKey(plugin, "silkchest_item_" + pos));
    }

    public boolean isSilkChest(@NotNull ItemStack item) {
        return PDCUtil.getStringData(item, this.getItemKey(0)) != null;
    }

    @NotNull
    public ItemStack getSilkChest(@NotNull Chest chest) {
        ItemStack chestItem = new ItemStack(chest.getType());

        // Store and count chest items.
        int amount = 0;
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

        return chestItem;
    }

    @Override
    public void handleDrop(@NotNull EnchantDropContainer e, @NotNull Player player, @NotNull ItemStack item, int level) {
        BlockDropItemEvent parent = e.getParent();
        BlockState state = parent.getBlockState();
        Block block = state.getBlock();

        if (!this.isEnchantmentAvailable(player)) return;
        if (!(state instanceof Chest chest)) return;
        if (!this.checkTriggerChance(level)) return;
        if (!this.takeCostItem(player)) return;

        // Добавляем в сундук обратно предметы из дроп листа, кроме самого сундука.
        parent.getItems().removeIf(drop -> drop.getItemStack().getType() == state.getType() && drop.getItemStack().getAmount() == 1);
        chest.getBlockInventory().addItem(parent.getItems().stream().map(Item::getItemStack).toList().toArray(new ItemStack[0]));

        // Добавляем кастомный сундук в кастомный дроп лист.
        e.getDrop().add(this.getSilkChest(chest));

        // Очищаем инвентарь сундука и дефолтный дроп лист.
        chest.getBlockInventory().clear();
        parent.getItems().clear();
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
            String data = PDCUtil.getStringData(item, this.getItemKey(pos));
            if (data == null) continue;

            ItemStack itemInv = ItemUtil.fromBase64(data);
            inventory.setItem(pos, itemInv);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestStore(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getType() == InventoryType.CRAFTING || inv.getType() == InventoryType.CREATIVE) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType().isAir()) return;

        if (this.isSilkChest(item)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSilkChestHopper(InventoryPickupItemEvent e) {
        e.setCancelled(this.isSilkChest(e.getItem().getItemStack()));
    }
}
