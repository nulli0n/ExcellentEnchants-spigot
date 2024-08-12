package su.nightexpress.excellentenchants.enchantment.impl.universal;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.type.GenericEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SoulboundEnchant extends GameEnchantment implements GenericEnchant, SimpeListener {

    public static final String ID = "soulbound";

    public SoulboundEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.TAIGA_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Protects from being dropped on death.",
            EnchantRarity.MYTHIC,
            1,
            ItemCategories.BREAKABLE,
            Lists.newSet(BukkitThing.toString(Enchantment.VANISHING_CURSE))
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

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
            if (EnchantUtils.getLevel(drop, this.getBukkitEnchantment()) > 0) {
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
                    this.consumeChargesNoUpdate(save, EnchantUtils.getLevel(save, this.getBukkitEnchantment()));
                    player.getInventory().addItem(save);
                }
            });
        });
    }
}
