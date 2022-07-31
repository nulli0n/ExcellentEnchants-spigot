package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.manager.object.EnchantScaler;

import java.util.function.UnaryOperator;

public abstract class IEnchantChanceTemplate extends ExcellentEnchant {

    public static final String PLACEHOLDER_CHANCE = "%enchantment_trigger_chance%";

    protected Scaler triggerChance;

    public IEnchantChanceTemplate(@NotNull ExcellentEnchants plugin, @NotNull JYML cfg, @NotNull EnchantPriority priority) {
        super(plugin, cfg, priority);
    }

    @Override
    public void loadConfig() {
        super.loadConfig();
        this.triggerChance = new EnchantScaler(this, "Settings.Trigger_Chance");
    }

    @Override
    protected void updateConfig() {
        super.updateConfig();

        if (cfg.contains("settings.enchant-trigger-chance")) {
            String triggerChance = cfg.getString("settings.enchant-trigger-chance", "100").replace("%level%", PLACEHOLDER_LEVEL);

            cfg.set("Settings.Trigger_Chance", triggerChance);
            cfg.set("settings.enchant-trigger-chance", null);
        }
    }

    @Override
    public @NotNull UnaryOperator<String> replacePlaceholders(int level) {
        return str -> super.replacePlaceholders(level).apply(str
                .replace(PLACEHOLDER_CHANCE, NumberUtil.format(this.getTriggerChance(level)))
        );
    }

    public final double getTriggerChance(int level) {
        return this.triggerChance.getValue(level);
    }

    public final boolean checkTriggerChance(int level) {
        return Rnd.get(true) <= this.getTriggerChance(level);
    }
}
