package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.random.Rnd;

public class DecayBlock {

    private final Location location;
    private final long createDate;
    private final long decayDate;
    private final int sourceId;

    public DecayBlock(@NotNull Location location, double seconds) {
        this.location = location;
        this.createDate = System.currentTimeMillis();
        this.decayDate = TimeUtil.createFutureTimestamp(seconds);
        this.sourceId = Rnd.nextInt(10000);
    }

    public float getProgress() {
        //long start = booster.getCreationDate();
        float end = this.decayDate - this.createDate;
        float now = System.currentTimeMillis() - this.createDate;

        return /*1F - */now / end;
    }

    public boolean isExpired() {
        return TimeUtil.isPassed(this.decayDate);
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    public int getSourceId() {
        return this.sourceId;
    }

    public long getDecayDate() {
        return this.decayDate;
    }
}
