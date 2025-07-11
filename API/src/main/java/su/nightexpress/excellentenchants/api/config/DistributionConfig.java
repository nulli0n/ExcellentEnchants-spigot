package su.nightexpress.excellentenchants.api.config;

import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.EnchantBlacklist;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;

import java.util.Map;
import java.util.Set;

import static su.nightexpress.nightcore.util.Placeholders.WILDCARD;

public class DistributionConfig {

    public static final ConfigValue<Boolean> CUSTOM_NAMESPACE_ENABLED = ConfigValue.create("Custom_Namespace.Enabled",
        true,
        "Controls whether plugin will use custom '" + ConfigBridge.NAMESPACE + "' namespace for new enchantments.",
        "[*] Toggle only if you're experiencing compatibility issues.",
        "[*] All enchantments with old (previous) namespace will be removed forever!"
    );

    public static final ConfigValue<Set<String>> DISABLED_GLOBAL = ConfigValue.create("Disabled.Global",
        Lists.newSet("example_name", "custom_sharpness"),
        "Put here CUSTOM enchantment names that you want to disable and remove completely.",
        "Enchantment names are equal to their config file names in the '" + ConfigBridge.DIR_ENCHANTS + "' directory.",
        "[*] Server reboot required.",
        "[*] Disabled enchantments will be removed from all items forever!"
    ).whenRead(set -> Lists.modify(set, String::toLowerCase));

    public static final ConfigValue<Map<String, EnchantBlacklist>> DISABLED_BY_WORLD = ConfigValue.forMapById("Disabled.ByWorld",
        EnchantBlacklist::read,
        map -> {
            map.put("your_world_name", new EnchantBlacklist(Lists.newSet("enchantment_name", "ice_aspect")));
            map.put("another_world", new EnchantBlacklist(Lists.newSet("another_enchantment", "ice_aspect")));
        },
        "Put here CUSTOM enchantment names that you want to disable in specific worlds.",
        "To disable all enchantments for a world, use '" + WILDCARD + "' instead of enchantment names.",
        "Enchantment names are equal to their config file names in the '" + ConfigBridge.DIR_ENCHANTS + "' directory.",
        "[*] This setting only disables enchantment effects, not the enchantment distribution there!"
    );

    public static final ConfigValue<Integer> ANVIL_ENCHANT_LIMIT = ConfigValue.create("Anvil.Enchant_Limit",
        5,
        "Prevents item from being enchanted using anvil if it already contains specific amount of custom enchantments.",
        "[Default is 5]"
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_ENCHANTING = ConfigValue.create("Distribution.Enchanting_Table",
        true,
        "Controls whether excellent enchants can be obtained from Enchanting Table. .",
        "https://minecraft.wiki/w/Enchanting#Enchanting_table",
        "[*] Server reboot required.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_TRADING = ConfigValue.create("Distribution.Trading",
        true,
        "Controls whether excellent enchants can be sold by villagers.",
        "https://minecraft.wiki/w/Trading#Librarian",
        "[*] Server reboot required.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_MOB_EQUIPMENT = ConfigValue.create("Distribution.Mob_Equipment",
        true,
        "Controls whether excellent enchants can be found on spawned mobs' equipment.",
        "https://minecraft.wiki/w/Armor#Armor_equipped_on_mobs",
        "[*] Server reboot required.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_TRADE_EQUIPMENT = ConfigValue.create("Distribution.Trade_Equipment",
        true,
        "Controls whether excellent enchants can be found on equipment sold by villagers.",
        "https://minecraft.wiki/w/Trading#Trade_offers",
        "[*] Works only if Villager Trade Rebalance is DISABLED.",
        "[*] Server reboot required.",
        "[Default is true]"
    );

    public static final ConfigValue<Boolean> DISTRIBUTION_RANDOM_LOOT = ConfigValue.create("Distribution.Random_Loot",
        true,
        "Controls whether excellent enchants can be found on naturally generated equipment from loot tables.",
        "https://minecraft.wiki/w/Loot_table",
        "[*] Server reboot required.",
        "[Default is true]"
    );

    public static boolean isDisabled(@NotNull String id) {
        return DISABLED_GLOBAL.get().contains(id.toLowerCase());
    }

    @Nullable
    public static EnchantBlacklist getDisabled(@NotNull World world) {
        return DistributionConfig.DISABLED_BY_WORLD.get().get(BukkitThing.getValue(world));
    }
}
