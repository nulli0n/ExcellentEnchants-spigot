package su.nightexpress.excellentenchants.manager.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Explosion {

    private final LivingEntity owner;

    private Consumer<EntityExplodeEvent> onExplode;
    private Consumer<EntityDamageByEntityEvent> onDamage;

    public Explosion(@NotNull LivingEntity owner) {
        this.owner = owner;
    }

    public void handleExplosion(@NotNull EntityExplodeEvent event) {
        if (this.onExplode != null) {
            this.onExplode.accept(event);
        }
    }

    public void handleDamage(@NotNull EntityDamageByEntityEvent event) {
        if (this.onDamage != null) {
            this.onDamage.accept(event);
        }
    }

    @NotNull
    public LivingEntity getOwner() {
        return this.owner;
    }

    public void setOnExplode(@NotNull Consumer<EntityExplodeEvent> onExplode) {
        this.onExplode = onExplode;
    }

    public void setOnDamage(@NotNull Consumer<EntityDamageByEntityEvent> onDamage) {
        this.onDamage = onDamage;
    }
}
