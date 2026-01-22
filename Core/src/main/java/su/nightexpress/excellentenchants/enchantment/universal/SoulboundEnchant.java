package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.type.InventoryEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

public class SoulboundEnchant extends GameEnchantment implements InventoryEnchant {

    public SoulboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    public boolean onDeath(@NotNull PlayerDeathEvent event, @NotNull Player player, ItemStack itemStack, int level) {
        if (event.getKeepInventory()) return false;

        event.getDrops().remove(itemStack);
        event.getItemsToKeep().add(itemStack);
        return true;
    }
}
