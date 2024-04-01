package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.ChanceSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class LuckyMinerEnchant extends AbstractEnchantmentData implements ChanceData, BlockBreakEnchant {

    public static final String ID = "lucky_miner";

    private Modifier           xpModifier;
    private ChanceSettingsImpl chanceSettings;

    public LuckyMinerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(ENCHANTMENT_CHANCE + "% chance to gain " + GENERIC_AMOUNT + "% more XP from ores.");
        this.setMaxLevel(5);
        this.setRarity(Rarity.COMMON);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.chanceSettings = ChanceSettingsImpl.create(config, Modifier.add(25, 7.5, 1, 100));

        this.xpModifier = Modifier.read(config, "Settings.Exp_Modifier",
            Modifier.add(1, 0.5, 1, 5),
            "XP Modifier. Amount of dropped XP will be multiplied on this value."
        );

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPModifier(level) * 100D - 100D));
    }

    @NotNull
    @Override
    public ChanceSettings getChanceSettings() {
        return chanceSettings;
    }

    public double getXPModifier(int level) {
        return this.xpModifier.getValue(level);
    }

    @Override
    @NotNull
    public ItemCategory[] getItemCategories() {
        return new ItemCategory[]{ItemCategory.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        double expMod = this.getXPModifier(level);
        event.setExpToDrop((int) ((double) event.getExpToDrop() * expMod));
        return true;
    }
}
