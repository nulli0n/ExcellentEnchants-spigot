package su.nightexpress.excellentenchants.manager.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Scaler;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;

public class EnchantScaler extends Scaler {

    public EnchantScaler(@NotNull ExcellentEnchant enchant, @NotNull String path) {
        super(enchant.getConfig(), path, Placeholders.ENCHANTMENT_LEVEL, enchant.getStartLevel(), enchant.getMaxLevel());
    }
}
