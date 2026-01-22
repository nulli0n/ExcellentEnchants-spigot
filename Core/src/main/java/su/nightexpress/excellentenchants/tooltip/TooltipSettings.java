package su.nightexpress.excellentenchants.tooltip;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.tooltip.format.ChargesFormat;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigType;
import su.nightexpress.nightcore.configuration.ConfigTypes;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static su.nightexpress.excellentenchants.EnchantsPlaceholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class TooltipSettings extends AbstractConfig {

    private static final ConfigType<ChargesFormat> CHARGES_FORMAT_CONFIG_TYPE = ConfigType.of(
        ChargesFormat::read,
        FileConfig::set
    );

    @Override
    public void load(@NotNull FileConfig config) {
        if (config.contains("Description")) {
            config.set("EnchantTooltip.Books_Only", config.getBoolean("Description.Books_Only"));
            config.set("EnchantTooltip.Format.Default", config.getString("Description.Format.Default"));
            config.set("EnchantTooltip.Format.WithCharges", config.getString("Description.Format.WithCharges"));
            config.remove("Description");
        }

        super.load(config);
    }

    private final ConfigProperty<Boolean> booksOnly = this.addProperty(ConfigTypes.BOOLEAN, "EnchantTooltip.Books_Only",
        false,
        "Sets whether or not only enchanted books will have enchantment descriptions."
    );

    private final ConfigProperty<String> formatDefault = this.addProperty(ConfigTypes.STRING, "EnchantTooltip.Format.Default",
        GRAY.wrap("• " + GENERIC_DESCRIPTION),
        "Sets description format for enchantments without charges."
    );

    private final ConfigProperty<String> formatWithCharges = this.addProperty(ConfigTypes.STRING, "EnchantTooltip.Format.WithCharges",
        GRAY.wrap("• " + GENERIC_DESCRIPTION + " " + GENERIC_CHARGES),
        "Sets description format for enchantments with charges enabled."
    );

    private final ConfigProperty<Map<String, ChargesFormat>> chargesByAmountFormat = this.addProperty(ConfigTypes.forMapWithLowerKeys(CHARGES_FORMAT_CONFIG_TYPE),
        "EnchantTooltip.Format.Charges-By-Percent",
        getDefaultChargesFormat(),
        "Sets charges format based on percent of max. charges amount."
    );

    public boolean isForBooksOnly() {
        return this.booksOnly.get();
    }

    @NotNull
    public String getTooltipFormat() {
        return this.formatDefault.get();
    }

    @NotNull
    public String getTooltipFormatWithCharges() {
        return this.formatWithCharges.get();
    }

    @Nullable
    public ChargesFormat getTooltipChargesFormat(int percent) {
        return this.chargesByAmountFormat.get().values().stream()
            .filter(other -> other.isAboveThreshold(percent))
            .max(Comparator.comparingInt(ChargesFormat::getThreshold)).orElse(null);
    }

    @NotNull
    private static Map<String, ChargesFormat> getDefaultChargesFormat() {
        Map<String, ChargesFormat> map = new LinkedHashMap<>();

        map.put("zero", new ChargesFormat(0, SOFT_RED.wrap("(" + GENERIC_AMOUNT + "⚡)")));
        map.put("low", new ChargesFormat(25, SOFT_ORANGE.wrap("(" + GENERIC_AMOUNT + "⚡)")));
        map.put("medium", new ChargesFormat(50, SOFT_YELLOW.wrap("(" + GENERIC_AMOUNT + "⚡)")));
        map.put("high", new ChargesFormat(75, SOFT_GREEN.wrap("(" + GENERIC_AMOUNT + "⚡)")));

        return map;
    }
}
