package su.nightexpress.excellentenchants.tooltip;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.nightcore.manager.AbstractListener;

public class TooltipListener extends AbstractListener<EnchantsPlugin> {

    private final TooltipManager manager;

    public TooltipListener(@NotNull EnchantsPlugin plugin, @NotNull TooltipManager manager) {
        super(plugin);
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        GameMode current = player.getGameMode();
        GameMode newGameMode = event.getNewGameMode();

        // When enter Creative gamemode, force update all inventory to flush item's lore so they don't have enchant descriptions.
        if (newGameMode == GameMode.CREATIVE) {
            this.manager.runInStopList(player, player::updateInventory);
        }
        else if (current == GameMode.CREATIVE) {
            this.plugin.runTask(player::updateInventory);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.manager.removeFromUpdateStopList(event.getPlayer());
    }
}
