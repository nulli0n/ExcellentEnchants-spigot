package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface Definition {

    boolean hasConflicts();

    @NotNull String getDisplayName();

    @NotNull List<String> getDescription();

    /**
     * Items on which this enchantment can be applied using an anvil or using the /enchant command.
     */
    @NotNull ItemsCategory getSupportedItems();

    /**
     * Items for which this enchantment appears in an enchanting table.
     */
    @NotNull ItemsCategory getPrimaryItems();

    @NotNull Set<String> getConflicts();

    @NotNull Rarity getRarity();

    int getMaxLevel();

    @NotNull Cost getMinCost();

    @NotNull Cost getMaxCost();

    int getAnvilCost();
}
