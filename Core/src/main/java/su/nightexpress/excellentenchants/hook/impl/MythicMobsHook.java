package su.nightexpress.excellentenchants.hook.impl;

import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MythicMobsHook {

    private static final MythicBukkit MYTHIC_MOBS = MythicBukkit.inst();

    public static boolean isMythicMob(@NotNull Entity entity) {
        return MYTHIC_MOBS.getAPIHelper().isMythicMob(entity);
    }
}
