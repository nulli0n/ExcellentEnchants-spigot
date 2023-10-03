package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.Cleanable;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.task.AbstractEnchantmentTask;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class SaturationEnchant extends ExcellentEnchant implements PassiveEnchant, Cleanable {

    public static final String ID = "saturation";

    private static final String PLACEHOLDER_SATURATION_AMOUNT   = "%enchantment_saturation_amount%";
    private static final String PLACEHOLDER_SATURATION_INTERVAL = "%enchantment_saturation_interval%";
    private static final String PLACEHOLDER_SATURATION_MAX_FOOD_LEVEL = "%enchantment_saturation_max_food_level%";

    private long          saturationInterval;
    private EnchantScaler saturationAmount;
    private EnchantScaler saturationMaxFoodLevel;

    private Task task;

    public SaturationEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Restores " + PLACEHOLDER_SATURATION_AMOUNT + " food points every " + PLACEHOLDER_SATURATION_INTERVAL + "s.");
        this.getDefaults().setLevelMax(3);
        this.getDefaults().setTier(0.5);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.saturationInterval = JOption.create("Settings.Saturation.Interval", 100,
            "How often (in ticks) enchantment will have effect? 1 second = 20 ticks.").read(cfg);
        this.saturationAmount = EnchantScaler.read(this, "Settings.Saturation.Amount", Placeholders.ENCHANTMENT_LEVEL,
            "Amount of food points to restore.");
        this.saturationMaxFoodLevel = EnchantScaler.read(this, "Settings.Saturation.Max_Food_Level", "20",
            "Maximal player's food level for the enchantment to stop feeding them.");

        this.addPlaceholder(PLACEHOLDER_SATURATION_AMOUNT, level -> NumberUtil.format(this.getSaturationAmount(level)));
        this.addPlaceholder(PLACEHOLDER_SATURATION_INTERVAL, level -> NumberUtil.format((double) this.saturationInterval / 20D));
        this.addPlaceholder(PLACEHOLDER_SATURATION_MAX_FOOD_LEVEL, level -> NumberUtil.format(this.getMaxFoodLevel(level)));

        this.task = new Task(plugin);
        this.task.start();
    }

    @Override
    public void clear() {
        this.stopTask();
    }

    private void stopTask() {
        if (this.task != null) {
            this.task.stop();
            this.task = null;
        }
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

    public long getSaturationInterval() {
        return saturationInterval;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        if (!(entity instanceof Player player)) return false;
        if (player.getFoodLevel() >= this.getMaxFoodLevel(level)) return false;

        int amount = this.getSaturationAmount(level);
        player.setFoodLevel(Math.min(20, player.getFoodLevel() + amount));
        return true;
    }

    class Task extends AbstractEnchantmentTask {

        public Task(@NotNull ExcellentEnchants plugin) {
            super(plugin, saturationInterval, false);
        }

        @Override
        public void action() {
            for (LivingEntity entity : this.getEntities()) {
                EnchantUtils.getEquipped(entity, SaturationEnchant.class).forEach((item, enchants) -> {
                    enchants.forEach((enchant, level) -> {
                        if (enchant.isOutOfCharges(item)) return;
                        if (enchant.onTrigger(entity, item, level)) {
                            enchant.consumeCharges(item, level);
                        }
                    });
                });
            }
        }
    }
}
