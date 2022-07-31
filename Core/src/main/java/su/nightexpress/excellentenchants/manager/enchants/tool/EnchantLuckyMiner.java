package su.nightexpress.excellentenchants.manager.enchants.tool;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.BlockBreakEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;
import su.nightexpress.excellentenchants.manager.type.FitItemType;

import java.util.function.UnaryOperator;

public class EnchantLuckyMiner extends IEnchantChanceTemplate implements BlockBreakEnchant {

    private Scaler expModifier;

    public static final String ID = "lucky_miner";

    private static final String PLACEHOLDER_EXP_MODIFIER = "%enchantment_exp_modifier%";

    public EnchantLuckyMiner(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.expModifier = new EnchantScaler(this, "Settings.Exp_Modifier");
    }

    public double getExpModifier(int level) {
        return this.expModifier.getValue(level);
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_EXP_MODIFIER, NumberUtil.format(this.getExpModifier(level) * 100D - 100D))
        );
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
    public boolean use(@NotNull BlockBreakEvent e, @NotNull Player player, @NotNull ItemStack item, int level) {
        if (e.isCancelled()) return false;
        if (!this.isEnchantmentAvailable(player)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(player)) return false;

        double expMod = this.getExpModifier(level);
        e.setExpToDrop((int) ((double) e.getExpToDrop() * expMod));
        return true;
    }
}
