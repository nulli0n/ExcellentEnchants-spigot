package su.nightexpress.excellentenchants.enchantment.impl.meta;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.enchantment.task.ArrowTrailsTask;

import java.util.Optional;

public final class ArrowImplementation implements Arrowed {

    private final ExcellentEnchant enchant;
    private final NamespacedKey projectileKey;

    private final SimpleParticle trailParticle;

    private ArrowImplementation(@NotNull ExcellentEnchant enchant, @Nullable SimpleParticle trailParticle) {
        this.enchant = enchant;
        this.projectileKey = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "arrow.enchant_id");
        this.trailParticle = trailParticle;
    }

    @NotNull
    public static ArrowImplementation create(@NotNull ExcellentEnchant enchant) {
        return create(enchant, SimpleParticle.of(Particle.REDSTONE));
    }

    @NotNull
    public static ArrowImplementation create(@NotNull ExcellentEnchant enchant, @NotNull SimpleParticle particle) {
        JYML cfg = enchant.getConfig();

        SimpleParticle effect = new JOption<>("Settings.Arrow.Trail_Effect",
            (cfg1, path, def) -> SimpleParticle.read(cfg1, path),
            particle,
            "Sets particle effect for the arrow trail of this enchantment."
        ).setWriter((cfg1, path, particle1) -> particle1.write(cfg1, path)).read(cfg);

        return new ArrowImplementation(enchant, effect);
    }

    @Override
    @NotNull
    public Arrowed getArrowImplementation() {
        return this;
    }

    @Override
    public void addTrail(@NotNull Projectile projectile) {
        if (!this.enchant.hasVisualEffects()) return;
        if (this.getTrailParticle().isEmpty()) return;

        this.getTrailParticle().ifPresent(particle -> {
            ArrowTrailsTask.add(projectile, particle);
        });
    }

    @NotNull
    @Override
    public Optional<SimpleParticle> getTrailParticle() {
        return trailParticle == null ? Optional.empty() : Optional.of(trailParticle);
    }

    @NotNull
    public NamespacedKey getProjectileKey() {
        return projectileKey;
    }

    @Override
    public void addData(@NotNull Projectile projectile) {
        PDCUtil.set(projectile, this.getProjectileKey(), this.enchant.getId());
    }

    @Override
    public boolean isOurProjectile(@NotNull Projectile projectile) {
        String enchantId = PDCUtil.getString(projectile, this.getProjectileKey()).orElse(null);
        return this.enchant.getId().equalsIgnoreCase(enchantId);
    }
}
