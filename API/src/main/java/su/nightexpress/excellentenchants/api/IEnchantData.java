package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;

public interface IEnchantData {

    @NotNull EnchantDefinition getDefinition();

    @NotNull EnchantDistribution getDistribution();

    boolean isCurse();
}
