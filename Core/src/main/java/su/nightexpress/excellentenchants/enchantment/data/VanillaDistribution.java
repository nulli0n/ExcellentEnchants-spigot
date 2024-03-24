package su.nightexpress.excellentenchants.enchantment.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.distribution.VanillaOptions;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

public class VanillaDistribution implements VanillaOptions {

    private boolean discoverable;
    private boolean tradeable;

    @Override
    public void load(@NotNull FileConfig config) {
        this.setTradeable(ConfigValue.create("Distribution.Tradeable",
            true,
            "Sets whether or not this enchantment can be populated in villager trades.").read(config));

        this.setDiscoverable(ConfigValue.create("Distribution.Discoverable",
            true,
            "Sets whether or not this enchantment can be populated in enchanting table.").read(config));
    }

    @Override
    public boolean isTradeable() {
        return this.tradeable;
    }

    @Override
    public void setTradeable(boolean tradeable) {
        this.tradeable = tradeable;
    }

    @Override
    public boolean isDiscoverable() {
        return discoverable;
    }

    @Override
    public void setDiscoverable(boolean discoverable) {
        this.discoverable = discoverable;
    }
}
