package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.nightcore.config.FileConfig;

public class ProbabilityComponent implements EnchantComponent<Probability> {

    @Override
    @NotNull
    public String getName() {
        return "probability";
    }

    @Override
    @NotNull
    public Probability read(@NotNull FileConfig config, @NotNull Probability defaultValue) {
        Modifier triggerChance = Modifier.load(config, "Probability.Trigger_Chance", defaultValue.getTriggerChance());

        return new Probability(triggerChance);
    }
}
