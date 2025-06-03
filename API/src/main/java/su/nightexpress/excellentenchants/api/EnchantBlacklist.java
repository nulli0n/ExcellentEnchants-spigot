package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.Lists;

import java.util.Set;

public class EnchantBlacklist implements Writeable {

    private final Set<String> names;

    public EnchantBlacklist(@NotNull Set<String> names) {
        this.names = Lists.modify(names, String::toLowerCase);
    }

    @NotNull
    public static EnchantBlacklist read(@NotNull FileConfig config, @NotNull String path) {
        Set<String> names = config.getStringSet(path);
        return new EnchantBlacklist(names);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path, this.names);
    }

    public boolean contains(@NotNull CustomEnchantment enchantment) {
        return this.contains(enchantment.getId());
    }

    public boolean contains(@NotNull String name) {
        return this.names.contains(EnchantsPlaceholders.WILDCARD) || this.names.contains(name.toLowerCase());
    }
}
