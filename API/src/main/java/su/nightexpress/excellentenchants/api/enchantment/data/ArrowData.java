package su.nightexpress.excellentenchants.api.enchantment.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public interface ArrowData {

    @NotNull ArrowSettings getArrowSettings();

    @NotNull
    default UniParticle getProjectileTrail() {
        return this.getArrowSettings().getProjectileTrail();
    }
}
