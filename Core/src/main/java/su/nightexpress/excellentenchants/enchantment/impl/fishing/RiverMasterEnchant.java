package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
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

import java.io.File;

public class RiverMasterEnchant extends GameEnchantment implements GenericEnchant, SimpeListener {

    public static final String ID = "river_master";

    private Modifier distanceMod;

    public RiverMasterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Increases casting distance.",
            EnchantRarity.COMMON,
            5,
            ItemCategories.FISHING_ROD
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.distanceMod = Modifier.read(config, "Settings.Distance_Modifier",
            Modifier.add(1, 0.25, 1, 3D),
            "Multiplies the casted fish hook's velocity by specified value.",
            "Setting too high values will result in hook auto removal by vanilla game/server mechanics."
        );
    }

    public double getDistanceMod(int level) {
        return this.distanceMod.getValue(level);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHookLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) return;
        if (!(hook.getShooter() instanceof Player player)) return;

        EquipmentSlot slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
        if (slot == null) return;

        ItemStack rod = player.getInventory().getItem(slot);
        if (rod == null) return;

        int level = EnchantUtils.getLevel(rod, this.getBukkitEnchantment());
        if (level < 1) return;
        if (this.isOutOfCharges(rod)) return;

        hook.setVelocity(hook.getVelocity().multiply(this.getDistanceMod(level)));

        this.consumeCharges(rod, level);
    }
}
