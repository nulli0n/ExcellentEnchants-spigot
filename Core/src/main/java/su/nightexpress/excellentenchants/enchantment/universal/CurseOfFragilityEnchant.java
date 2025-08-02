package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.ContainerEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class CurseOfFragilityEnchant extends GameEnchantment implements ContainerEnchant {

    public CurseOfFragilityEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
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
