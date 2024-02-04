package su.nightexpress.excellentenchants.enchantment.task;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.server.AbstractTask;
import su.nexmedia.engine.utils.Pair;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PassiveEnchantsTask extends AbstractTask<ExcellentEnchants> {

    private final Set<Pair<PassiveEnchant, ExcellentEnchant>> enchants;

    public PassiveEnchantsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_ENCHANTS_TRIGGER_INTERVAL.get(), false);
        this.enchants = new HashSet<>();

        EnchantRegistry.getEnchantments(PassiveEnchant.class).forEach(enchant -> {
            ExcellentEnchant excellent = EnchantRegistry.getByKey(enchant.getKey());
            if (excellent == null) return;

            this.enchants.add(Pair.of(enchant, excellent));
        });
    }

    @Override
    public void action() {
        if (this.enchants.isEmpty()) return;

        var entities = this.getEntities();

        this.enchants.forEach(pair -> {
            PassiveEnchant enchant = pair.getFirst();
            ExcellentEnchant excellent = pair.getSecond();
            if (!enchant.isTriggerTime()) return;

            for (LivingEntity entity : entities) {
                EnchantUtils.getEquipped(entity, excellent).forEach((item, level) -> {
                    if (!enchant.isAvailableToUse(entity)) return;
                    if (enchant.isOutOfCharges(item)) return;
                    if (enchant.onTrigger(entity, item, level)) {
                        enchant.consumeCharges(item, level);
                    }
                });
            }

            enchant.updateTriggerTime();
        });
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
