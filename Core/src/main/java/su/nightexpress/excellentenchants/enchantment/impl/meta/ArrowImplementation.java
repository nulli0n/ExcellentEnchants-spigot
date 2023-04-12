package su.nightexpress.excellentenchants.enchantment.impl.meta;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.meta.Arrowed;
import su.nightexpress.excellentenchants.enchantment.task.ArrowTrailsTask;

import java.util.Optional;

public final class ArrowImplementation implements Arrowed {

    private final ExcellentEnchant enchant;
    private final NamespacedKey projectileKey;

    private final Particle trailParticle;
    private final String   trailData;

    private ArrowImplementation(@NotNull ExcellentEnchant enchant,
                                @Nullable Particle trailParticle, @Nullable String  trailData) {
        this.enchant = enchant;
        this.projectileKey = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "arrow.enchant_id");

        this.trailParticle = trailParticle;
        this.trailData = trailData;
    }

    @NotNull
    public static ArrowImplementation create(@NotNull ExcellentEnchant enchant) {
        JYML cfg = enchant.getConfig();
        Particle trailParticle = JOption.create("Settings.Arrow.Trail_Effect.Name", Particle.class, Particle.REDSTONE,
            "Particle name for the arrow trail effect.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Particle.html").read(cfg);
        String trailData = JOption.create("Settings.Arrow.Trail_Effect.Data", "",
            "Particle data for the particle effect.",
            "This is required for certain particles only.").read(cfg);

        return new ArrowImplementation(enchant, trailParticle, trailData);
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

        Particle particle = this.getTrailParticle().get();
        String data = this.getTrailData().orElse("");

        ArrowTrailsTask.add(projectile, particle, data);
    }

    @NotNull
    @Override
    public Optional<Particle> getTrailParticle() {
        return trailParticle == null ? Optional.empty() : Optional.of(trailParticle);
    }

    @NotNull
    @Override
    public Optional<String> getTrailData() {
        return trailData == null ? Optional.empty() : Optional.of(trailData);
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
