package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class AutoReelEnchant extends GameEnchantment implements FishingEnchant {

    public static final String ID = "auto_reel";

    public AutoReelEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Automatically reels in a hook on bite.",
            EnchantRarity.MYTHIC,
            1,
            ItemCategories.FISHING_ROD
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.BITE) return false;

        Player player = event.getPlayer();
        EquipmentSlot slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
        if (slot == null) return false;

        int id = slot == EquipmentSlot.HAND ? 0 : 3;

        this.plugin.runTask(task -> {
            if (event.isCancelled()) return;
            if (!event.getHook().isValid()) return;

            plugin.getEnchantNMS().sendAttackPacket(event.getPlayer(), id);
            plugin.getEnchantNMS().retrieveHook(event.getHook(), item, slot);
        });
        return true;
    }
}
