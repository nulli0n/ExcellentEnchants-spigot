package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.excellentenchants.api.config.DistributionConfig;
import su.nightexpress.excellentenchants.api.item.ItemSetId;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;

public class EnchantDefaults {

    private static final int COMMON = 10;
    private static final int UNCOMMON = 5;
    private static final int RARE = 2;
    private static final int VERY_RARE = 1;

    public static void load(@NotNull File dataDir) {
        String dirPath = dataDir.getAbsolutePath() + ConfigBridge.DIR_ENCHANTS;

        loadDefaults();

        EnchantRegistry.getDataMap().forEach((id, data) -> {
            // Skip disabled enchantments to keep the directory clean.
            if (DistributionConfig.isDisabled(id)) return;

            File file = new File(dirPath, id + FileConfig.EXTENSION);
            boolean exists = file.exists();

            FileConfig config = new FileConfig(file);
            EnchantDefinition definition = data.getDefinition();
            EnchantDistribution distribution = data.getDistribution();

            if (!exists) {
                config.set("Definition", definition);
                config.set("Distribution", distribution);
                config.saveChanges();
                return;
            }

            definition = EnchantDefinition.read(config, "Definition");
            distribution = EnchantDistribution.read(config, "Distribution");

            EnchantRegistry.addData(id, definition, distribution, data.isCurse());

            config.saveChanges();
        });
    }

    private static void loadDefaults() {
        loadDefaultArmors();
        loadDefaultBow();
        loadDefaultFishing();
        loadDefaultTool();
        loadDefaultUniversal();
        loadDefaultWeapon();
    }

    private static void loadDefaultArmors() {
        EnchantRegistry.addData(EnchantId.COLD_STEEL, EnchantDefinition.builder("Cold Steel", 3)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.DARKNESS_CLOAK, EnchantDefinition.builder("Darkness Cloak", 3)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.ELEMENTAL_PROTECTION, EnchantDefinition.builder("Elemental Protection", 4)
            .description("Reduces potion and elemental damage by " + GENERIC_AMOUNT + "%.")
            .weight(COMMON)
            .items(ItemSetId.ARMOR)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.FIRE_SHIELD, EnchantDefinition.builder("Fire Shield", 4)
            .description(TRIGGER_CHANCE + "% chance to ignite attackers for " + GENERIC_DURATION + "s.")
            .weight(RARE)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.FLAME_WALKER, EnchantDefinition.builder("Flame Walker", 2)
            .description("Ability to walk on lava, immunity to magma damage.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOOTS)
            .exclusives(EnchantKeys.FROST_WALKER)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));

        EnchantRegistry.addData(EnchantId.HARDENED, EnchantDefinition.builder("Hardened", 2)
            .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) when damaged.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.ICE_SHIELD, EnchantDefinition.builder("Ice Shield", 3)
            .description(TRIGGER_CHANCE + "% chance to freeze and apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(COMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.JUMPING, EnchantDefinition.builder("Jumping", 2)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.KAMIKADZE, EnchantDefinition.builder("Kamikadze", 3)
            .description(TRIGGER_CHANCE + "% chance to explode on death.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));

        EnchantRegistry.addData(EnchantId.NIGHT_VISION, EnchantDefinition.builder("Night Vision", 1)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(VERY_RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_SPECIAL));

        EnchantRegistry.addData(EnchantId.REBOUND, EnchantDefinition.builder("Rebound", 1)
            .description("Effect of landing on a slime block.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .exclusives(EnchantKeys.FEATHER_FALLING)
            .build(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.REGROWTH, EnchantDefinition.builder("Regrowth", 4)
            .description("Restores " + GENERIC_AMOUNT + "❤ every few seconds.")
            .weight(RARE)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));

        EnchantRegistry.addData(EnchantId.SATURATION, EnchantDefinition.builder("Saturation", 2)
            .description("Restores " + GENERIC_AMOUNT + " food points every few seconds.")
            .weight(RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL));

        EnchantRegistry.addData(EnchantId.SPEED, EnchantDefinition.builder("Speed", 2)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .build(), EnchantDistribution.regular(TradeType.DESERT_SPECIAL));

        EnchantRegistry.addData(EnchantId.STOPPING_FORCE, EnchantDefinition.builder("Stopping Force", 3)
            .description(TRIGGER_CHANCE + "% chance to reduce knockback for " + GENERIC_AMOUNT + "%.")
            .weight(UNCOMMON)
            .items(ItemSetId.LEGGINGS)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.WATER_BREATHING, EnchantDefinition.builder("Water Breathing", 1)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(VERY_RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL));
    }

    private static void loadDefaultBow() {
        EnchantRegistry.addData(EnchantId.BOMBER, EnchantDefinition.builder("Bomber", 3)
            .description(TRIGGER_CHANCE + "% chance to shoot a TNT ignited for " + GENERIC_TIME + "s.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(
                EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST),
                EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER
            )
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));

        EnchantRegistry.addData(EnchantId.CONFUSING_ARROWS, EnchantDefinition.builder("Confusing Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.DARKNESS_ARROWS, EnchantDefinition.builder("Darkness Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.DRAGONFIRE_ARROWS, EnchantDefinition.builder("Dragonfire Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have Dragonfire Effect (R=" + GENERIC_RADIUS + ", " + GENERIC_DURATION + "s).")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_SPECIAL));

        EnchantRegistry.addData(EnchantId.ELECTRIFIED_ARROWS, EnchantDefinition.builder("Electrified Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for an arrow to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.ENDER_BOW, EnchantDefinition.builder("Ender Bow", 1)
            .description("Shoots ender pearls instead of arrows.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW)
            .exclusives(EnchantKeys.custom(EnchantId.BOMBER), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER)
            .build(), EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL));

        EnchantRegistry.addData(EnchantId.EXPLOSIVE_ARROWS, EnchantDefinition.builder("Explosive Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance to shoot an explosive arrow.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));

        EnchantRegistry.addData(EnchantId.FLARE, EnchantDefinition.builder("Flare", 1)
            .description(TRIGGER_CHANCE + "% chance to create a torch where arrow lands.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.treasure(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.GHAST, EnchantDefinition.builder("Ghast", 1)
            .description("Shoots fireballs instead of arrows.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.HOVER, EnchantDefinition.builder("Hover", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL));

        EnchantRegistry.addData(EnchantId.LINGERING, EnchantDefinition.builder("Lingering", 3)
            .description(TRIGGER_CHANCE + "% chance for tipped arrows to generate a lingering effect.")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.POISONED_ARROWS, EnchantDefinition.builder("Poisoned Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.SNIPER, EnchantDefinition.builder("Sniper", 2)
            .description("Increases projectile speed by " + GENERIC_AMOUNT + "%")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL));

        EnchantRegistry.addData(EnchantId.VAMPIRIC_ARROWS, EnchantDefinition.builder("Vampiric Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance to restore " + GENERIC_AMOUNT + "❤ on arrow hit.")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_SPECIAL));

        EnchantRegistry.addData(EnchantId.WITHERED_ARROWS, EnchantDefinition.builder("Withered Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.custom(EnchantId.ENDER_BOW), EnchantKeys.custom(EnchantId.GHAST), EnchantKeys.custom(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SNOW_SPECIAL));
    }

    private static void loadDefaultFishing() {
        EnchantRegistry.addData(EnchantId.AUTO_REEL, EnchantDefinition.builder("Auto Reel", 1)
            .description("Automatically reels in a hook on bite.")
            .weight(VERY_RARE)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));

        EnchantRegistry.addData(EnchantId.CURSE_OF_DROWNED, EnchantDefinition.builder("Curse of Drowned", 3)
            .description(TRIGGER_CHANCE + "% chance to fish up a Drowned Zombie.")
            .weight(UNCOMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON), true);

        EnchantRegistry.addData(EnchantId.DOUBLE_CATCH, EnchantDefinition.builder("Double Catch", 3)
            .description("Increases amount of caught item by x2 with " + TRIGGER_CHANCE + "% chance.")
            .weight(RARE)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.RIVER_MASTER, EnchantDefinition.builder("River Master", 5)
            .description("Increases casting distance.")
            .weight(COMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.SEASONED_ANGLER, EnchantDefinition.builder("Seasoned Angler", 3)
            .description("Increases amount of XP gained from fishing by " + GENERIC_AMOUNT + "%.")
            .weight(UNCOMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.SURVIVALIST, EnchantDefinition.builder("Survivalist", 1)
            .description("Automatically cooks fish if what is caught is raw.")
            .weight(RARE)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_SPECIAL));
    }

    private static void loadDefaultTool() {
        EnchantRegistry.addData(EnchantId.BLAST_MINING, EnchantDefinition.builder("Blast Mining", 5)
            .description(TRIGGER_CHANCE + "% chance to mine blocks by explosion.")
            .weight(RARE)
            .items(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.custom(EnchantId.VEINMINER), EnchantKeys.custom(EnchantId.TUNNEL))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.GLASSBREAKER, EnchantDefinition.builder("Glass Breaker", 1)
            .description("Breaks glass instantly")
            .weight(COMMON)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.HASTE, EnchantDefinition.builder("Haste", 3)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.LUCKY_MINER, EnchantDefinition.builder("Lucky Miner", 3)
            .description(TRIGGER_CHANCE + "% chance to gain " + GENERIC_AMOUNT + "% more XP from ores.")
            .weight(UNCOMMON)
            .items(ItemSetId.PICKAXE)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));

        EnchantRegistry.addData(EnchantId.REPLANTER, EnchantDefinition.builder("Replanter", 1)
            .description("Automatically replant crops on right click and when harvest.")
            .weight(VERY_RARE)
            .items(ItemSetId.HOE)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.SILK_CHEST, EnchantDefinition.builder("Silk Chest", 1)
            .description("Drop chests and saves all its content.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.MINING_TOOLS)
            .primaryItems(ItemSetId.AXE)
            .exclusives(EnchantKeys.custom(EnchantId.SMELTER))
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.SILK_SPAWNER, EnchantDefinition.builder("Silk Spawner", 1)
            .description(TRIGGER_CHANCE + "% chance to mine spawner.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.MINING_TOOLS)
            .primaryItems(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.custom(EnchantId.SMELTER))
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));

        EnchantRegistry.addData(EnchantId.SMELTER, EnchantDefinition.builder("Smelter", 5)
            .description("Smelts mined blocks with " + TRIGGER_CHANCE + "% chance.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .exclusives(EnchantKeys.custom(EnchantId.SILK_SPAWNER), EnchantKeys.custom(EnchantId.SILK_CHEST), EnchantKeys.SILK_TOUCH)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.TELEKINESIS, EnchantDefinition.builder("Telekinesis", 1)
            .description("Moves all blocks loot directly to your inventory.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));

        EnchantRegistry.addData(EnchantId.TREEFELLER, EnchantDefinition.builder("Treefeller", 1)
            .description("Cuts down an entire tree.")
            .weight(RARE)
            .items(ItemSetId.AXE)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_SPECIAL));

        EnchantRegistry.addData(EnchantId.TUNNEL, EnchantDefinition.builder("Tunnel", 3)
            .description("Mines multiple blocks at once in a certain shape.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.MINING_TOOLS)
            .primaryItems(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.custom(EnchantId.VEINMINER), EnchantKeys.custom(EnchantId.BLAST_MINING))
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL));

        EnchantRegistry.addData(EnchantId.VEINMINER, EnchantDefinition.builder("Veinminer", 3)
            .description("Mines up to " + GENERIC_AMOUNT + " blocks of the ore vein at once.")
            .weight(RARE)
            .items(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.custom(EnchantId.BLAST_MINING), EnchantKeys.custom(EnchantId.TUNNEL))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL));
    }

    private static void loadDefaultUniversal() {
        EnchantRegistry.addData(EnchantId.CURSE_OF_BREAKING, EnchantDefinition.builder("Curse of Breaking", 3)
            .description(TRIGGER_CHANCE + "% chance to consume extra " + GENERIC_AMOUNT + " durability points.")
            .weight(COMMON)
            .items(ItemSetId.BREAKABLE)
            .exclusives(EnchantKeys.UNBREAKING)
            .build(), EnchantDistribution.treasure(TradeType.SAVANNA_COMMON), true);

        EnchantRegistry.addData(EnchantId.CURSE_OF_FRAGILITY, EnchantDefinition.builder("Curse of Fragility", 1)
            .description("Prevents an item from being grindstoned or anviled.")
            .weight(COMMON)
            .items(ItemSetId.BREAKABLE)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_COMMON), true);

        EnchantRegistry.addData(EnchantId.CURSE_OF_MEDIOCRITY, EnchantDefinition.builder("Curse of Mediocrity", 3)
            .description(TRIGGER_CHANCE + "% chance to disenchant item drops.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.TOOLS_WEAPONS)
            .primaryItems(ItemSetId.ALL_WEAPON)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_COMMON), true);

        EnchantRegistry.addData(EnchantId.CURSE_OF_MISFORTUNE, EnchantDefinition.builder("Curse of Misfortune", 3)
            .description(TRIGGER_CHANCE + "% chance to have no drops from blocks or mobs.")
            .weight(UNCOMMON)
            .items(ItemSetId.TOOLS_WEAPONS)
            .exclusives(EnchantKeys.FORTUNE, EnchantKeys.LOOTING)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_COMMON), true);

        EnchantRegistry.addData(EnchantId.RESTORE, EnchantDefinition.builder("Restore", 3)
            .description(TRIGGER_CHANCE + "% chance to save item from breaking back to " + GENERIC_AMOUNT + "%")
            .weight(RARE)
            .items(ItemSetId.BREAKABLE)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.SOULBOUND, EnchantDefinition.builder("Soulbound", 1)
            .description("Protects from being dropped on death.")
            .weight(RARE)
            .items(ItemSetId.BREAKABLE)
            .exclusives(EnchantKeys.VANISHING_CURSE)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL));
    }

    private static void loadDefaultWeapon() {
        EnchantRegistry.addData(EnchantId.BANE_OF_NETHERSPAWN, EnchantDefinition.builder("Bane of Netherspawn", 5)
            .description("Inflicts " + GENERIC_DAMAGE + "❤ more damage to nether mobs.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.BLINDNESS, EnchantDefinition.builder("Blindness", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));

        EnchantRegistry.addData(EnchantId.CONFUSION, EnchantDefinition.builder("Confusion", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.CURE, EnchantDefinition.builder("Cure", 3)
            .description(TRIGGER_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.CURSE_OF_DEATH, EnchantDefinition.builder("Curse of Death", 3)
            .description("When killing players, you have a chance of dying too.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), true);

        EnchantRegistry.addData(EnchantId.CUTTER, EnchantDefinition.builder("Cutter", 3)
            .description(TRIGGER_CHANCE + "% chance to throw away enemy''s armor and damage it for " + GENERIC_DAMAGE + "%.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.DECAPITATOR, EnchantDefinition.builder("Decapitator", 2)
            .description(TRIGGER_CHANCE + "% chance to obtain player's or mob's head.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_SPECIAL));

        EnchantRegistry.addData(EnchantId.DOUBLE_STRIKE, EnchantDefinition.builder("Double Strike", 2)
            .description(TRIGGER_CHANCE + "% chance to inflict double damage.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));

        EnchantRegistry.addData(EnchantId.EXHAUST, EnchantDefinition.builder("Exhaust", 4)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.ICE_ASPECT, EnchantDefinition.builder("Ice Aspect", 3)
            .description("Freezes and applies " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));

        EnchantRegistry.addData(EnchantId.INFERNUS, EnchantDefinition.builder("Infernus", 3)
            .description("Launched trident will ignite the enemy for " + GENERIC_TIME + "s. on hit.")
            .weight(COMMON)
            .items(ItemSetId.TRIDENT)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.NIMBLE, EnchantDefinition.builder("Nimble", 1)
            .description("Moves all entity's loot directly to your inventory.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));

        EnchantRegistry.addData(EnchantId.PARALYZE, EnchantDefinition.builder("Paralyze", 5)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));

        EnchantRegistry.addData(EnchantId.RAGE, EnchantDefinition.builder("Rage", 2)
            .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.ROCKET, EnchantDefinition.builder("Rocket", 3)
            .description(TRIGGER_CHANCE + "% chance to launch your enemy into the space.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_COMMON));

//        EnchantRegistry.addData(EnchantId.SURPRISE, EnchantDefinition.builder("Surprise", 3)
//            .description(TRIGGER_CHANCE + "% chance to apply random potion effect to enemy on hit.")
//            .weight(UNCOMMON)
//            .supportedItems(ItemSetId.SWORDS_AXES)
//            .primaryItems(ItemSetId.SWORD)
//            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));

        EnchantRegistry.addData(EnchantId.SWIPER, EnchantDefinition.builder("Swiper", 3)
            .description(TRIGGER_CHANCE + "% chance to steal " + GENERIC_AMOUNT + " XP from players.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.TEMPER, EnchantDefinition.builder("Temper", 5)
            .description("Inflicts " + GENERIC_AMOUNT + "% more damage for each " + GENERIC_RADIUS + "❤ missing.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON));

        EnchantRegistry.addData(EnchantId.THRIFTY, EnchantDefinition.builder("Thrifty", 3)
            .description(TRIGGER_CHANCE + "% chance for mobs to drop spawn egg.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));

        EnchantRegistry.addData(EnchantId.THUNDER, EnchantDefinition.builder("Thunder", 5)
            .description(TRIGGER_CHANCE + "% chance to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.VAMPIRE, EnchantDefinition.builder("Vampire", 3)
            .description(TRIGGER_CHANCE + "% chance to heal for " + GENERIC_AMOUNT + "❤ on hit.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON));

        EnchantRegistry.addData(EnchantId.VENOM, EnchantDefinition.builder("Venom", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON));

        EnchantRegistry.addData(EnchantId.VILLAGE_DEFENDER, EnchantDefinition.builder("Village Defender", 5)
            .description("Inflicts " + GENERIC_AMOUNT + "❤ more damage to all pillagers.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON));

        EnchantRegistry.addData(EnchantId.WISDOM, EnchantDefinition.builder("Wisdom", 5)
            .description("Mobs drops x" + GENERIC_MODIFIER + " more XP.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON));

        EnchantRegistry.addData(EnchantId.WITHER, EnchantDefinition.builder("Wither", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON));
    }
}
