package su.nightexpress.excellentenchants.manager.tasks;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantPotionTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;

public class EnchantEffectPassiveTask extends AbstractEnchantPassiveTask {

    public EnchantEffectPassiveTask(@NotNull ExcellentEnchants plugin) {
        super(plugin, Config.TASKS_PASSIVE_ENCHANTS_TICKS_INTERVAL, true);
    }

    @Override
    protected void apply(@NotNull LivingEntity entity, @NotNull ItemStack armor, @NotNull ItemMeta meta) {
        meta.getEnchants().forEach((enchantment, level) -> {
            if (level < 1) return;
            if (!(enchantment instanceof PassiveEnchant passiveEnchant)) return;
            if (!(enchantment instanceof IEnchantPotionTemplate)) return;

            passiveEnchant.use(entity, level);
        });
    }
}
