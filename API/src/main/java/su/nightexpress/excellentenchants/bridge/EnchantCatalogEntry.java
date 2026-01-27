package su.nightexpress.excellentenchants.bridge;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

public interface EnchantCatalogEntry {

    @NotNull String getId();

    @NotNull NamespacedKey getKey();

    @NotNull EnchantDefinition getDefinition();

    @NotNull EnchantDistribution getDistribution();

    boolean isCurse();
}
