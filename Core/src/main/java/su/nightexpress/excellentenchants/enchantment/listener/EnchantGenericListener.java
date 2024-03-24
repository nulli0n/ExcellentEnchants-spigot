package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;

public class EnchantGenericListener extends AbstractListener<ExcellentEnchantsPlugin> {

    //private final EnchantManager enchantManager;

    public EnchantGenericListener(@NotNull ExcellentEnchantsPlugin plugin, @NotNull EnchantManager enchantManager) {
        super(plugin);
        //this.enchantManager = enchantManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        EnchantUtils.setSpawnReason(event.getEntity(), event.getSpawnReason());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEnchantedProjectileShoot(EntityShootBowEvent event) {
        if (event.getProjectile() instanceof Projectile projectile) {
            EnchantUtils.addEnchantedProjectile(projectile, event.getBow());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnchantedProjectileLand(ProjectileHitEvent event) {
        this.plugin.runTask(task -> {
            EnchantUtils.removeEnchantedProjectile(event.getEntity());
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantDisplayUpdateGrindstoneClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;
        if (event.getRawSlot() == 2) return;

        this.updateGrindstone(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantDisplayUpdateGrindstoneDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getType() != InventoryType.GRINDSTONE) return;

        this.updateGrindstone(inventory);
    }

    private void updateGrindstone(@NotNull Inventory inventory) {
        this.plugin.runTask(task -> {
            ItemStack result = inventory.getItem(2);
            if (result == null || result.getType().isAir()) return;

            EnchantUtils.updateDisplay(result);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantDisplayUpdatePickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Item item = event.getItem();
        ItemStack itemStack = item.getItemStack();
        if (EnchantUtils.updateDisplay(itemStack)) {
            item.setItemStack(itemStack);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnchantDisplayUpdateEnchanting(EnchantItemEvent event) {
        this.plugin.runTask(task -> {
            Inventory inventory = event.getInventory();

            ItemStack result = inventory.getItem(0);
            if (result == null) return;

            event.getEnchantsToAdd().forEach((enchantment, level) -> {
                EnchantmentData enchant = EnchantRegistry.getByKey(enchantment.getKey());
                if (enchant != null) {
                    enchant.restoreCharges(result, level);
                }
            });
            EnchantUtils.updateDisplay(result);

            inventory.setItem(0, result);
        });
    }
}
