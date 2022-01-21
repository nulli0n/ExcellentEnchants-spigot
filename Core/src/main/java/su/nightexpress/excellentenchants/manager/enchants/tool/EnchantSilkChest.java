package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
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
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.CustomDropEnchant;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EnchantSilkChest extends IEnchantChanceTemplate implements BlockBreakEnchant, CustomDropEnchant {

    private final Map<Integer, NamespacedKey> keyItems;
    private final String                      chestName;

    public static final String ID = "silk_chest";

    public EnchantSilkChest(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
        this.keyItems = new TreeMap<>();
        this.chestName = StringUtil.color(cfg.getString("Settings.Chest_Item.Name", "%name% &7(%items% items)"));

        for (int pos = 0; pos < 27; pos++) {
            this.getItemKey(pos);
        }
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
        Block block = chest.getBlock();
        ItemStack chestItem = new ItemStack(block.getType());

        // Store and count chest items.
        int amount = 0;
        int count = 0;
        for (ItemStack itemInv : chest.getBlockInventory().getContents()) {
            if (itemInv == null) itemInv = new ItemStack(Material.AIR);
            else amount++;

            String base64 = ItemUtil.toBase64(itemInv);
            if (base64 == null) continue;
            if (base64.length() >= Short.MAX_VALUE) {
                block.getWorld().dropItemNaturally(block.getLocation(), itemInv);
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
    @NotNull
    public List<ItemStack> getCustomDrops(@NotNull Player player, @NotNull ItemStack item, @NotNull Block block, int level) {
        if (block.getType() == Material.ENDER_CHEST) return Collections.emptyList();
        if (!(block.getState() instanceof Chest chest)) return Collections.emptyList();

        return Collections.singletonList(this.getSilkChest(chest));
    }

    @Override
    public boolean isEventMustHaveDrops() {
        return true;
    }

    @Override
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        Block block = e.getBlock();
        if (!this.isEnchantmentAvailable(player)) return false;
        if (EnchantTelekinesis.isDropHandled(block)) return false;
        if (block.getType() == Material.ENDER_CHEST) return false;
        if (this.isEventMustHaveDrops() && !e.isDropItems()) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!(block.getState() instanceof Chest chest)) return false;
        if (!this.takeCostItem(player)) return false;

        // Drop custom chest and do not drop the original one.
        this.getCustomDrops(player, item, block, level).forEach(chestItem -> block.getWorld().dropItemNaturally(block.getLocation(), chestItem));

        // Do not drop chest items.
        chest.getBlockInventory().clear();
        e.setDropItems(false);

        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSilkChestPlace(BlockPlaceEvent e) {
        ItemStack item = e.getItemInHand();
        if (item.getType().isAir()) return;

        Block block = e.getBlockPlaced();
        BlockState state = block.getState();
        if (!(state instanceof Chest chest)) return;

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
