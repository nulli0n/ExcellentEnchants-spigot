package su.nightexpress.excellentenchants.manager.enchants.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantChanceTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.CombatEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public class EnchantFireShield extends IEnchantChanceTemplate implements CombatEnchant {

    public static final String ID = "fire_shield";
    public static final String PLACEHOLDER_FIRE_DURATION = "%enchantment_fire_duration%";

    private Scaler fireDuration;

    public EnchantFireShield(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg) {
        super(plugin, cfg, EnchantPriority.MEDIUM);
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
            .replace(PLACEHOLDER_FIRE_DURATION, NumberUtil.format(this.getFireDuration(level)))
        );
    }

    @Override
    public void loadConfig() {
        super.loadConfig();

        this.fireDuration = new EnchantScaler(this, "Settings.Fire.Duration");
    }

    @NotNull
    @Override
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR;
    }

    public double getFireDuration(int level) {
        return this.fireDuration.getValue(level);
    }

    @Override
    public boolean use(@NotNull EntityDamageByEntityEvent e,
                       @NotNull LivingEntity damager, @NotNull LivingEntity victim, @NotNull ItemStack weapon, int level) {
        if (!this.isEnchantmentAvailable(victim)) return false;
        if (!this.checkTriggerChance(level)) return false;
        if (!this.takeCostItem(victim)) return false;

        int fireTicks = (int) (this.getFireDuration(level) * 20);
        damager.setFireTicks(fireTicks);

        return true;
    }
}
