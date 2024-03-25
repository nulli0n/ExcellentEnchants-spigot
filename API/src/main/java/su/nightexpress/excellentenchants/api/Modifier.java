package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;

public class Modifier {

    private double base;
    private double perLevel;
    private double step;
    private double cap;
    private ModifierAction action;

    public Modifier(double base, double perLevel, double step, double cap, @NotNull ModifierAction action) {
        this.setBase(base);
        this.setPerLevel(perLevel);
        this.setStep(step);
        this.setCap(cap);
        this.setAction(action);
    }

    @NotNull
    public static Modifier add(double base, double perLevel, double step) {
        return add(base, perLevel, step, -1);
    }

    @NotNull
    public static Modifier multiply(double base, double perLevel, double step) {
        return multiply(base, perLevel, step, -1);
    }

    @NotNull
    public static Modifier add(double base, double perLevel, double step, double cap) {
        return new Modifier(base, perLevel, step, cap, ModifierAction.ADD);
    }

    @NotNull
    public static Modifier multiply(double base, double perLevel, double step, double cap) {
        return new Modifier(base, perLevel, step, cap, ModifierAction.MULTIPLY);
    }

    @NotNull
    public static Modifier read(@NotNull FileConfig cfg, @NotNull String path, @NotNull Modifier def, String ... comments) {
        return new ConfigValue<>(path,
            (cfg2, path2, def2) -> Modifier.read(cfg2, path2),
            (cfg2, path2, mod) -> mod.write(cfg2, path2),
            def,
            comments
        ).read(cfg);
    }

    @NotNull
    public static Modifier read(@NotNull FileConfig cfg, @NotNull String path) {
        double base = ConfigValue.create(path + ".Base", 0D,
            "Start modifier value."
        ).read(cfg);

        double perLevel = ConfigValue.create(path + ".Per_Level", 0D,
            "Additional value calculated by enchantment level step (see below). Formula: <per_level> * <step>"
        ).read(cfg);

        double step = ConfigValue.create(path + ".Step" , 1D,
            "Defines level step for 'Per_Level' value calculation. Formula: <enchant_level> / <step>"
        ).read(cfg);

        double cap = ConfigValue.create(path + ".Cap", -1,
            "Sets a limit for the final (base & per level calculations) value.",
            "Set '-1' for no limit."
        ).read(cfg);

        ModifierAction action = ConfigValue.create(path + ".Action", ModifierAction.class, ModifierAction.ADD,
            "Sets action performed between 'Base' and final 'Per_Level' values.",
            "Available types: " + StringUtil.inlineEnum(ModifierAction.class, ", ")
        ).read(cfg);

        return new Modifier(base, perLevel, step, cap, action);
    }

    public void write(@NotNull FileConfig cfg, @NotNull String path) {
        cfg.set(path + ".Base", this.getBase());
        cfg.set(path + ".Per_Level", this.getPerLevel());
        cfg.set(path + ".Step", this.getStep());
        cfg.set(path + ".Cap", this.getCap());
        cfg.set(path + ".Action", this.getAction().name());
    }

    public double getValue(int level) {
        if (/*level == 1 || */this.perLevel == 0D) return this.capValue(this.getBase());

        //level -= 1;

        double step = this.getStep() == 0D ? 1D : Math.floor((double) level / this.getStep());
        double result =  this.action.math(this.getBase(), this.getPerLevel() * step);

        return this.capValue(result);
    }

    public double capValue(double result) {
        return this.cap > 0 ? Math.min(result, this.cap) : result;
    }

    public int getIntValue(int level) {
        return (int) this.getValue(level);
    }

    public double getBase() {
        return this.base;
    }

    public void setBase(double base) {
        this.base = base;
    }

    public double getPerLevel() {
        return this.perLevel;
    }

    public void setPerLevel(double perLevel) {
        this.perLevel = perLevel;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getCap() {
        return cap;
    }

    public void setCap(double cap) {
        this.cap = cap;
    }

    @NotNull
    public ModifierAction getAction() {
        return this.action;
    }

    public void setAction(@NotNull ModifierAction action) {
        this.action = action;
    }
}
