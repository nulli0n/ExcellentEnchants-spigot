package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

public class SaturationEnchant extends GameEnchantment implements PassiveEnchant {

    private Modifier feedAmount;
    private Modifier maxFoodLevel;

    public SaturationEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PERIODIC, Period.ofSeconds(15));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.feedAmount = Modifier.load(config, "Saturation.Amount",
            Modifier.addictive(0).perLevel(1).capacity(5),
            "Amount of food points to restore.");

        this.maxFoodLevel = Modifier.load(config, "Saturation.Max_Food_Level",
            Modifier.addictive(20),
            "Max. food level where saturation stops.");

        this.addPlaceholder(EnchantsPlaceholders.GENERIC_AMOUNT, level -> NumberUtil.format(this.getFeedAmount(level)));
        this.addPlaceholder(EnchantsPlaceholders.GENERIC_MAX, level -> NumberUtil.format(this.getMaxFoodLevel(level)));
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
