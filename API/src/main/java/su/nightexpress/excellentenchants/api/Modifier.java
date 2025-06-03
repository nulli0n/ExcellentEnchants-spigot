package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

public class Modifier implements Writeable {

    private final double         base;
    private final double         perLevel;
    private final double         capacity;
    private final ModifierAction action;

    public Modifier(double base, double perLevel, double capacity, @NotNull ModifierAction action) {
        this.base = base;
        this.perLevel = perLevel;
        this.capacity = capacity;
        this.action = action;
    }

    @NotNull
    public static Builder addictive(double base) {
        return new Builder(base, ModifierAction.ADD);
    }

    @NotNull
    public static Builder multiplier(double base) {
        return new Builder(base, ModifierAction.MULTIPLY);
    }

    @NotNull
    public static Modifier load(@NotNull FileConfig config, @NotNull String path, @NotNull Modifier.Builder builder, String ... comments) {
        return load(config, path, builder.build(), comments);
    }

    @NotNull
    public static Modifier load(@NotNull FileConfig config, @NotNull String path, @NotNull Modifier def, String ... comments) {
        return ConfigValue.create(path, Modifier::read, def, comments).read(config);
    }

    @NotNull
    public static Modifier read(@NotNull FileConfig config, @NotNull String path) {
        double base = ConfigValue.create(path + ".Base", 0D).read(config);
        double perLevel = ConfigValue.create(path + ".Per_Level", 0D).read(config);
        double cap = ConfigValue.create(path + ".Capacity", -1).read(config);
        ModifierAction action = ConfigValue.create(path + ".Action", ModifierAction.class, ModifierAction.ADD).read(config);

        return new Modifier(base, perLevel, cap, action);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Base", this.base);
        config.set(path + ".Per_Level", this.perLevel);
        config.set(path + ".Capacity", this.capacity);
        config.set(path + ".Action", this.action.name());
    }

    public double getValue(int level) {
        if (this.perLevel == 0D) return this.base;

        //double step = this.step == 0D ? 1D : Math.floor((double) level / this.step);
        double result =  this.action.math(this.base, this.perLevel * level);

        return this.capValue(result);
    }

    public double capValue(double result) {
        return this.capacity > 0 ? Math.min(result, this.capacity) : result;
    }

    public int getIntValue(int level) {
        return (int) this.getValue(level);
    }

    public double getBase() {
        return this.base;
    }

    public double getPerLevel() {
        return this.perLevel;
    }

    public double getCapacity() {
        return this.capacity;
    }

    @NotNull
    public ModifierAction getAction() {
        return this.action;
    }

    public static class Builder {

        private final double base;
        private final ModifierAction action;

        private double perLevel;
        private double capacity;

        public Builder(double base, @NotNull ModifierAction action) {
            this.base = base;
            this.action = action;
            this.perLevel = 0D;
            this.capacity = -1D;
        }

        @NotNull
        public Modifier build() {
            return new Modifier(this.base, this.perLevel, this.capacity, this.action);
        }

        @NotNull
        public Builder perLevel(double perLevel) {
            this.perLevel = perLevel;
            return this;
        }

        @NotNull
        public Builder capacity(double capacity) {
            this.capacity = capacity;
            return this;
        }
    }
}
