package su.nightexpress.excellentenchants.manager.listener;

import org.bukkit.GameMode;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.manager.AbstractListener;

public class GenericListener extends AbstractListener<EnchantsPlugin> {

    private final EnchantManager manager;

    public GenericListener(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager) {
        super(plugin);
        this.manager = manager;
    }

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
            this.plugin.runTask(player, player::updateInventory);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChargesFillOnEnchant(EnchantItemEvent event) {
        if (!Config.isChargesEnabled()) return;

        this.plugin.runTask(event.getView().getPlayer(), () -> {
            Inventory inventory = event.getInventory();

            ItemStack result = inventory.getItem(0);
            if (result == null) return;

            event.getEnchantsToAdd().forEach((enchantment, level) -> EnchantUtils.restoreCharges(result, enchantment, level));

            inventory.setItem(0, result);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTickedBlockBreak(BlockBreakEvent event) {
        if (this.manager.removeTickedBlock(event.getBlock())) {
            event.setDropItems(false);
            event.setExpToDrop(0);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTickedBlockTNTExplode(BlockExplodeEvent event) {
        event.blockList().forEach(this.manager::removeTickedBlock);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onTickedBlockEntityExplode(EntityExplodeEvent event) {
        event.blockList().forEach(this.manager::removeTickedBlock);

        if (event.getEntity() instanceof LivingEntity entity) {
            this.manager.handleEnchantExplosion(event, entity);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageByEntityEvent event) {
        DamageSource source = event.getDamageSource();
        DamageType type = source.getDamageType();

        if (type != DamageType.PLAYER_EXPLOSION && type != DamageType.EXPLOSION) return;
        if (!(source.getCausingEntity() instanceof LivingEntity entity)) return;

        this.manager.handleEnchantExplosionDamage(event, entity);
    }
}
