package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.ContainerEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

public class CurseOfFragilityEnchant extends GameEnchantment implements ContainerEnchant {

    public CurseOfFragilityEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getClickPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onClick(@NotNull InventoryClickEvent event, @NotNull Player player, @NotNull ItemStack itemStack, int level) {
        InventoryView view = event.getView();
        if (view.getMenuType() == MenuType.ANVIL || view.getMenuType() == MenuType.GRINDSTONE) {
            event.setCancelled(true);
            return true;
        }

        return false;
    }
}
