package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

public class Cost {

    private final int base;
    private final int perLevel;

    public Cost(int base, int perLevel) {
        this.base = base;
        this.perLevel = perLevel;
    }

    @NotNull
    public static Cost read(@NotNull FileConfig config, @NotNull String path, @NotNull Cost defaultValue, String ... comments) {
        return ConfigValue.create(path,
            (cfg2, path2, def2) -> Cost.read(cfg2, path2),
            (cfg2, path2, mod) -> mod.write(cfg2, path2),
            () -> defaultValue,
            comments
        ).read(config);
    }

    @NotNull
    public static Cost read(@NotNull FileConfig config, @NotNull String path) {
        int base = ConfigValue.create(path + ".Base", 0).read(config);
        int perLevel = ConfigValue.create(path + ".Per_Level", 0).read(config);

        return new Cost(base, perLevel);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Base", this.base());
        config.set(path + ".Per_Level", this.perLevel());
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
