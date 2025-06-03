package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.TimeUtil;

public class Period {

    private final long interval;

    public Period(long interval) {
        this.interval = interval;
    }

    @NotNull
    public static Period ofSeconds(int seconds) {
        return new Period(TimeUtil.secondsToTicks(seconds));
    }

    @NotNull
    public static Period ofTicks(long interval) {
        return new Period(interval);
    }

    public long getInterval() {
        return this.interval;
    }

    public boolean isTriggerTime(@NotNull LivingEntity entity) {
        return entity.getTicksLived() % this.interval == 0L;
    }
}
