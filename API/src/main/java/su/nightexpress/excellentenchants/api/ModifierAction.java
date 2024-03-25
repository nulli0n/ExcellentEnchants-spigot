package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public enum ModifierAction {

    ADD(Double::sum),
    MULTIPLY((origin, target) -> origin * target);

    private final BiFunction<Double, Double, Double> function;

    ModifierAction(@NotNull BiFunction<Double, Double, Double> function) {
        this.function = function;
    }

    public double math(double origin, double target) {
        return this.function.apply(origin, target);
    }
}
