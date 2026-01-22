package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.configuration.AbstractConfig;
import su.nightexpress.nightcore.configuration.ConfigProperty;
import su.nightexpress.nightcore.configuration.ConfigTypes;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import static su.nightexpress.excellentenchants.EnchantsPlaceholders.URL_WIKI_ITEMS;
import static su.nightexpress.excellentenchants.EnchantsPlaceholders.WIKI_CHRAGES;

public class Config extends AbstractConfig {

    @Override
    public void load(@NotNull FileConfig config) {
        if (config.contains("Description.Enabled")) {
            config.set("Modules.EnchantTooltip", config.getBoolean("Description.Enabled"));
            config.remove("Description.Enabled");
        }

        super.load(config);
    }

    private final ConfigProperty<Boolean> featuresEnchantTooltip = this.addProperty(ConfigTypes.BOOLEAN, "Modules.EnchantTooltip",
        true,
        "When 'true', adds the enchantment description to item lore under enchantment names."
    );

    public static final ConfigValue<Boolean> CHARGES_ENABLED = ConfigValue.create("Charges.Enabled",
        false,
        "Enables the Charges feature: " + WIKI_CHRAGES
    );

    public static final ConfigValue<Boolean> CHARGES_FUEL_IGNORE_META = ConfigValue.create("Charges.Fuel.Ignore_Meta",
        false,
        "Controls if item meta (such as display name, lore, model data, etc.) should be ignored when checks if item is valid fuel.",
        "[Default is false]"
    );

    public static final ConfigValue<NightItem> CHARGES_FUEL_ITEM = ConfigValue.create("Charges.Fuel.Item",
        NightItem.fromType(Material.LAPIS_LAZULI),
        "Default item used to fuel enchantments on anvils.",
        "Enchantments can have their own custom fuel items.",
        URL_WIKI_ITEMS
    );

    public boolean isEnchantTooltipEnabled() {
        return this.featuresEnchantTooltip.get();
    }

    public static boolean isChargesEnabled() {
        return CHARGES_ENABLED.get();
    }
}
