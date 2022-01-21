package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.utils.EffectUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;

import java.util.*;

public class ArrowTrailsTask extends AbstractTask<ExcellentEnchants> {

    private static final Map<Projectile, Set<Map.Entry<String, String>>> TRAILS_MAP = Collections.synchronizedMap(new HashMap<>());

    public ArrowTrailsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_ARROW_TRAIL_TICKS_INTERVAL, true);
        TRAILS_MAP.clear();
    }

    @Override
    public void action() {
        TRAILS_MAP.keySet().removeIf(projectile -> !projectile.isValid() || projectile.isDead());

        TRAILS_MAP.forEach((arrow, effects) -> {
            effects.forEach(entry -> {
                EffectUtil.playEffect(arrow.getLocation(), entry.getKey(), entry.getValue(), 0f, 0f, 0f, 0f, 10);
            });
        });
    }

    public static void add(@NotNull Projectile projectile, @NotNull String particleName, @NotNull String particleData) {
        synchronized (TRAILS_MAP) {
            TRAILS_MAP.computeIfAbsent(projectile, list -> new HashSet<>()).add(new AbstractMap.SimpleEntry<>(particleName, particleData));
        }
    }
}