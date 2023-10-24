package su.nightexpress.excellentenchants.enchantment.task;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PassiveEnchantsTask extends AbstractTask<ExcellentEnchants> {

    public PassiveEnchantsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get(), false);
    }

    @Override
    public void action() {
        for (LivingEntity entity : this.getEntities()) {
            EnchantUtils.triggerPassiveEnchants(entity);
        }
    }

    @NotNull
    private Collection<? extends LivingEntity> getEntities() {
        Set<LivingEntity> list = new HashSet<>(plugin.getServer().getOnlinePlayers());

        if (Config.ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS.get()) {
            plugin.getServer().getWorlds().stream().filter(world -> !world.getPlayers().isEmpty()).forEach(world -> {
                list.addAll(world.getEntitiesByClass(LivingEntity.class));
            });
        }
        list.removeIf(entity -> entity.isDead() || !entity.isValid());
        return list;
    }
}
