package su.nightexpress.excellentenchants.enchantment.universal;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.enchantment.type.InventoryEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Players;

import java.io.File;

public class SoulboundEnchant extends GameEnchantment implements InventoryEnchant {

    public SoulboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    public boolean onDeath(@NotNull PlayerDeathEvent event, @NotNull Player player, ItemStack itemStack, int level) {
        if (event.getKeepInventory()) return false;

        event.getDrops().remove(itemStack);
        this.plugin.runTask(event.getEntity(), () -> Players.addItem(event.getEntity(), itemStack));
        return true;
    }
}
