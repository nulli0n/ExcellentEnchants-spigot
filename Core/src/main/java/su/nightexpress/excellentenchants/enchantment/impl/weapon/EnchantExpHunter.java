package su.nightexpress.excellentenchants.enchantment.impl.weapon;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.type.DeathEnchant;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantExpHunter extends ExcellentEnchant implements DeathEnchant {

    public static final String ID = "exp_hunter";
    public static final String PLACEHOLDER_EXP_MODIFIER = "%enchantment_exp_modifier%";

    private EnchantScaler expModifier;

    public EnchantExpHunter(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID, EnchantPriority.MEDIUM);
        this.getDefaults().setDescription("Increases exp drop from mobs by " + PLACEHOLDER_EXP_MODIFIER + "%.");
        this.getDefaults().setLevelMax(5);
        this.getDefaults().setTier(0.3);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.expModifier = EnchantScaler.read(this, "Settings.Exp_Modifier",
            "1.0 + " + Placeholders.ENCHANTMENT_LEVEL + " * 0.5",
            "Exp modifier value. The original exp amount will be multiplied on this value.");
    }

    public final double getExpModifier(int level) {
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
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.WEAPON;
    }

    @Override
    public boolean onKill(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, @NotNull Player killer, int level) {
        if (!this.isAvailableToUse(entity)) return false;

        double expModifier = this.getExpModifier(level);
        double expFinal = Math.ceil((double) e.getDroppedExp() * expModifier);

        e.setDroppedExp((int) expFinal);
        return true;
    }

    @Override
    public boolean onDeath(@NotNull EntityDeathEvent e, @NotNull LivingEntity entity, int level) {
        return false;
    }
}
