package su.nightexpress.excellentenchants.enchantment.task;

import org.bukkit.Particle;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ArrowTrailsTask extends AbstractTask<ExcellentEnchants> {

    private static final Map<Projectile, Set<Pair<Particle, String>>> TRAILS_MAP = new ConcurrentHashMap<>();

    public ArrowTrailsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_ARROW_TRAIL_TICKS_INTERVAL.get(), true);
        TRAILS_MAP.clear();
    }

    @Override
    public void action() {
        TRAILS_MAP.keySet().removeIf(projectile -> !projectile.isValid() || projectile.isDead());

        TRAILS_MAP.forEach((arrow, effects) -> {
            effects.forEach(entry -> {
                EffectUtil.playEffect(arrow.getLocation(), entry.getFirst(), entry.getSecond(), 0f, 0f, 0f, 0f, 10);
            });
        });
    }

    @Deprecated
    public static void add(@NotNull Projectile projectile, @NotNull String particleName, @NotNull String particleData) {
        TRAILS_MAP.computeIfAbsent(projectile, list -> new HashSet<>()).add(Pair.of(Particle.valueOf(particleName), particleData));
    }

    public static void add(@NotNull Projectile projectile, @NotNull Particle particle, @NotNull String data) {
        TRAILS_MAP.computeIfAbsent(projectile, list -> new HashSet<>()).add(Pair.of(particle, data));
    }
}