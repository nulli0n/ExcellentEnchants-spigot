package su.nightexpress.excellentenchants.enchantment.task;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

public class PotionEffectsTask extends AbstractEnchantmentTask {

    public PotionEffectsTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_POTION_EFFECTS_APPLY_INTERVAL.get(), false);
    }

    @Override
    public void action() {
        for (LivingEntity entity : this.getEntities()) {
            EnchantUtils.updateEquippedEffects(entity);
        }
    }
}
