package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public interface ArrowMeta extends MetaHolder {

    @NotNull
    default UniParticle getProjectileTrail() {
        return this.getMeta().getArrowEffects().getProjectileTrail();
    }
}
