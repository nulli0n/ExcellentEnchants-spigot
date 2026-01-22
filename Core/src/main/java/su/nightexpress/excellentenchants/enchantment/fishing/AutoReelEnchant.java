package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.EnchantsUtils;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.EnchantContext;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;

import java.nio.file.Path;

public class AutoReelEnchant extends GameEnchantment implements FishingEnchant {

    public AutoReelEnchant(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        super(plugin, manager, file, context);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack itemStack, int level) {
        if (event.getState() != PlayerFishEvent.State.BITE) return false;

        Player player = event.getPlayer();
        EquipmentSlot slot = EnchantsUtils.getItemHand(player, Material.FISHING_ROD);
        if (slot == null) return false;

        this.plugin.runTask(() -> {
            if (event.isCancelled()) return;
            if (!event.getHook().isValid()) return;

            player.swingHand(slot);
            event.getHook().retrieve(slot);
            player.damageItemStack(itemStack, 1);
        });
        return true;
    }
}
