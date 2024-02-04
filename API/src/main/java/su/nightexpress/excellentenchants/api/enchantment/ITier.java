package su.nightexpress.excellentenchants.api.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.placeholder.Placeholder;

import java.util.Map;

public interface ITier extends Placeholder {

    @NotNull String getId();

    int getPriority();

    @NotNull String getName();

    @NotNull String getColor();

    @NotNull Map<ObtainType, Double> getChance();

    double getChance(@NotNull ObtainType obtainType);
}
