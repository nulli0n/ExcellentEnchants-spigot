package su.nightexpress.excellentenchants.enchantment.data;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.data.ArrowSettings;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public class ArrowSettingsImpl implements ArrowSettings {

    //private final EnchantmentData enchantmentData;
    private final UniParticle trailParticle;

    private ArrowSettingsImpl(@NotNull UniParticle trailParticle) {
        //this.enchantmentData = enchantmentData;
        this.trailParticle = trailParticle;
    }

    @NotNull
    public static ArrowSettingsImpl create(@NotNull FileConfig config) {
        return create(config, UniParticle.of(Particle.REDSTONE));
    }

    @NotNull
    public static ArrowSettingsImpl create(@NotNull FileConfig config, @NotNull UniParticle particle) {
        UniParticle effect = ConfigValue.create("Settings.VisualEffects.Trail",
            (cfg, path, def) -> UniParticle.read(cfg, path),
            (cfg, path, particle1) -> particle1.write(cfg, path),
            () -> particle,
            "Sets projectile particle trail effect."
        ).read(config);

        return new ArrowSettingsImpl(effect);
    }

    @Override
    @NotNull
    public UniParticle getProjectileTrail() {
        return this.trailParticle;
    }
}
