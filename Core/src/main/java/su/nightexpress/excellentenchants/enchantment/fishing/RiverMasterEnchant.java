package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.entity.FishHook;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

public class RiverMasterEnchant extends GameEnchantment implements FishingEnchant {

    private Modifier distanceMod;

    public RiverMasterEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.distanceMod = Modifier.load(config, "RiverMaster.Distance_Modifier",
            Modifier.addictive(1).perLevel(0.25).capacity(3D),
            "Multiplies the casted fish hook's velocity by specified value.",
            "This does not bypasses the hook distance limits."
        );
    }

    public double getDistanceMod(int level) {
        return this.distanceMod.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.LOWEST;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.FISHING) return false;

        FishHook hook = event.getHook();
        this.plugin.runTask(hook, () -> hook.setVelocity(hook.getVelocity().multiply(this.getDistanceMod(level))));
        return true;
    }
}
