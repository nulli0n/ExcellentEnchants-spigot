package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;

public record EnchantContext(@NotNull String id,
                             @NotNull Enchantment enchantment,
                             @NotNull EnchantDefinition definition,
                             @NotNull EnchantDistribution distribution,
                             boolean curse) {
}
