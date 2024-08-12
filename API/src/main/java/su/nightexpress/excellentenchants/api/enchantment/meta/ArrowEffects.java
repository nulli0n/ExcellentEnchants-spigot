package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public class ArrowEffects {

    private final UniParticle trailParticle;

    private ArrowEffects(@NotNull UniParticle trailParticle) {
        this.trailParticle = trailParticle;
    }

    @NotNull
    public static ArrowEffects create(@NotNull FileConfig config) {
        return create(config, UniParticle.of(Particle.CLOUD));
    }

    @NotNull
    public static ArrowEffects create(@NotNull FileConfig config, @NotNull UniParticle particle) {
        UniParticle effect = ConfigValue.create("Settings.VisualEffects.Trail",
            (cfg, path, def) -> UniParticle.read(cfg, path),
            (cfg, path, particle1) -> particle1.write(cfg, path),
            () -> particle,
            "Sets projectile particle trail effect."
        ).read(config);

        return new ArrowEffects(effect);
    }

    @NotNull
    public UniParticle getProjectileTrail() {
        return this.trailParticle;
    }
}
