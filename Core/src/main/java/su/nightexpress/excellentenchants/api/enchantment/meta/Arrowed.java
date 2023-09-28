package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.values.UniParticle;

import java.util.Optional;

public interface Arrowed {

    @NotNull Arrowed getArrowImplementation();

    @NotNull
    default Optional<UniParticle> getTrailParticle() {
        return this.getArrowImplementation().getTrailParticle();
    }

    default void addTrail(@NotNull Projectile projectile) {
        this.getArrowImplementation().addTrail(projectile);
    }

    default void addData(@NotNull Projectile projectile) {
        this.getArrowImplementation().addData(projectile);
    }

    default boolean isOurProjectile(@NotNull Projectile projectile) {
        return this.getArrowImplementation().isOurProjectile(projectile);
    }
}
