package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;
import su.nightexpress.excellentenchants.util.ChargesFormat;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.bukkit.NightItem;

import java.util.Map;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class Config {

    public static final ConfigValue<Long> ARROW_EFFECTS_TICK_INTERVAL = ConfigValue.create("Arrow_Effects.Tick_Interval",
        1L,
        "Sets tick interval for arrow & trident particle effect trails added by enchantments.",
        "[Increase for performance; Decrease for better visuals]",
        "[20 ticks = 1 second]",
        "[Default is 1]"
    );

    public static final ConfigValue<Integer> PASSIVE_ENCHANTS_TICK_INTERVAL = ConfigValue.create("Passive_Enchants.Interval",
        1,
        "Tick interval for passive enchantments (in seconds).",
        "Passive enchants depends on entity's 'ticksLived' value, changing this value may result in passive enchants not triggered correctly.",
        "[Default is 1]"
    );

    public static final ConfigValue<Boolean> PASSIVE_ENCHANTS_ALLOW_FOR_MOBS = ConfigValue.create("Passive_Enchants.AllowForMobs",
        true,
        "Controls whether mobs are affected by effects of passive enchantments.",
        "[Disable for performance; Enable for better experience]",
        "[Default is true]"
    );



    public static final ConfigValue<Boolean> DESCRIPTION_ENABLED = ConfigValue.create("Description.Enabled",
        true,
        "When 'true', adds the enchantment description to item lore under enchantment names."
    );

    public static final ConfigValue<Boolean> DESCRIPTION_BOOKS_ONLY = ConfigValue.create("Description.Books_Only",
        false,
        "Sets whether or not only enchanted books will have enchantment descriptions."
    );

    public static final ConfigValue<String> DESCRIPTION_FORMAT_DEFAULT = ConfigValue.create("Description.Format.Default",
        SOFT_GRAY.wrap("• " + GENERIC_DESCRIPTION),
        "Sets description format for enchantments without charges."
    );

    public static final ConfigValue<String> DESCRIPTION_FORMAT_CHARGES = ConfigValue.create("Description.Format.WithCharges",
        SOFT_GRAY.wrap("• " + GENERIC_DESCRIPTION + " " + GENERIC_CHARGES),
        "Sets description format for enchantments with charges enabled."
    );



    public static final ConfigValue<Boolean> CHARGES_ENABLED = ConfigValue.create("Charges.Enabled",
        false,
        "Enables the Charges feature: " + WIKI_CHRAGES
    );

    public static final ConfigValue<Map<String, ChargesFormat>> CHARGES_FORMAT = ConfigValue.forMapById("Charges.Format",
        ChargesFormat::read,
        map -> {
            map.put("zero", new ChargesFormat(0, SOFT_RED.wrap("(" + GENERIC_AMOUNT + "⚡)")));
            map.put("low", new ChargesFormat(25, SOFT_ORANGE.wrap("(" + GENERIC_AMOUNT + "⚡)")));
            map.put("medium", new ChargesFormat(50, SOFT_YELLOW.wrap("(" + GENERIC_AMOUNT + "⚡)")));
            map.put("high", new ChargesFormat(75, SOFT_GREEN.wrap("(" + GENERIC_AMOUNT + "⚡)")));
        },
        "Sets charges format based on percent of max. charges amount."
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


    public static boolean isDescriptionEnabled() {
        return DESCRIPTION_ENABLED.get();
    }

    public static boolean isChargesEnabled() {
        return CHARGES_ENABLED.get();
    }
}
