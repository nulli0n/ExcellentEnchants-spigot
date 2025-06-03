package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;

public class EnchantData {

    private final EnchantDefinition   definition;
    private final EnchantDistribution distribution;
    private final boolean             curse;

    public EnchantData(@NotNull EnchantDefinition definition, @NotNull EnchantDistribution distribution, boolean curse) {
        this.definition = definition;
        this.distribution = distribution;
        this.curse = curse;
    }

    @NotNull
    public EnchantDefinition getDefinition() {
        return this.definition;
    }

    @NotNull
    public EnchantDistribution getDistribution() {
        return this.distribution;
    }

    public boolean isCurse() {
        return this.curse;
    }
}
