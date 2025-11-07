package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.IEnchantData;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;

public class EnchantData implements IEnchantData {

    private final EnchantDefinition   definition;
    private final EnchantDistribution distribution;
    private final EnchantProvider<?>  provider;
    private final boolean             curse;

    public EnchantData(@NotNull EnchantDefinition definition, @NotNull EnchantDistribution distribution, @NotNull EnchantProvider<?> provider, boolean curse) {
        this.definition = definition;
        this.distribution = distribution;
        this.provider = provider;
        this.curse = curse;
    }

    @Override
    @NotNull
    public EnchantDefinition getDefinition() {
        return this.definition;
    }

    @Override
    @NotNull
    public EnchantDistribution getDistribution() {
        return this.distribution;
    }

    @NotNull
    public EnchantProvider<?> getProvider() {
        return this.provider;
    }

    public boolean isCurse() {
        return this.curse;
    }
}
