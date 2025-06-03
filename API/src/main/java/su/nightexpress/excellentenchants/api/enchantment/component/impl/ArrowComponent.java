package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.ArrowEffects;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public class ArrowComponent implements EnchantComponent<ArrowEffects> {

    @Override
    @NotNull
    public String getName() {
        return "arrow";
    }

    @Override
    @NotNull
    public ArrowEffects read(@NotNull FileConfig config, @NotNull ArrowEffects defaultValue) {
        UniParticle effect = ConfigValue.create("ArrowEffects.Trail", UniParticle::read, defaultValue.getTrailEffect(),
            "Sets projectile particle trail effect."
        ).read(config);

        return new ArrowEffects(effect);
    }
}
