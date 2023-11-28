package su.nightexpress.excellentenchants.enchantment.util;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;

public class Modifier {

    private final double         base;
    private final double         perLevel;
    private final ModifierAction action;

    public Modifier(double base, double perLevel, @NotNull ModifierAction action) {
        this.base = base;
        this.perLevel = perLevel;
        this.action = action;
    }

    @NotNull
    public static Modifier add(double base, double perLevel) {
        return new Modifier(base, perLevel, ModifierAction.ADD);
    }

    @NotNull
    public static Modifier multiply(double base, double perLevel) {
        return new Modifier(base, perLevel, ModifierAction.MULTIPLY);
    }

    @NotNull
    public static Modifier read(@NotNull JYML cfg, @NotNull String path, @NotNull Modifier def, @NotNull String... comments) {
        return new JOption<>(path,
            (cfg2, path2, def2) -> read(cfg2, path2),
            def,
            comments
        ).setWriter((cfg2, path2, mod) -> mod.write(cfg2, path2)).read(cfg);
    }

    @NotNull
    public static Modifier read(@NotNull JYML cfg, @NotNull String path) {
        double base = JOption.create(path + ".Base", 0D).read(cfg);
        double perLevel = JOption.create(path + ".Per_Level", 0D).read(cfg);
        ModifierAction action = JOption.create(path + ".Action", ModifierAction.class, ModifierAction.ADD).read(cfg);

        return new Modifier(base, perLevel, action);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Base", this.getBase());
        cfg.set(path + ".Per_Level", this.getPerLevel());
        cfg.set(path + ".Action", this.getAction().name());
    }

    public double getValue(int level) {
        return this.action.math(this.getBase(), this.getPerLevel() * level);
    }

    public double getBase() {
        return base;
    }

    public double getPerLevel() {
        return perLevel;
    }

    @NotNull
    public ModifierAction getAction() {
        return action;
    }
}
