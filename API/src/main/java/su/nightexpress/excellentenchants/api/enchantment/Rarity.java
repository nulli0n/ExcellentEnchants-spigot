package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;

public interface Rarity {

    void write(@NotNull FileConfig config, @NotNull String path);

    @NotNull String getId();

    @NotNull String getName();

    @NotNull String getNameFormat();

    int getWeight();
}
