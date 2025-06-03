package su.nightexpress.excellentenchants.api.enchantment.component;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;

public interface ComponentLoader<T> {

    @NotNull T load(@NotNull FileConfig config);
}
