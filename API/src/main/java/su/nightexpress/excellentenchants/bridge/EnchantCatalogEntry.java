package su.nightexpress.excellentenchants.bridge;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

public interface EnchantCatalogEntry {

    @NotNull String getId();

    @NotNull EnchantDefinition getDefinition();

    @NotNull EnchantDistribution getDistribution();

    boolean isCurse();
}
