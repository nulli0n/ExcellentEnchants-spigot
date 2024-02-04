package su.nightexpress.excellentenchants.enchantment.impl.fishing;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;

public class SeasonedAnglerEnchant extends ExcellentEnchant implements FishingEnchant {

    public static final String ID = "seasoned_angler";

    private EnchantScaler expMod;

    public SeasonedAnglerEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Increases amount of XP gained from fishing by " + Placeholders.GENERIC_AMOUNT + "%.");
        this.getDefaults().setLevelMax(4);
        this.getDefaults().setTier(0.1);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.expMod = EnchantScaler.read(this, "Settings.Exp_Percent",
            "25.0 * " + Placeholders.ENCHANTMENT_LEVEL,
            "Amount (in percent) of additional XP from fishing.");

        this.addPlaceholder(Placeholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getExpPercent(level)));
    }

    public int getExpPercent(int level) {
        return (int) this.expMod.getValue(level);
    }

    @NotNull
    @Override
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.FISHING_ROD;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack item, int level) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return false;
        if (event.getExpToDrop() == 0) return false;

        int expDrop = event.getExpToDrop();
        int expPercent = this.getExpPercent(level);
        int expModified = (int) Math.ceil(expDrop * (1D + expPercent / 100D));

        event.setExpToDrop(expModified);
        return true;
    }
}
