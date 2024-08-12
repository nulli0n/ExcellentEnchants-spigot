package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_MAX;

public class SaturationEnchant extends GameEnchantment implements PassiveEnchant {

    public static final String ID = "saturation";

    private Modifier feedAmount;
    private Modifier maxFoodLevel;

    public SaturationEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.SAVANNA_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Restores " + GENERIC_AMOUNT + " food points every few seconds.",
            EnchantRarity.LEGENDARY,
            3,
            ItemCategories.HELMET
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setPeriod(Period.create(config));

        this.feedAmount = Modifier.read(config, "Settings.Saturation.Amount",
            Modifier.add(0, 1, 1, 10),
            "Amount of food points to restore.");

        this.maxFoodLevel = Modifier.read(config, "Settings.Saturation.Max_Food_Level",
            Modifier.add(20, 0, 0),
            "Maximal player's food level for the enchantment to stop feeding them.");

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getFeedAmount(level)));
        this.addPlaceholder(GENERIC_MAX, level -> NumberUtil.format(this.getMaxFoodLevel(level)));
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
