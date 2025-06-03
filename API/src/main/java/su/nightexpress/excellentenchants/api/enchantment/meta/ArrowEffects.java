package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public class ArrowEffects {

    private final UniParticle trailEffect;

    public ArrowEffects(@NotNull UniParticle trailEffect) {
        this.trailEffect = trailEffect;
    }

    @NotNull
    public static ArrowEffects basic(@NotNull Particle particle) {
        return new ArrowEffects(UniParticle.of(particle));
    }

    @NotNull
    public UniParticle getTrailEffect() {
        return this.trailEffect;
    }
}
