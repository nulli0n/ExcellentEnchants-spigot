package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Deprecated
public abstract class AbstractEnchantPassiveTask extends AbstractTask<ExcellentEnchants> {

    public AbstractEnchantPassiveTask(@NotNull ExcellentEnchants plugin, long interval, boolean async) {
        super(plugin, interval, async);
    }

    protected abstract void apply(@NotNull LivingEntity entity, @NotNull Map<ExcellentEnchant, Integer> enchants);

    @Override
    public void action() {
        for (LivingEntity entity : this.getEntities()) {
            this.apply(entity, EnchantManager.getEquippedEnchantsMax(entity));
        }
    }

    @NotNull
    protected Collection<@NotNull ? extends LivingEntity> getEntities() {
        Set<LivingEntity> list = new HashSet<>(plugin.getServer().getOnlinePlayers());

        if (Config.ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS) {
            plugin.getServer().getWorlds().forEach(world -> {
                list.addAll(world.getEntitiesByClass(LivingEntity.class));
            });
        }
        return list;
    }
}
