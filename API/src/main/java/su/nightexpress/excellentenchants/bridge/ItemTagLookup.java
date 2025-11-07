package su.nightexpress.excellentenchants.bridge;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ItemTagLookup {

    @NotNull Set<String> getBreakable();

    @NotNull Set<String> getHelmets();

    @NotNull Set<String> getChestplates();

    @NotNull Set<String> getLeggings();

    @NotNull Set<String> getBoots();

    @NotNull Set<String> getSwords();

    @NotNull Set<String> getAxes();

    @NotNull Set<String> getHoes();

    @NotNull Set<String> getPickaxes();

    @NotNull Set<String> getShovels();
}
