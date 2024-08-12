package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;

import java.util.*;
import java.util.stream.Collectors;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Config {

    public static final String DIR_MENU     = "/menu/";
    public static final String DIR_ENCHANTS = "/enchants/";

    public static final ConfigValue<Long> CORE_PROJECTILE_PARTICLE_INTERVAL = ConfigValue.create("Core.Projectile_Particles_Tick_Interval",
        1L,
        "Sets how often (in ticks) enchantment particle effects will be spawned behind projectiles.",
        "[Increase for performance; Decrease for better visuals]",
        "[20 ticks = 1 second]",
        "[Default is 1]"
    );

    public static final ConfigValue<Long> CORE_PASSIVE_ENCHANTS_TRIGGER_INTERVAL = ConfigValue.create("Core.Passive_Enchants_Trigger_Interval",
        100L,
        "Sets how often (in ticks) passive enchantment effects will trigger on all alive and loaded entities.",
        "For best results it's recommened to keep this value lower, but at the same rate as enchantment's 'Trigger_Interval' option.",
        "=".repeat(15) + " EXAMPLES " + "=".repeat(15),
        "==> Global (this): 100 ticks; Regrowth: 200 ticks; Saturation: 300 ticks;",
        "==> Global (this): 50 ticks, Regrowth: 100 ticks; Saturation: 150 ticks;",
        "[Increase for performance; Decrease for more smooth effect]",
        "[20 ticks = 1 second]",
        "[Default is 100]"
    );

    public static final ConfigValue<Boolean> CORE_PASSIVE_ENCHANTS_FOR_MOBS = ConfigValue.create("Core.Apply_Passive_Enchants_To_Mobs",
        true,
        "Sets whether or not mobs can have passive enchantment effects (such as permanent potion effects, regeneration, etc.).",
        "[Enable for enhanced gameplay; Disable for performance]",
        "[Default is true]"
    );

    public static final ConfigValue<Integer> CORE_ITEM_ENCHANT_LIMIT = ConfigValue.create("Core.Item_Enchantments_Limit",
        3,
        "Sets max. amount of custom enchantments per item.",
        "[Default is 3]"
    );

    public static final ConfigValue<Boolean> CORE_SWORD_ENCHANTS_TO_AXES = ConfigValue.create("Core.Sword_Enchants_To_Axes",
        true,
        "Sets whether or not Sword enchantments can be applied on Axes.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> CORE_BOW_ENCHANTS_TO_CROSSBOW = ConfigValue.create("Core.Bow_Enchants_To_Crossbows",
        true,
        "Sets whether or not Bow enchantments can be applied on Crossbows.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> CORE_CHESTPLATE_ENCHANTS_TO_ELYTRA = ConfigValue.create("Core.Chestplate_Enchants_To_Elytra",
        false,
        "Sets whether or not Chestplate enchantments can be applied on Elytras.",
        "[Default is false]"
    );



    public static final ConfigValue<Set<String>> ENCHANTMENTS_DISABLED_LIST = ConfigValue.forSet("Enchantments.Disabled.List",
        String::toLowerCase,
        FileConfig::set,
        Lists.newSet("example_name", "custom_sharpness"),
        "Put here CUSTOM enchantment names that you want to disable and remove completely.",
        "Enchantment names are equal to their config file names in the '" + DIR_ENCHANTS + "' directory.",
        "[*] Reboot required when changed!",
        "[**] Disabled enchantments will be removed from all items forever!"
    );

    public static final ConfigValue<Map<String, Set<String>>> ENCHANTMENTS_DISABLED_IN_WORLDS = ConfigValue.forMap("Enchantments.Disabled.ByWorld",
        String::toLowerCase,
        (cfg, path, worldName) -> cfg.getStringSet(path + "." + worldName).stream().map(String::toLowerCase).collect(Collectors.toSet()),
        (cfg, path, map) -> map.forEach((world, enchants) -> cfg.set(path + "." + world, enchants)),
        () -> Map.of(
            "your_world_name", Lists.newSet("enchantment_name", "ice_aspect"),
            "another_world", Lists.newSet("another_enchantment", "ice_aspect")
        ),
        "Put here CUSTOM enchantment names that you want to disable in specific worlds.",
        "To disable all enchantments for a world, use '" + WILDCARD + "' instead of enchantment names.",
        "Enchantment names are equal to their config file names in the '" + DIR_ENCHANTS + "' directory.",
        "[*] This setting only disables enchantment effects, not the enchantment distribution there!"
    );

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_COMPONENT = ConfigValue.create("Enchantments.Display.Name.Component.Name",
        GENERIC_VALUE,
        "Enchantment name display component for name format."
    );

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_LEVEL_COMPONENT = ConfigValue.create("Enchantments.Display.Name.Component.Level",
        " " + GENERIC_VALUE,
        "Enchantment level display component for name format."
    );

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_CHARGES_COMPONENT = ConfigValue.create("Enchantments.Display.Name.Component.Charges",
        " " + GENERIC_VALUE,
        "Enchantment charges display component for name format."
    );

    public static final ConfigValue<Boolean> ENCHANTMENTS_DISPLAY_DESCRIPTION_ENABLED = ConfigValue.create("Enchantments.Display.Description.Enabled",
        true,
        "When 'true', adds the enchantment description to item lore under enchantment names."
    );

    public static final ConfigValue<Boolean> ENCHANTMENTS_DISPLAY_DESCRIPTION_BOOKS_ONLY = ConfigValue.create("Enchantments.Display.Description.Books_Only",
        false,
        "Sets whether or not only enchanted books will have enchantment descriptions."
    );

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_DESCRIPTION_FORMAT = ConfigValue.create("Enchantments.Display.Description.Format",
        LIGHT_GRAY.enclose("• " + GENERIC_NAME + GENERIC_CHARGES + ": " + GENERIC_DESCRIPTION),
        "Sets enchantment description format."
    ).onRead(str -> str.replace(ENCHANTMENT_NAME, GENERIC_NAME).replace(ENCHANTMENT_CHARGES, GENERIC_CHARGES));




    public static final ConfigValue<Boolean> ENCHANTMENTS_CHARGES_ENABLED = ConfigValue.create("Enchantments.Charges.Enabled",
        false,
        "Enables Enchantment Charges feature.",
        "When enabled in the first time, make sure to check enchantments configs for new 'Charges' section.",
        WIKI_CHRAGES
    );

    public static final ConfigValue<TreeMap<Integer, String>> ENCHANTMENTS_CHARGES_FORMAT = ConfigValue.forTreeMap("Enchantments.Charges.Format",
        raw -> NumberUtil.getInteger(raw, 0),
        (cfg, path, value) -> cfg.getString(path + "." + value, GENERIC_AMOUNT),
        (cfg, path, map) -> map.forEach((perc, str) -> cfg.set(path + "." + perc, str)),
        () -> {
            TreeMap<Integer, String> map = new TreeMap<>();
            map.put(0, LIGHT_RED.enclose("(" + GENERIC_AMOUNT + "⚡)"));
            map.put(25, LIGHT_ORANGE.enclose("(" + GENERIC_AMOUNT + "⚡)"));
            map.put(50, LIGHT_YELLOW.enclose("(" + GENERIC_AMOUNT + "⚡)"));
            map.put(75, LIGHT_GREEN.enclose("(" + GENERIC_AMOUNT + "⚡)"));
            return map;
        },
        "Enchantment charges format depends on amount of charges left (in percent).",
        "If you don't want to display charges, leave only keys with negative values.",
        "Use '" + GENERIC_AMOUNT + "' placeholder for charges amount."
    );

    public static final ConfigValue<Boolean> ENCHANTMENTS_CHARGES_COMPARE_TYPE_ONLY = ConfigValue.create("Enchantments.Charges.Compare_Material_Only",
        false,
        "When enabled, only item material will be checked to determine if an item can be used as an enchantment fuel.",
        "When disabled, it will compare the whole item meta including name, lore, model data etc.",
        "[Default is false]"
    );

    public static final ConfigValue<ItemStack> ENCHANTMENTS_CHARGES_FUEL_ITEM = ConfigValue.create("Enchantments.Charges.Fuel_Item",
        new ItemStack(Material.LAPIS_LAZULI),
        "Default item used to recharge item's enchantments on anvils.",
        "If you want different item for certain enchantments, you can do it in that enchantment configs.",
        "Item Options: " + WIKI_ITEMS_URL
    );


    public static boolean isDescriptionEnabled() {
        return ENCHANTMENTS_DISPLAY_DESCRIPTION_ENABLED.get();
    }

    public static boolean isChargesEnabled() {
        return ENCHANTMENTS_CHARGES_ENABLED.get();
    }
}
