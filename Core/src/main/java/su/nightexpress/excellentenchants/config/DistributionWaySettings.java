package su.nightexpress.excellentenchants.config;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniInt;

@Deprecated
public class DistributionWaySettings {

    private final boolean enabled;
    private final int    maxEnchantments;
    private final double generationChance;
    private final UniInt amount;

    public DistributionWaySettings(boolean enabled,
                                   int maxEnchantments, double generationChance,
                                   @NotNull UniInt amount) {
        this.enabled = enabled;
        this.maxEnchantments = maxEnchantments;
        this.generationChance = generationChance;
        this.amount = amount;
    }

    @NotNull
    public static DistributionWaySettings read(@NotNull FileConfig cfg, @NotNull String path) {
        boolean enabled = cfg.getBoolean(path + ".Enabled");
        int maxEnchantments = cfg.getInt(path + ".Max_Enchantments", 4);
        double generationChance = cfg.getDouble(path + ".Generation_Chance", 50D);
        UniInt amount = UniInt.read(cfg, path + ".Amount");

        return new DistributionWaySettings(enabled, maxEnchantments, generationChance, amount);
    }

    public void write(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.set(path + ".Enabled", this.isEnabled());
        cfg.set(path + ".Max_Enchantments", this.getMaxEnchantments());
        cfg.set(path + ".Generation_Chance", this.getGenerationChance());
        this.amount.write(cfg, path + ".Amount");
    }

    public int rollAmount() {
        return this.amount.roll();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxEnchantments() {
        return maxEnchantments;
    }

    public double getGenerationChance() {
        return generationChance;
    }

    @NotNull
    public UniInt getAmount() {
        return amount;
    }
}
