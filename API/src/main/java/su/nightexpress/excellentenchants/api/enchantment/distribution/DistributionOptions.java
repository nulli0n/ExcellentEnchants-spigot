package su.nightexpress.excellentenchants.api.enchantment.distribution;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;

@Deprecated
public interface DistributionOptions {

    void load(@NotNull FileConfig config);
}
