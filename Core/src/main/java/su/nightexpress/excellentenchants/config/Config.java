package su.nightexpress.excellentenchants.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.*;
import java.util.stream.Collectors;

public class Config {

    public static long TASKS_ARROW_TRAIL_TICKS_INTERVAL;
    public static final JOption<Integer> TASKS_PASSIVE_POTION_EFFECTS_APPLY_INTERVAL = JOption.create("General.Tasks.Passive_Potion_Effects.Apply_Interval", 150,
        "Sets how often (in ticks) the plugin will apply permanent potion effects from enchanted items to an entity who wear them.",
        "This setting does NOT refreshes currently active effects, but only attempts to add them if absent."
    );

    public static  Set<String> ENCHANTMENTS_DISABLED;
    public static Map<String, Set<String>> ENCHANTMENTS_DISABLED_IN_WORLDS;
    public static  boolean     ENCHANTMENTS_DESCRIPTION_ENABLED;
    private static String      ENCHANTMENTS_DESCRIPTION_FORMAT;

    public static int     ENCHANTMENTS_ITEM_CUSTOM_MAX;
    public static boolean ENCHANTMENTS_ITEM_AXES_AS_SWORDS;
    public static boolean ENCHANTMENTS_ITEM_CROSSBOWS_AS_BOWS;
    public static boolean ENCHANTMENTS_ITEM_ELYTRA_AS_CHESTPLATE;
    public static boolean ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS;

    private static Map<ObtainType, ObtainSettings> OBTAIN_SETTINGS;
    private static Map<String, EnchantTier>        TIERS;

    public static void load(@NotNull ExcellentEnchants plugin) {
        JYML cfg = plugin.getConfig();
        cfg.initializeOptions(Config.class);

        String path = "General.Tasks.";
        TASKS_ARROW_TRAIL_TICKS_INTERVAL = cfg.getLong(path + "Arrow_Trails.Ticks_Interval", 1);

        path = "General.Enchantments.";
        cfg.addMissing(path + "Disabled_In_Worlds.my_world", Collections.singletonList(Placeholders.WILDCARD));
        cfg.addMissing(path + "Disabled_In_Worlds.other_world", Arrays.asList("enchant_name", "another_enchant"));

        ENCHANTMENTS_DISABLED = cfg.getStringSet(path + "Disabled").stream().map(String::toLowerCase).collect(Collectors.toSet());
        ENCHANTMENTS_DISABLED_IN_WORLDS = new HashMap<>();
        for (String worldName : cfg.getSection(path + "Disabled_In_Worlds")) {
            ENCHANTMENTS_DISABLED_IN_WORLDS.put(worldName, cfg.getStringSet(path + "Disabled_In_Worlds." + worldName)
                .stream().map(String::toLowerCase).collect(Collectors.toSet()));
        }

        ENCHANTMENTS_DESCRIPTION_ENABLED = cfg.getBoolean(path + "Description.Enabled");
        ENCHANTMENTS_DESCRIPTION_FORMAT = StringUtil.color(cfg.getString(path + "Description.Format", ""));

        path = "General.Enchantments.Item.";
        ENCHANTMENTS_ITEM_CUSTOM_MAX = cfg.getInt(path + "Max_Custom_Enchants", 3);
        ENCHANTMENTS_ITEM_AXES_AS_SWORDS = cfg.getBoolean(path + "Axes_As_Swords");
        ENCHANTMENTS_ITEM_CROSSBOWS_AS_BOWS = cfg.getBoolean(path + "Crossbows_As_Bows");
        ENCHANTMENTS_ITEM_ELYTRA_AS_CHESTPLATE = cfg.getBoolean(path + "Elytra_As_Chestplate");

        path = "General.Enchantments.Entity.";
        ENCHANTMENTS_ENTITY_PASSIVE_FOR_MOBS = cfg.getBoolean(path + "Passive_Enchants_Applied_To_Mobs");

        OBTAIN_SETTINGS = new HashMap<>();
        for (ObtainType obtainType : ObtainType.values()) {
            String path2 = "General." + obtainType.getPathName() + ".";

            cfg.addMissing(path2 + "Enabled", true);
            cfg.addMissing(path2 + "Enchantments.Total_Maximum", 4);
            cfg.addMissing(path2 + "Enchantments.Custom_Generation_Chance", 50D);
            cfg.addMissing(path2 + "Enchantments.Custom_Minimum", 0);
            cfg.addMissing(path2 + "Enchantments.Custom_Maximum", 2);

            if (!cfg.getBoolean(path2 + "Enabled")) continue;

            int enchantsTotalMax = cfg.getInt(path2 + "Enchantments.Total_Maximum", 4);
            double enchantsCustomGenerationChance = cfg.getDouble(path2 + "Enchantments.Custom_Generation_Chance", 50D);
            int enchantsCustomMin = cfg.getInt(path2 + "Enchantments.Custom_Minimum", 0);
            int enchantsCustomMax = cfg.getInt(path2 + "Enchantments.Custom_Maximum", 2);

            ObtainSettings settings = new ObtainSettings(enchantsTotalMax, enchantsCustomGenerationChance, enchantsCustomMin, enchantsCustomMax);
            OBTAIN_SETTINGS.put(obtainType, settings);
        }

        setupTiers(plugin);
    }

    private static void setupTiers(@NotNull ExcellentEnchants plugin) {
        // Reloading tiers will reset their lists with enchants = break the plugin mechanics
        if (ExcellentEnchants.isLoaded) return;

        JYML cfg = plugin.getConfig();
        TIERS = new HashMap<>();

        // No tiers defined, setup a default one.
        // Every enchantment must have a tier.
        if (cfg.getSection("Tiers").isEmpty()) {
            plugin.info("No tiers defined! Creating a default one for you...");
            cfg.set("Tiers.default.Name", "&7Default");
            cfg.set("Tiers.default.Color", "&7");
            for (ObtainType obtainType : ObtainType.values()) {
                cfg.set("Tiers.default.Obtain_Chance." + obtainType.name(), 100D);
            }
        }

        // Load existing tiers.
        for (String sId : cfg.getSection("Tiers")) {
            String path = "Tiers." + sId + ".";
            cfg.addMissing(path + "Priority", 0);

            int priority = cfg.getInt(path + "Priority");
            String name = cfg.getString(path + "Name", sId);
            String color = cfg.getString(path + "Color", "&f");
            Map<ObtainType, Double> chance = new HashMap<>();

            for (ObtainType obtainType : ObtainType.values()) {
                cfg.addMissing(path + "Obtain_Chance." + obtainType.name(), 50D);

                double chanceType = cfg.getDouble(path + "Obtain_Chance." + obtainType.name());
                chance.put(obtainType, chanceType);
            }

            EnchantTier tier = new EnchantTier(sId, priority, name, color, chance);
            TIERS.put(tier.getId(), tier);
        }

        plugin.info("Tiers Loaded: " + TIERS.size());
    }

    public static boolean isEnchantmentDisabled(@NotNull ExcellentEnchant enchant, @NotNull String world) {
        Set<String> disabled = ENCHANTMENTS_DISABLED_IN_WORLDS.getOrDefault(world, Collections.emptySet());
        return disabled.contains(enchant.getKey().getKey()) || disabled.contains(Placeholders.WILDCARD);
    }

    @Nullable
    public static EnchantTier getTierById(@NotNull String id) {
        return TIERS.get(id.toLowerCase());
    }

    @NotNull
    public static Collection<EnchantTier> getTiers() {
        return TIERS.values();
    }

    @NotNull
    public static List<String> getTierIds() {
        return new ArrayList<>(TIERS.keySet());
    }

    @Nullable
    public static EnchantTier getTierByChance(@NotNull ObtainType obtainType) {
        Map<EnchantTier, Double> map = getTiers().stream().collect(Collectors.toMap(k -> k, v -> v.getChance(obtainType)));
        return Rnd.get(map);
    }

    @Nullable
    public static ObtainSettings getObtainSettings(@NotNull ObtainType obtainType) {
        return OBTAIN_SETTINGS.get(obtainType);
    }

    @NotNull
    public static List<String> formatDescription(@NotNull List<String> description) {
        return new ArrayList<>(description.stream().map(line -> ENCHANTMENTS_DESCRIPTION_FORMAT.replace("%description%", line)).toList());
    }
}
