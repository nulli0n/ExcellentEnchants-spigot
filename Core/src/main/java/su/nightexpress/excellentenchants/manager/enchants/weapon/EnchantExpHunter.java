package su.nightexpress.excellentenchants.manager.enchants.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantExpHunter extends IEnchantChanceTemplate implements DeathEnchant {

    private Scaler expModifier;

    public static final String ID = "exp_hunter";
    public static final String PLACEHOLDER_EXP_MODIFIER = "%enchantment_exp_modifier%";

    public EnchantExpHunter(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.expModifier = new EnchantScaler(this, "Settings.Exp_Modifier");
    }

    public final double getExpModifier(int level) {
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
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean use(@NotNull EntityDeathEvent e, @NotNull LivingEntity dead, int level) {
        if (!this.isEnchantmentAvailable(dead)) return false;

        Player killer = dead.getKiller();
        if (killer == null) return false;

        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(killer)) return false;

        double expModifier = this.getExpModifier(level);
        double expFinal = Math.ceil((double) e.getDroppedExp() * expModifier);

        e.setDroppedExp((int) expFinal);
        return true;
    }
}
