package su.nightexpress.excellentenchants.enchantment.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;

public class GenericListener extends AbstractListener<EnchantsPlugin> {

    //private final EnchantManager enchantManager;

    public GenericListener(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager) {
        super(plugin);
        //this.enchantManager = enchantManager;
    }

    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDisplayGameMode(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode current = player.getGameMode();
        GameMode changed = event.getNewGameMode();

        // When enter Creative gamemode, force update all inventory to flush item's lore so they don't have enchant descriptions.
        if (changed == GameMode.CREATIVE) {
            EnchantUtils.runInDisabledDisplayUpdate(player, player::updateInventory);
        }
        else if (current == GameMode.CREATIVE) {
            this.plugin.runTask(task -> player.updateInventory());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDisplayQuit(PlayerQuitEvent event) {
        EnchantUtils.allowDisplayUpdate(event.getPlayer());
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChargesFillOnEnchant(EnchantItemEvent event) {
        if (!Config.isChargesEnabled()) return;

        this.plugin.runTask(task -> {
            Inventory inventory = event.getInventory();

            ItemStack result = inventory.getItem(0);
            if (result == null) return;

            event.getEnchantsToAdd().forEach((enchantment, level) -> {
                CustomEnchantment customEnchantment = EnchantRegistry.getByKey(enchantment.getKey());
                if (customEnchantment != null) {
                    customEnchantment.restoreCharges(result, level);
                }
            });

            inventory.setItem(0, result);
        });
    }
}
