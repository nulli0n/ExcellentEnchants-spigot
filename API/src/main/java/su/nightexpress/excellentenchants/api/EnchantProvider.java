package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;

import java.io.File;

public interface EnchantProvider<T extends CustomEnchantment> {

    @NotNull T create(@NotNull File file, @NotNull EnchantData data);
}
