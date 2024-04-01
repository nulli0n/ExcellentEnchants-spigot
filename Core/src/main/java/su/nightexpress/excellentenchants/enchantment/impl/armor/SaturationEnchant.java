package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.PeriodicSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.PeriodSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class SaturationEnchant extends AbstractEnchantmentData implements PassiveEnchant {

    public static final String ID = "saturation";

    private Modifier feedAmount;
    private Modifier maxFoodLevel;

    private PeriodSettingsImpl periodSettings;

    public SaturationEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Restores " + GENERIC_AMOUNT + " food points every few seconds.");
        this.setMaxLevel(3);
        this.setRarity(Rarity.RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.periodSettings = PeriodSettingsImpl.create(config);

        this.feedAmount = Modifier.read(config, "Settings.Saturation.Amount",
            Modifier.add(0, 1, 1, 10),
            "Amount of food points to restore.");

        this.maxFoodLevel = Modifier.read(config, "Settings.Saturation.Max_Food_Level",
            Modifier.add(20, 0, 0),
            "Maximal player's food level for the enchantment to stop feeding them.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getFeedAmount(level)));
        this.addPlaceholder(GENERIC_MAX, level -> NumberUtil.format(this.getMaxFoodLevel(level)));
    }

    @NotNull
    @Override
    public PeriodicSettings getPeriodSettings() {
        return periodSettings;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    public final int getFeedAmount(int level) {
        return (int) this.feedAmount.getValue(level);
    }

    public final int getMaxFoodLevel(int level) {
        return (int) this.maxFoodLevel.getValue(level);
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (player.getFoodLevel() >= this.getMaxFoodLevel(level)) return false;

        int amount = this.getFeedAmount(level);
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + amount));
        return true;
    }
}
