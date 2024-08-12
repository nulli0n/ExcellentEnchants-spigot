package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_CHANCE;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_AMOUNT;

public class LuckyMinerEnchant extends GameEnchantment implements ChanceMeta, BlockBreakEnchant {

    public static final String ID = "lucky_miner";

    private Modifier xpModifier;

    public LuckyMinerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.regular(TradeType.DESERT_COMMON));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            ENCHANTMENT_CHANCE + "% chance to gain " + GENERIC_AMOUNT + "% more XP from ores.",
            EnchantRarity.COMMON,
            5,
            ItemCategories.TOOL,
            ItemCategories.PICKAXE
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setProbability(Probability.create(config, Modifier.add(25, 7.5, 1, 100)));

        this.xpModifier = Modifier.read(config, "Settings.Exp_Modifier",
            Modifier.add(1, 0.5, 1, 5),
            "XP Modifier. Amount of dropped XP will be multiplied on this value."
        );

        this.addPlaceholder(GENERIC_AMOUNT, level -> NumberUtil.format(this.getXPModifier(level) * 100D - 100D));
    }

    public double getXPModifier(int level) {
        return this.xpModifier.getValue(level);
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent event, @NotNull LivingEntity player, @NotNull ItemStack item, int level) {
        if (!this.checkTriggerChance(level)) return false;

        double expMod = this.getXPModifier(level);
        event.setExpToDrop((int) ((double) event.getExpToDrop() * expMod));
        return true;
    }
}
