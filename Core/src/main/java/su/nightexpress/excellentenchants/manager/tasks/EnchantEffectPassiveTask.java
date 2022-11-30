package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.Map;

public class EnchantEffectPassiveTask extends AbstractEnchantPassiveTask {

    public EnchantEffectPassiveTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_POTION_EFFECTS_APPLY_INTERVAL.get(), false);
    }

    @Override
    protected void apply(@NotNull LivingEntity entity, @NotNull Map<ExcellentEnchant, Integer> enchants) {
        EnchantManager.updateEquippedEnchantEffects(entity);
    }
}
