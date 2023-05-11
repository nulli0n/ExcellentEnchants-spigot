package su.nightexpress.excellentenchants.enchantment.task;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.particle.SimpleParticle;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ArrowTrailsTask extends AbstractTask<ExcellentEnchants> {

    private static final Map<Projectile, Set<SimpleParticle>> TRAILS_MAP = new ConcurrentHashMap<>();

    public ArrowTrailsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_ARROW_TRAIL_TICKS_INTERVAL.get(), true);
        TRAILS_MAP.clear();
    }

    @Override
    public void action() {
        TRAILS_MAP.keySet().removeIf(projectile -> !projectile.isValid() || projectile.isDead());

        TRAILS_MAP.forEach((arrow, effects) -> {
            effects.forEach(entry -> {
                entry.play(arrow.getLocation(), 0f, 0f, 10);
            });
        });
    }

    public static void add(@NotNull Projectile projectile, @NotNull SimpleParticle particle) {
        TRAILS_MAP.computeIfAbsent(projectile, list -> new HashSet<>()).add(particle);
    }
}