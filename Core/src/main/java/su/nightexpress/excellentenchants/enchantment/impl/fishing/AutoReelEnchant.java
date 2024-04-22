package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.Material;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class AutoReelEnchant extends AbstractEnchantmentData implements FishingEnchant {

    public static final String ID = "auto_reel";

    public AutoReelEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Automatically reels in a hook on bite.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.VERY_RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
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

            plugin.getEnchantNMS().sendAttackPacket(event.getPlayer(), id);
            plugin.getEnchantNMS().retrieveHook(event.getHook(), item, slot);
        });
        return true;
    }
}
