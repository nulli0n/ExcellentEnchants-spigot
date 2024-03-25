package su.nightexpress.excellentenchants.api.enchantment.distribution;

public interface VanillaOptions extends DistributionOptions {

    boolean isDiscoverable();

    void setDiscoverable(boolean discoverable);

    boolean isTradeable();

    void setTradeable(boolean tradeable);
}
