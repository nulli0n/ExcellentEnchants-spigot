package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SoulboundEnchant extends AbstractEnchantmentData implements GenericEnchant, SimpeListener {

    public static final String ID = "soulbound";

    public SoulboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Protects from being dropped on death.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.VERY_RARE);
        this.setConflicts(Enchantment.VANISHING_CURSE.getKey().getKey());
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.BREAKABLE;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(@NotNull PlayerDeathEvent deathEvent) {
        Player player = deathEvent.getEntity();
        if (!this.isAvailableToUse(player)) return;
        if (deathEvent.getKeepInventory()) return;

        List<ItemStack> saveList = new ArrayList<>();
        Location location = player.getLocation();
        World world = player.getWorld();

        deathEvent.getDrops().removeIf(drop -> {
            if (EnchantUtils.getLevel(drop, this.getEnchantment()) > 0) {
                if (this.isOutOfCharges(drop)) return false;

                saveList.add(drop);
                return true;
            }
            return false;
        });

        if (saveList.isEmpty()) return;

        this.plugin.runTask(task -> {
            saveList.forEach(save -> {
                if (player.getInventory().firstEmpty() == -1) {
                    world.dropItemNaturally(location, save);
                }
                else {
                    this.consumeChargesNoUpdate(save, EnchantUtils.getLevel(save, this.getEnchantment()));
                    player.getInventory().addItem(save);
                }
            });
        });
    }
}
