package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;

public class AutoFishEnchant extends ExcellentEnchant implements FishingEnchant {

    public static final String ID = "auto_fish";

    public AutoFishEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("Automatically reels in a hook on bite.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(1.0);
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.BITE) return false;

        this.plugin.runTask(task -> {
            if (event.isCancelled()) return;

            plugin.getEnchantNMS().sendAttackPacket(event.getPlayer(), 0);
            plugin.getEnchantNMS().retrieveHook(event.getHook(), item);
        });
        return true;
    }
}
