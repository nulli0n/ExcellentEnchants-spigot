package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.DistributionMode;
import su.nightexpress.excellentenchants.api.DistributionWay;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.hook.HookPlugin;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.wrapper.UniInt;

import java.util.*;
import java.util.stream.Collectors;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Config {

    public static final String DIR_MENU = "/menu/";
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
        "[Default is 3]");

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
        Set.of("example_name", "custom_sharpness"),
        "Put here CUSTOM enchantment names that you want to disable and remove completely.",
        "Enchantment names are equal to their config file names in '" + DIR_ENCHANTS + "' folder.",
        "[*] You MUST REBOOT your server for disabled enchantments to have effect.",
        "[*] Once enchantment is disabled, it will be removed from all items in the world on next load!"
    );

    public static final ConfigValue<Map<String, Set<String>>> ENCHANTMENTS_DISABLED_IN_WORLDS = ConfigValue.forMap("Enchantments.Disabled.ByWorld",
        String::toLowerCase,
        (cfg, path, worldName) -> cfg.getStringSet(path + "." + worldName).stream().map(String::toLowerCase).collect(Collectors.toSet()),
        (cfg, path, map) -> map.forEach((world, enchants) -> cfg.set(path + "." + world, enchants)),
        () -> Map.of(
            "your_world_name", Set.of("enchantment_name", "ice_aspect"),
            "another_world", Set.of("another_enchantment", "ice_aspect")
        ),
        "Put here CUSTOM enchantment names that you want to disable in specific worlds.",
        "To disable all enchantments for a world, use '" + WILDCARD + "' instead of enchantment names.",
        "Enchantment names are equal to their config file names in '" + DIR_ENCHANTS + "' folder.",
        VANILLA_DISTRIBUTION_HEADER,
        "Enchantments will have no effect, but will appear in the world.",
        CUSTOM_DISTRIBUTION_HEADER,
        "Enchantments will have no effect and won't appear in the world."
    );

    public static final ConfigValue<DistributionMode> DISTRIBUTION_MODE = ConfigValue.create("Enchantments.Distribution.Mode",
        DistributionMode.class, DistributionMode.VANILLA,
        "Sets in a which way new enchantments will be distributed to the worlds.",
        "Allowed values: " + StringUtil.inlineEnum(DistributionMode.class, ", "),
        "=".repeat(15) + " ! WARNING ! " + "=".repeat(15),
        "You MUST REBOOT your server when changing this. Otherwise result is unpredictable.",
        VANILLA_DISTRIBUTION_HEADER,
        "[+] Very simple to use, almost no need to configure anything.",
        "[+] Handled by the server, automatically supports all possible ways to get enchantments.",
        "[+] Very accurate generation, repsects all vanilla game mechanics.",
        "[-] Customization is almost non-existent.",
        CUSTOM_DISTRIBUTION_HEADER,
        "[+] Very flexible and customizable.",
        "[+] Possibility for new ways to generate / obtain enchantments.",
        "[-] Might be difficult to configure and balance everything.",
        "[-] Enchantment generation is not such accurate as vanilla does."
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_SINGLE_ENCHANT_IN_VILLAGER_BOOKS = ConfigValue.create("Enchantments.Distribution.Custom.Single_Enchant_In_Villager_Books",
        true,
        "When enabled, enchanted books in villager trades will have no more than 1 enchantment (vanilla or custom one).");

    private static final ConfigValue<Map<DistributionWay, DistributionWaySettings>> DISTRIBUTION_WAY_SETTINGS = ConfigValue.forMap("Enchantments.Distribution.Custom.Ways",
        id -> StringUtil.getEnum(id, DistributionWay.class).orElse(null),
        (cfg, path, def) -> DistributionWaySettings.read(cfg, path + "." + def),
        (cfg, path, map) -> map.forEach((type, settings) -> settings.write(cfg, path + "." + type.name())),
        () -> Map.of(
            DistributionWay.ENCHANTING, new DistributionWaySettings(true, 5, 75, UniInt.of(0, 2)),
            DistributionWay.FISHING, new DistributionWaySettings(true, 4, 45, UniInt.of(0, 2)),
            DistributionWay.LOOT_GENERATION, new DistributionWaySettings(true, 4, 80, UniInt.of(0, 2)),
            DistributionWay.MOB_EQUIPMENT, new DistributionWaySettings(true, 4, 35, UniInt.of(0, 2)),
            DistributionWay.VILLAGER, new DistributionWaySettings(true, 4, 60, UniInt.of(0, 2))
        ),
        "Settings for the different ways of obtaining enchantments."
    );



    public static final ConfigValue<Integer> ENCHANTMENTS_DISPLAY_MODE = ConfigValue.create("Enchantments.Display.Mode",
        1,
        "Sets how enchantment names and descriptions will be handled on items.",
        "=".repeat(15) + " AVAILABLE VALUES " + "=".repeat(15),
        "1 = Plain modification of item's lore (lore changes are real and persistent).",
        "2 = Packet modification of item's lore (no real changes are made to the items). Requires " + HookPlugin.PROTOCOL_LIB + " to be installed.",
        "",
        "Plain mode is faster, but may not reflect all changes immediately.",
        "Packet mode is slower, but instantly reflect all changes. In creative mode, there is a chance for lore duplication."
    );

    public static final ConfigValue<Boolean> ENCHANTMENTS_DISPLAY_NAME_HIDE_1ST_LEVEL = ConfigValue.create("Enchantments.Display.Name.Hide_1st_Level",
        true,
        "Hides enchantment level component from name format for level 1 enchantments.");

    public static final ConfigValue<Map<Rarity, String>> ENCHANTMENTS_DISPLAY_NAME_RARITY_FORMAT = ConfigValue.forMap("Enchantments.Display.Name.Rarity",
        (id) -> StringUtil.getEnum(id, Rarity.class).orElse(null),
        (cfg, path, id) -> cfg.getString(path + "." + id, GENERIC_NAME),
        (cfg, path, map) -> map.forEach((rarity, format) -> cfg.set(path + "." + rarity.name(), format)),
        () -> Map.of(
            Rarity.COMMON, WHITE.enclose(GENERIC_NAME),
            Rarity.UNCOMMON, LIGHT_GREEN.enclose(GENERIC_NAME),
            Rarity.RARE, LIGHT_CYAN.enclose(GENERIC_NAME),
            Rarity.VERY_RARE, LIGHT_ORANGE.enclose(GENERIC_NAME)
        ),
        "Sets enchantment name format depends on enchantment rarity.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_CURSE_FORMAT = ConfigValue.create("Enchantments.Display.Name.Curse",
        LIGHT_RED.enclose(GENERIC_NAME),
        "Sets cursed enchantments name format.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_FORMAT = ConfigValue.create("Enchantments.Display.Name.Format",
        ENCHANTMENT_NAME + ENCHANTMENT_LEVEL + ENCHANTMENT_CHARGES,
        "Enchantment name format created from name components.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_COMPONENT_NAME = ConfigValue.create("Enchantments.Display.Name.Component.Name",
        GENERIC_VALUE,
        "Enchantment name display component for name format.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_COMPONENT_LEVEL = ConfigValue.create("Enchantments.Display.Name.Component.Level",
        " " + GENERIC_VALUE,
        "Enchantment level display component for name format.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_NAME_COMPONENT_CHARGES = ConfigValue.create("Enchantments.Display.Name.Component.Charges",
        " " + GENERIC_VALUE,
        "Enchantment charges display component for name format.");

    public static final ConfigValue<Boolean> ENCHANTMENTS_DISPLAY_DESCRIPTION_ENABLED = ConfigValue.create("Enchantments.Display.Description.Enabled",
        true,
        "When 'true', adds the enchantment description to item lore under enchantment names.",
        "For Display-Mode = 2 description is not shown while you're in Creative gamemode.");

    public static final ConfigValue<Boolean> ENCHANTMENTS_DISPLAY_DESCRIPTION_BOOKS_ONLY = ConfigValue.create("Enchantments.Display.Description.Books_Only",
        false,
        "Sets whether or not only enchanted books will have enchantment descriptions.");

    public static final ConfigValue<String> ENCHANTMENTS_DISPLAY_DESCRIPTION_FORMAT = ConfigValue.create("Enchantments.Display.Description.Format",
        LIGHT_GRAY.enclose("• " + GENERIC_DESCRIPTION),
        "Sets enc" +
            "hantment description format.");




    public static final ConfigValue<Boolean> ENCHANTMENTS_CHARGES_ENABLED = ConfigValue.create("Enchantments.Charges.Enabled",
        false,
        "Enables Enchantment Charges feature.",
        "When enabled in the first time, make sure to check enchantments configs for new 'Charges' section.",
        URL_CHRAGES);

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
        "Use '" + GENERIC_AMOUNT + "' placeholder for charges amount.");

    public static final ConfigValue<Boolean> ENCHANTMENTS_CHARGES_COMPARE_TYPE_ONLY = ConfigValue.create("Enchantments.Charges.Compare_Material_Only",
        false,
        "When enabled, only item material will be checked to determine if an item can be used as an enchantment fuel.",
        "When disabled, it will compare the whole item meta including name, lore, model data etc.",
        "[Default is false]");

    public static final ConfigValue<ItemStack> ENCHANTMENTS_CHARGES_FUEL_ITEM = ConfigValue.create("Enchantments.Charges.Fuel_Item",
        new ItemStack(Material.LAPIS_LAZULI),
        "Default item used to recharge item's enchantments on anvils.",
        "If you want different item for certain enchantments, you can do it in that enchantment configs.",
        "Item Options: " + WIKI_ITEMS_URL);

    @NotNull
    public static Optional<DistributionWaySettings> getDistributionWaySettings(@NotNull DistributionWay way) {
        DistributionWaySettings settings = DISTRIBUTION_WAY_SETTINGS.get().get(way);
        return settings == null || !settings.isEnabled() ? Optional.empty() : Optional.of(settings);
    }

    public static void loadRarityWeights(@NotNull FileConfig config) {
        for (Rarity rarity : Rarity.values()) {
            int weight = ConfigValue.create("Enchantments.Distribution.Custom.Rarity_Weights." + rarity.name(), rarity.getWeight()).read(config);
            rarity.setWeight(weight);
        }
    }

    public static boolean isVanillaDistribution() {
        return Config.DISTRIBUTION_MODE.get() == DistributionMode.VANILLA;
    }

    public static boolean isCustomDistribution() {
        return Config.DISTRIBUTION_MODE.get() == DistributionMode.CUSTOM;
    }
}
