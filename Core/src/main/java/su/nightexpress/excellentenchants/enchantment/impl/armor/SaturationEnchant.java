package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PeriodImplementation;

public class SaturationEnchant extends ExcellentEnchant implements PassiveEnchant {

    public static final String ID = "saturation";

    private static final String PLACEHOLDER_SATURATION_AMOUNT   = "%enchantment_saturation_amount%";
    private static final String PLACEHOLDER_SATURATION_MAX_FOOD_LEVEL = "%enchantment_saturation_max_food_level%";

    private EnchantScaler saturationAmount;
    private EnchantScaler saturationMaxFoodLevel;

    private PeriodImplementation periodImplementation;

    public SaturationEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Restores " + PLACEHOLDER_SATURATION_AMOUNT + " food points every few seconds.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.periodImplementation = PeriodImplementation.create(this, "100");

        this.saturationAmount = EnchantScaler.read(this, "Settings.Saturation.Amount", Placeholders.ENCHANTMENT_LEVEL,
            "Amount of food points to restore.");
        this.saturationMaxFoodLevel = EnchantScaler.read(this, "Settings.Saturation.Max_Food_Level", "20",
            "Maximal player's food level for the enchantment to stop feeding them.");

        this.addPlaceholder(PLACEHOLDER_SATURATION_AMOUNT, level -> NumberUtil.format(this.getSaturationAmount(level)));
        this.addPlaceholder(PLACEHOLDER_SATURATION_MAX_FOOD_LEVEL, level -> NumberUtil.format(this.getMaxFoodLevel(level)));
    }

    @NotNull
    @Override
    public PeriodImplementation getPeriodImplementation() {
        return periodImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    public final int getSaturationAmount(int level) {
        return (int) this.saturationAmount.getValue(level);
    }

    public final int getMaxFoodLevel(int level) {
        return (int) this.saturationMaxFoodLevel.getValue(level);
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (player.getFoodLevel() >= this.getMaxFoodLevel(level)) return false;

        int amount = this.getSaturationAmount(level);
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + amount));
        return true;
    }
}
