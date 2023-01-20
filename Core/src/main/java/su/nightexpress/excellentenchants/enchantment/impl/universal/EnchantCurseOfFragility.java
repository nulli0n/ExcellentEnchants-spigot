package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

public class EnchantCurseOfFragility extends ExcellentEnchant {

    public static final String ID = "curse_of_fragility";

    public EnchantCurseOfFragility(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemAnvil(PrepareAnvilEvent e) {
        AnvilInventory inventory = e.getInventory();
        ItemStack first = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        boolean cursedFirst = (first != null && EnchantManager.getEnchantmentLevel(first, this) >= 1);
        boolean cursedSecond = (second != null && EnchantManager.getEnchantmentLevel(second, this) >= 1);

        if (cursedFirst || cursedSecond) {
            e.setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemGrindstoneClick(InventoryClickEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;

        this.stopGrindstone(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemGrindstoneDrag(InventoryDragEvent e) {
        Inventory inventory = e.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;

        this.stopGrindstone(inventory);
    }

    private void stopGrindstone(@NotNull Inventory inventory) {
        plugin.getScheduler().runTask(plugin, () -> {
            ItemStack first = inventory.getItem(0);
            ItemStack second = inventory.getItem(1);

            boolean cursedFirst = (first != null && EnchantManager.getEnchantmentLevel(first, this) >= 1);
            boolean cursedSecond = (second != null && EnchantManager.getEnchantmentLevel(second, this) >= 1);

            if (cursedFirst || cursedSecond) {
                inventory.setItem(2, null);
            }
        });
    }

}
