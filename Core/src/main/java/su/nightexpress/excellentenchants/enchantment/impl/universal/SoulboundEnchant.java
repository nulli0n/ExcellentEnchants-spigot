package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.ArrayList;
import java.util.List;

public class SoulboundEnchant extends ExcellentEnchant {

    public static final String ID = "soulbound";

    public SoulboundEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.LOWEST);
        this.getDefaults().setDescription("Protects from being dropped on death.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.8);
        this.getDefaults().setConflicts(Enchantment.VANISHING_CURSE.getKey().getKey());
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent deathEvent) {
        Player player = deathEvent.getEntity();
        if (!this.isAvailableToUse(player)) return;
        if (deathEvent.getKeepInventory()) return;

        List<ItemStack> saveList = new ArrayList<>();

        deathEvent.getDrops().removeIf(drop -> {
            if (EnchantUtils.getLevel(drop, this) > 0) {
                if (this.isOutOfCharges(drop)) return false;

                saveList.add(drop);
                return true;
            }
            return false;
        });

        if (saveList.isEmpty()) return;

        this.plugin.runTask(task -> {
            saveList.forEach(save -> {
                this.consumeCharges(save, EnchantUtils.getLevel(save, this));
                player.getInventory().addItem(save);
            });
        });
    }
}
