package su.nightexpress.excellentenchants.manager.object;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.leveling.Scaler;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;

public class EnchantScaler extends Scaler {

    public EnchantScaler(@NotNull ExcellentEnchant enchant, @NotNull String path) {
        super(enchant.getConfig(), path, ExcellentEnchant.PLACEHOLDER_LEVEL, enchant.getStartLevel(), enchant.getMaxLevel());
    }
}
