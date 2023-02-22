package su.nightexpress.excellentenchants.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {

    public static final JOption<Long> TASKS_ARROW_TRAIL_TICKS_INTERVAL = JOption.create("Tasks.Arrow_Trail.Tick_Interval", 1L,
        "Sets how often (in ticks) arrow trail particle effects will be spawned behind the arrow.");
    public static final JOption<Long> TASKS_PASSIVE_POTION_EFFECTS_APPLY_INTERVAL = JOption.create("Tasks.Passive_Potion_Effects.Apply_Interval", 150L,
        "Sets how often (in ticks) the plugin will apply permanent potion effects from enchanted items to an entity who wear them.",
        "This setting does NOT refreshes currently active effects, but only attempts to add them if absent."
    );

    public static final JOption<Boolean> ENCHANTMENTS_CHARGES_ENABLED = JOption.create("Enchantments.Charges.Enabled", false,
        "Enables the enchantment Charges feature.",
        Placeholders.URL_WIKI + "Charges-System");

    public static final JOption<TreeMap<Double, String>> ENCHANTMENTS_CHARGES_FORMAT = new JOption<TreeMap<Double, String>>("Enchantments.Charges.Format",
        (cfg, path, def) -> cfg.getSection(path).stream().collect(Collectors.toMap(k -> StringUtil.getDouble(k, 0), v -> Colorizer.apply(cfg.getString(path + "." + v, "")), (o,n) -> n, TreeMap::new)),
        () -> {
            TreeMap<Double, String> map = new TreeMap<>();
            map.put(0D, "#ff9a9a(" + Placeholders.GENERIC_AMOUNT + "⚡)");
            map.put(25D, "#ffc39a(" + Placeholders.GENERIC_AMOUNT + "⚡)");
            map.put(50D, "#f6ff9a(" + Placeholders.GENERIC_AMOUNT + "⚡)");
            map.put(75D, "#bcff9a(" + Placeholders.GENERIC_AMOUNT + "⚡)");
            return map;
        },
        "Enchantment charges format depends on amount of charges left (in percent).",
        "If you don't want to display charges, leave only keys with negative values.",
        "Use '" + Placeholders.GENERIC_AMOUNT + "' placeholder for amount of charges.")
        .setWriter((cfg, path, map) -> map.forEach((perc, str) -> cfg.set(path + "." + perc, str)));

    public static final JOption<ItemStack> ENCHANTMENTS_CHARGES_FUEL_ITEM = JOption.create("Enchantments.Charges.Fuel_Item",
        new ItemStack(Material.LAPIS_LAZULI),
        "Default item used to recharge item's enchantments on anvils.",
        "If you want different item for certain enchantments, you can do it in that enchantment configs.",
        "Item Options: " + Placeholders.URL_ENGINE_SCALER)
        .setWriter(JYML::setItem);

    public static final JOption<Set<String>> ENCHANTMENTS_DISABLED = JOption.create("Enchantments.Disabled",
        Set.of("enchant_name", "other_enchant"),
        "A list of enchantments, that will be disabled and removed from the game (server).",
        "Enchantment names are the same as enchantment file name in /enchants/ folder. ! Must be in lower_case !",
        "Example: To disable 'Explosive Arrows' you need to add 'explosive_arrows' here.")
        .mapReader(set -> set.stream().map(String::toLowerCase).collect(Collectors.toSet()));

    public static final JOption<Map<String, Set<String>>> ENCHANTMENTS_DISABLED_IN_WORLDS = new JOption<Map<String, Set<String>>>("Enchantments.Disabled_In_Worlds",
        (cfg, path, def) -> cfg.getSection(path).stream().collect(Collectors.toMap(k -> k, worldName -> cfg.getStringSet(path + "." + worldName))),
        () -> Map.of("your_world_name", Set.of("enchantment_name", "ice_aspect")),
        "Here you can disable certain enchantments in certain worlds.",
        "Enchantment names are the same as enchantment file name in /enchants/ folder. ! Must be in lower_case !",
        "To disable all enchantments for a world, use '" + Placeholders.WILDCARD + "' instead of enchantment names.")
        .setWriter((cfg, path, map) -> map.forEach((world, enchants) -> cfg.set(path + "." + world, enchants)));

    public static final JOption<Integer> ENCHANTMENTS_DISPLAY_MODE = JOption.create("Enchantments.Display.Mode", 1,
        "Sets how enchantment names and descriptions will be handled on items.",
        "1 = Plain modification of item's lore (lore changes are real and persistent).",
        "2 = Packet modification of item's lore (no real changes are made to the items). Requires ProtocolLib.",
        "Plain mode is faster, but may not reflect all changes immediately.",
        "Packet mode is slower, but instantly reflect all changes. In creative mode, there is a chance for lore duplication.");

    public static final JOption<Boolean> ENCHANTMENTS_DESCRIPTION_ENABLED = JOption.create("Enchantments.Description.Enabled", true,
        "When 'true', adds the enchantment description to item lore under enchantment names.",
        "Note #1: You must have ProtocolLib installed for this feature to work (as well as for enchantments name display).",
        "Note #2: Description is not shown while you're in Creative gamemode.");

    public static final JOption<String> ENCHANTMENTS_DESCRIPTION_FORMAT = JOption.create("Enchantments.Description.Format",
        "&8▸ " + Placeholders.GENERIC_DESCRIPTION,
        "Sets the global enchantment description format.");

    public static final JOption<Integer> ENCHANTMENTS_ITEM_CUSTOM_MAX = JOption.create("Enchantments.Item.Max_Custom_Enchants", 3,
        "How many of custom enchantments the item can contain at the same time?");

    public static final JOption<Boolean> ENCHANTMENTS_ITEM_SWORD_ENCHANTS_TO_AXES = JOption.create("Enchantments.Item.Sword_Enchants_To_Axes", true,
        "Set this to 'true' to allow Sword enchantments for Axes.");

    public static final JOption<Boolean> ENCHANTMENTS_ITEM_BOW_ENCHANTS_TO_CROSSBOW = JOption.create("Enchantments.Item.Bow_Enchants_To_Crossbows", true,
        "Set this to 'true' to allow Bow enchantments for Crossbows.");

    public static final JOption<Boolean> ENCHANTMENTS_ITEM_CHESTPLATE_ENCHANTS_TO_ELYTRA = JOption.create("Enchantments.Item.Chestplate_Enchants_To_Elytra", false,
        "Set this to 'true' to allow Chestplate enchantments for Elytras.");

    public static final JOption<Boolean> ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS = JOption.create("Enchantments.Entity.Apply_Passive_Enchants_To_Mobs", true,
        "When enabled, passive enchantments (permanent potion effects, regeneration, etc.) will be applied to mobs as well.",
        "Disable this if you're experiencing performance issues.");

    private static final JOption<Map<ObtainType, ObtainSettings>> OBTAIN_SETTINGS = new JOption<Map<ObtainType, ObtainSettings>>("Enchantments.Obtaining",
        (cfg, path, def) -> Stream.of(ObtainType.values()).collect(Collectors.toMap(k -> k, v -> ObtainSettings.read(cfg, path + "." + v.getPathName()))),
        () -> Stream.of(ObtainType.values()).collect(Collectors.toMap(k -> k, v -> new ObtainSettings(true, 4, 80D, 0, 2))),
        "Settings for the different ways of obtaining enchantments.")
        .setWriter((cfg, path, map) -> map.forEach((type, settings) -> ObtainSettings.write(cfg, path + "." + type.getPathName(), settings)));

    @NotNull
    public static Optional<ObtainSettings> getObtainSettings(@NotNull ObtainType obtainType) {
        ObtainSettings settings = OBTAIN_SETTINGS.get().get(obtainType);
        return settings == null || !settings.isEnabled() ? Optional.empty() : Optional.of(settings);
    }
}
