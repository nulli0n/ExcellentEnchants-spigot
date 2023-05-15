package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class RiverMasterEnchant extends ExcellentEnchant {

    public static final String ID = "river_master";

    private EnchantScaler distanceMod;

    public RiverMasterEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("Increases casting distance.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.distanceMod = EnchantScaler.read(this, "Settings.Distance_Modifier",
            "1.25 + " + Placeholders.ENCHANTMENT_LEVEL + " / 5",
            "Multiplies the casted fish hook's velocity by specified value.",
            "Setting too high values will result in hook auto removal by vanilla game/server mechanics.");
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.FISHING_ROD;
    }

    public double getDistanceMod(int level) {
        return this.distanceMod.getValue(level);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHookLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) return;
        if (!(hook.getShooter() instanceof Player player)) return;
        if (!this.isAvailableToUse(player)) return;

        ItemStack rod = EnchantUtils.getFishingRod(player);
        if (rod == null) return;

        int level = EnchantUtils.getLevel(rod, this);
        if (level < 1) return;

        if (this.isOutOfCharges(rod)) return;

        hook.setVelocity(hook.getVelocity().multiply(this.getDistanceMod(level)));

        this.consumeCharges(rod);
    }
}
