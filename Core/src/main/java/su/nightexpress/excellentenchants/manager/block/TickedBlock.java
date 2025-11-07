package su.nightexpress.excellentenchants.manager.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.EntityUtil;
import su.nightexpress.nightcore.util.LocationUtil;
import su.nightexpress.nightcore.util.TimeUtil;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

public class TickedBlock {

    private final Location location;
    private final Material originalType;
    private final long lifeTime;
    private final int  sourceId;

    private long livedTicks;

    public TickedBlock(@NotNull Location location, @NotNull Material originalType, int seconds) {
        this.location = location;
        this.originalType = originalType;
        this.lifeTime = TimeUtil.secondsToTicks(seconds);
        this.sourceId = EntityUtil.nextEntityId();

        this.livedTicks = 0;
    }

    public void restore() {
        if (!this.location.isWorldLoaded()) return;

        this.location.getBlock().setType(this.originalType);
    }

    public void sendDamageInfo(float progress) {
        if (!this.location.isWorldLoaded()) return;

        World world = this.location.getWorld();
        if (world != null) {
            world.getPlayers().forEach(player -> player.sendBlockDamage(this.location, progress, this.sourceId));
        }
    }

    public void tick() {
        this.livedTicks++;

        if (this.isDead()) {
            Location location = LocationUtil.setCenter3D(this.location);
            UniParticle.blockCrack(this.location.getBlock().getType()).play(location, 0.5, 0.7, 0.5, 0.03, 30);
            this.sendDamageInfo(0F);
            this.restore();
            return;
        }

        this.sendDamageInfo(this.getProgress());
    }

    public float getProgress() {
        return (float) this.livedTicks / (float) this.lifeTime;
    }

    @NotNull
    public Location getLocation() {
        return this.location;
    }

    public boolean isDead() {
        return this.livedTicks >= this.lifeTime;
    }

    public boolean isAlive() {
        return !this.isDead();
    }

    public int getSourceId() {
        return this.sourceId;
    }
}
