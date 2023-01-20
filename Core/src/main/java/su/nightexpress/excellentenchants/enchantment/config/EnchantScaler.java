package su.nightexpress.excellentenchants.enchantment.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnchantScaler extends Scaler {

    public EnchantScaler(@NotNull ExcellentEnchant enchant, @NotNull String path) {
        super(enchant.getConfig(), path, Placeholders.ENCHANTMENT_LEVEL, enchant.getStartLevel(), enchant.getMaxLevel());
    }

    @NotNull
    public static EnchantScaler read(@NotNull ExcellentEnchant enchant, @NotNull String path, @NotNull String def, @Nullable String... comments) {
        enchant.getConfig().addMissing(path, def);
        if (comments != null) {
            List<String> list = new ArrayList<>(Arrays.asList(comments));
            list.add("You can use formulas/expressions here: " + Placeholders.URL_ENGINE_SCALER);
            list.add("Level placeholder: " + Placeholders.ENCHANTMENT_LEVEL);
            enchant.getConfig().setComments(path, list);
        }
        return new EnchantScaler(enchant, path);
    }
}
