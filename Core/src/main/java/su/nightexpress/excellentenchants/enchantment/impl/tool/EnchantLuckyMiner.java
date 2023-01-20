package su.nightexpress.excellentenchants.enchantment.impl.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;

import java.util.function.UnaryOperator;

public class EnchantLuckyMiner extends ExcellentEnchant implements Chanced, BlockBreakEnchant {

    public static final String ID = "lucky_miner";
    private static final String PLACEHOLDER_EXP_MODIFIER = "%enchantment_exp_modifier%";

    private EnchantScaler expModifier;
    private ChanceImplementation chanceImplementation;

    public EnchantLuckyMiner(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.chanceImplementation = ChanceImplementation.create(this);
        this.expModifier = EnchantScaler.read(this, "Settings.Exp_Modifier", "1.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.5",
            "Exp modifier value. The original exp amount will be multiplied on this value.");
    }

    @NotNull
    @Override
    public ChanceImplementation getChanceImplementation() {
        return chanceImplementation;
    }

    public double getExpModifier(int level) {
        return this.expModifier.getValue(level);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str)
            .replace(PLACEHOLDER_EXP_MODIFIER, NumberUtil.format(this.getExpModifier(level) * 100D - 100D))
        ;
    }

    @Override
    @NotNull
    public FitItemType[] getFitItemTypes() {
        return new FitItemType[]{FitItemType.PICKAXE};
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.TOOL;
    }

    @Override
    public boolean onBreak(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (!this.isAvailableToUse(player)) return false;
        if (!this.checkTriggerChance(level)) return false;

        double expMod = this.getExpModifier(level);
        e.setExpToDrop((int) ((double) e.getExpToDrop() * expMod));
        return true;
    }
}
