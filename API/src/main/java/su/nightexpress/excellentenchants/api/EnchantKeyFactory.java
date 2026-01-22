package su.nightexpress.excellentenchants.api;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EnchantKeyFactory {

    @NotNull NamespacedKey create(@NotNull String value);
}
