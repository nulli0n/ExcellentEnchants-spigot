package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.io.File;

public interface EnchantProvider<T extends CustomEnchantment> {

    @NotNull T create(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data);
}
