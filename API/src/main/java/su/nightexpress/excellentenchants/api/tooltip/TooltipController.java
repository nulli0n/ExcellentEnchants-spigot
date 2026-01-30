package su.nightexpress.excellentenchants.api.tooltip;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface TooltipController {

    boolean hasHandler();

    @NotNull ItemStack addDescription(@NotNull ItemStack itemStack);

    boolean isReadyForTooltipUpdate(@NotNull Player player);

    boolean isEnchantTooltipAllowed(@NotNull ItemStack item);

    void addToUpdateStopList(@NotNull Player player);

    void removeFromUpdateStopList(@NotNull Player player);

    void runInStopList(@NotNull Player player, @NotNull Runnable runnable);
}
