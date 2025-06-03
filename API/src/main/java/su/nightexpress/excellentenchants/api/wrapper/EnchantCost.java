package su.nightexpress.excellentenchants.api.wrapper;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public class EnchantCost implements Writeable {

    private final int base;
    private final int perLevel;

    public EnchantCost(int base, int perLevel) {
        this.base = base;
        this.perLevel = perLevel;
    }

    @NotNull
    public static EnchantCost read(@NotNull FileConfig config, @NotNull String path) {
        int base = ConfigValue.create(path + ".Base",
            0,
            "The cost for a level I enchantment."
        ).read(config);

        int perLevel = ConfigValue.create(path + ".Per_Level",
            0,
            "The amount of levels added to the Base for each level above level I"
        ).read(config);

        return new EnchantCost(base, perLevel);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Base", this.base);
        config.set(path + ".Per_Level", this.perLevel);
    }

    public int calculate(int level) {
        return this.base + this.perLevel * (level - 1);
    }

    public int base() {
        return this.base;
    }

    public int perLevel() {
        return this.perLevel;
    }
}
