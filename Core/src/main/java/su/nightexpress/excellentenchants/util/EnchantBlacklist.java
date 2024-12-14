package su.nightexpress.excellentenchants.util;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;

import java.util.Set;
import java.util.stream.Collectors;

public class EnchantBlacklist implements Writeable {

    private final Set<String> enchantNames;

    public EnchantBlacklist(@NotNull Set<String> enchantNames) {
        this.enchantNames = enchantNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }

    public boolean contains(@NotNull CustomEnchantment enchantment) {
        return this.contains(enchantment.getId());
    }

    public boolean contains(@NotNull String name) {
        return this.enchantNames.contains(Placeholders.WILDCARD) || this.enchantNames.contains(name.toLowerCase());
    }

    @NotNull
    public static EnchantBlacklist read(@NotNull FileConfig config, @NotNull String path) {
        Set<String> names = config.getStringSet(path);
        return new EnchantBlacklist(names);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path, this.enchantNames);
    }
}
