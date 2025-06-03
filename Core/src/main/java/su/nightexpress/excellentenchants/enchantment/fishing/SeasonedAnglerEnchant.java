package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class SeasonedAnglerEnchant extends GameEnchantment implements FishingEnchant {

    private Modifier xpModifier;

    public SeasonedAnglerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.xpModifier = Modifier.load(config, "SeasonedAngler.XP_Modifier",
            Modifier.addictive(0).perLevel(50).capacity(300),
            "Amount (in percent) of additional XP from fishing.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPPercent(level)));
    }

    public int getXPPercent(int level) {
        return (int) this.xpModifier.getValue(level);
    }

    @Override
    @NotNull
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.NORMAL;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (event.getExpToDrop() == 0) return false;

        int expDrop = event.getExpToDrop();
        int expPercent = this.getXPPercent(level);
        int expModified = (int) Math.ceil(expDrop * (1D + expPercent / 100D));

        event.setExpToDrop(expModified);
        return true;
    }
}
