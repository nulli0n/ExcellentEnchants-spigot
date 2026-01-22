package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.nio.file.Path;

@FunctionalInterface
public interface EnchantFactory<T extends CustomEnchantment> {

    @NotNull T create(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context);
}
