package su.nightexpress.excellentenchants.api.enchantment.component;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.component.impl.*;
import su.nightexpress.excellentenchants.api.enchantment.meta.*;
import su.nightexpress.nightcore.config.FileConfig;

public interface EnchantComponent<T> {

    EnchantComponent<Probability>   PROBABILITY   = new ProbabilityComponent();
    EnchantComponent<PotionEffects> POTION_EFFECT = new EffectComponent();
    EnchantComponent<ArrowEffects>  ARROW         = new ArrowComponent();
    EnchantComponent<Period>        PERIODIC      = new PeriodComponent();
    EnchantComponent<Charges>       CHARGES       = new ChargesComponent();

    @NotNull String getName();

    @NotNull T read(@NotNull FileConfig config, @NotNull T defaultValue);
}
