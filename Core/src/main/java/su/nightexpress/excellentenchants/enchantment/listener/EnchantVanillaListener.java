package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantingBridge;
import su.nightexpress.nightcore.manager.AbstractListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnchantVanillaListener extends AbstractListener<EnchantsPlugin> {

    private final Map<UUID, Map<Integer, Map<Enchantment, Integer>>> finedEnchants;

    public EnchantVanillaListener(@NotNull EnchantsPlugin plugin) {
        super(plugin);
        this.finedEnchants = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.finedEnchants.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantTableBridgePurge(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.ENCHANTING) return;

        this.finedEnchants.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnchantTableBridgeSet(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.ENCHANTING) return;

        // I don't know if Mojang is stupid or something
        // they use "enchantment.category.canEnchant(item) instead of enchantment.canEnchant(item)
        // which fuck ups all custom changes in that overridden method.
        // Problem here: EnchantingMenu -> getEnchantmentList -> EnchantmentHelper.selectEnchantment -> getAvailableEnchantmentResults
        // We store enchanting item in EnchantingBridge and adjust custom enchantment's 'isDiscoverable' result depends on the item.
        // Then only proper enchantments will appear from 'getEnchantsList' method of EnchantmentMenu.
        // We store them in a map here and change offers in NMS for best visual result.
        // In EnchantItemEven prepared enchantments are used to replace the result.

        this.plugin.runTask(task -> {
            Player player = (Player) event.getWhoClicked();
            ItemStack target = inventory.getItem(0);
            EnchantingBridge.setEnchantingItem(target);

            if (target != null) {
                Map<Integer, Map<Enchantment, Integer>> map = plugin.getEnchantNMS().getEnchantLists(inventory, target);
                this.finedEnchants.put(player.getUniqueId(), map);

                EnchantingBridge.clear();
            }
            else {
                this.finedEnchants.remove(player.getUniqueId());
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnchantTableFixResult(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        int button = event.whichButton();

        Map<Integer, Map<Enchantment, Integer>> indexMap = this.finedEnchants.remove(player.getUniqueId());
        if (indexMap == null) return;

        Map<Enchantment, Integer> enchantmentMap = indexMap.get(button);
        if (enchantmentMap == null) return;

        event.getEnchantsToAdd().clear();
        event.getEnchantsToAdd().putAll(enchantmentMap);
    }
}
