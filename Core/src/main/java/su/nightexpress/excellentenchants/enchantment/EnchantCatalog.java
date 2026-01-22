package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsFiles;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantDefinition;
import su.nightexpress.excellentenchants.api.EnchantDistribution;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.item.ItemSetDefaults;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.excellentenchants.bridge.EnchantCatalogEntry;
import su.nightexpress.excellentenchants.enchantment.armor.*;
import su.nightexpress.excellentenchants.enchantment.bow.*;
import su.nightexpress.excellentenchants.enchantment.fishing.*;
import su.nightexpress.excellentenchants.enchantment.tool.*;
import su.nightexpress.excellentenchants.enchantment.universal.*;
import su.nightexpress.excellentenchants.enchantment.weapon.*;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static su.nightexpress.excellentenchants.EnchantsPlaceholders.*;
import static su.nightexpress.excellentenchants.enchantment.EnchantCatalog.Weight.*;

public enum EnchantCatalog implements EnchantCatalogEntry {

    COLD_STEEL(() -> EnchantDefinition.builder("Cold Steel", 3)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(),
        () -> EnchantDistribution.regular(TradeType.SNOW_COMMON),
        ColdSteelEnchant::new
    ),
    DARKNESS_CLOAK(() -> EnchantDefinition.builder("Darkness Cloak", 3)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(),
        () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON),
        DarknessCloakEnchant::new
    ),
    DRAGON_HEART(() -> EnchantDefinition.builder("Dragon Heart", 5)
        .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(),
        () -> EnchantDistribution.regular(TradeType.JUNGLE_SPECIAL),
        DragonHeartEnchant::new
    ),
    ELEMENTAL_PROTECTION(() -> EnchantDefinition.builder("Elemental Protection", 4)
        .description("Reduces potion and elemental damage by " + GENERIC_AMOUNT + "%.")
        .weight(COMMON)
        .items(ItemSetDefaults.ARMOR)
        .build(),
        () -> EnchantDistribution.regular(TradeType.SWAMP_COMMON), ElementalProtectionEnchant::new
    ),
    FIRE_SHIELD(() -> EnchantDefinition.builder("Fire Shield", 4)
        .description(TRIGGER_CHANCE + "% chance to ignite attackers for " + GENERIC_DURATION + "s.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), FireShieldEnchant::new
    ),
    FLAME_WALKER(() -> EnchantDefinition.builder("Flame Walker", 2)
        .description("Ability to walk on lava, immunity to magma damage.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.BOOTS)
        .exclusives(EnchantsKeys.FROST_WALKER)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), FlameWalkerEnchant::new
    ),
    HARDENED(() -> EnchantDefinition.builder("Hardened", 2)
        .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) when damaged.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), HardenedEnchant::new
    ),
    ICE_SHIELD(() -> EnchantDefinition.builder("Ice Shield", 3)
        .description(TRIGGER_CHANCE + "% chance to freeze and apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_COMMON), IceShieldEnchant::new
    ),
    LIGHTWEIGHT(() -> EnchantDefinition.builder("Lightweight", 1)
        .description("You can safely jump on turtle eggs, farmlands and big dripleaf.")
        .weight(COMMON)
        .items(ItemSetDefaults.BOOTS)
        .build(),
        () -> EnchantDistribution.regular(TradeType.SNOW_COMMON),
        LightweightEnchant::new
    ),
    JUMPING(() -> EnchantDefinition.builder("Jumping", 2)
        .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
        .weight(RARE)
        .items(ItemSetDefaults.BOOTS)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), JumpingEnchant::new
    ),
    KAMIKADZE(() -> EnchantDefinition.builder("Kamikadze", 3)
        .description(TRIGGER_CHANCE + "% chance to explode on death.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(), () -> EnchantDistribution.regular(TradeType.JUNGLE_COMMON), KamikadzeEnchant::new
    ),
    NIGHT_VISION(() -> EnchantDefinition.builder("Night Vision", 1)
        .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.HELMET)
        .build(), () -> EnchantDistribution.treasure(TradeType.TAIGA_SPECIAL), NightVisionEnchant::new
    ),
    REBOUND(() -> EnchantDefinition.builder("Rebound", 1)
        .description("Effect of landing on a slime block.")
        .weight(RARE)
        .items(ItemSetDefaults.BOOTS)
        .exclusives(EnchantsKeys.FEATHER_FALLING)
        .build(), () -> EnchantDistribution.treasure(TradeType.SWAMP_COMMON), ReboundEnchant::new
    ),
    REGROWTH(() -> EnchantDefinition.builder("Regrowth", 4)
        .description("Restores " + GENERIC_AMOUNT + "❤ every few seconds.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.CHESTPLATE_ELYTRA)
        .primaryItems(ItemSetDefaults.CHESTPLATE)
        .build(), () -> EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), RegrowthEnchant::new
    ),
    SATURATION(() -> EnchantDefinition.builder("Saturation", 2)
        .description("Restores " + GENERIC_AMOUNT + " food points every few seconds.")
        .weight(RARE)
        .items(ItemSetDefaults.HELMET)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL), SaturationEnchant::new
    ),
    SPEED(() -> EnchantDefinition.builder("Speed", 2)
        .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
        .weight(RARE)
        .items(ItemSetDefaults.BOOTS)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_SPECIAL), SpeedyEnchant::new
    ),
    STOPPING_FORCE(() -> EnchantDefinition.builder("Stopping Force", 3)
        .description(TRIGGER_CHANCE + "% chance to reduce knockback for " + GENERIC_AMOUNT + "%.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.LEGGINGS)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), StoppingForceEnchant::new
    ),
    WATER_BREATHING(() -> EnchantDefinition.builder("Water Breathing", 1)
        .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.HELMET)
        .build(), () -> EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL), WaterBreathingEnchant::new
    ),
    BOMBER(() -> EnchantDefinition.builder("Bomber", 3)
        .description(TRIGGER_CHANCE + "% chance to shoot a TNT ignited for " + GENERIC_TIME + "s.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.FLAME, EnchantsKeys.PUNCH, EnchantsKeys.POWER)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), BomberEnchant::new
    ),
    ENDER_BOW(() -> EnchantDefinition.builder("Ender Bow", 1)
        .description("Shoots ender pearls instead of arrows.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.BOW)
        .exclusives(EnchantsKeys.create(BOMBER.getId()), EnchantsKeys.FLAME, EnchantsKeys.PUNCH, EnchantsKeys.POWER)
        .build(), () -> EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL), EnderBowEnchant::new
    ),
    GHAST(() -> EnchantDefinition.builder("Ghast", 1)
        .description("Shoots fireballs instead of arrows.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(BOMBER.getId()), EnchantsKeys.FLAME, EnchantsKeys.PUNCH, EnchantsKeys.POWER)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_COMMON), GhastEnchant::new
    ),
    CONFUSING_ARROWS(() -> EnchantDefinition.builder("Confusing Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
        .weight(COMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_COMMON), ConfusingArrowsEnchant::new
    ),
    DARKNESS_ARROWS(() -> EnchantDefinition.builder("Darkness Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
        .weight(COMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_COMMON), DarknessArrowsEnchant::new
    ),
    DRAGONFIRE_ARROWS(() -> EnchantDefinition.builder("Dragonfire Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have Dragonfire Effect (R=" + GENERIC_RADIUS + ", " + GENERIC_DURATION + "s).")
        .weight(RARE)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_SPECIAL), DragonfireArrowsEnchant::new
    ),
    ELECTRIFIED_ARROWS(() -> EnchantDefinition.builder("Electrified Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for an arrow to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), ElectrifiedArrowsEnchant::new
    ),
    EXPLOSIVE_ARROWS(() -> EnchantDefinition.builder("Explosive Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance to shoot an explosive arrow.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_COMMON), ExplosiveArrowsEnchant::new
    ),
    FLARE(() -> EnchantDefinition.builder("Flare", 1)
        .description(TRIGGER_CHANCE + "% chance to create a torch where arrow lands.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.BOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.treasure(TradeType.SNOW_COMMON), FlareEnchant::new
    ),
    HOVER(() -> EnchantDefinition.builder("Hover", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
        .weight(COMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), HoverEnchant::new
    ),
    LINGERING(() -> EnchantDefinition.builder("Lingering", 3)
        .description(TRIGGER_CHANCE + "% chance for tipped arrows to generate a lingering effect.")
        .weight(RARE)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), LingeringEnchant::new
    ),
    POISONED_ARROWS(() -> EnchantDefinition.builder("Poisoned Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_COMMON), PoisonedArrowsEnchant::new
    ),
    SNIPER(() -> EnchantDefinition.builder("Sniper", 2)
        .description("Increases projectile speed by " + GENERIC_AMOUNT + "%")
        .weight(COMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), SniperEnchant::new
    ),
    VAMPIRIC_ARROWS(() -> EnchantDefinition.builder("Vampiric Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance to restore " + GENERIC_AMOUNT + "❤ on arrow hit.")
        .weight(RARE)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_SPECIAL), VampiricArrowsEnchant::new
    ),
    WITHERED_ARROWS(() -> EnchantDefinition.builder("Withered Arrows", 3)
        .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.BOW_CROSSBOW)
        .exclusives(EnchantsKeys.create(ENDER_BOW.getId()), EnchantsKeys.create(GHAST.getId()), EnchantsKeys.create(BOMBER.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_SPECIAL), WitheredArrowsEnchant::new
    ),
    AUTO_REEL(() -> EnchantDefinition.builder("Auto Reel", 1)
        .description("Automatically reels in a hook on bite.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), AutoReelEnchant::new, false, true
    ),
    CURSE_OF_DROWNED(() -> EnchantDefinition.builder("Curse of Drowned", 3)
        .description(TRIGGER_CHANCE + "% chance to fish up a Drowned Zombie.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.treasure(TradeType.SWAMP_COMMON), CurseOfDrownedEnchant::new, true
    ),
    DOUBLE_CATCH(() -> EnchantDefinition.builder("Double Catch", 3)
        .description("Increases amount of caught item by x2 with " + TRIGGER_CHANCE + "% chance.")
        .weight(RARE)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), DoubleCatchEnchant::new
    ),
    RIVER_MASTER(() -> EnchantDefinition.builder("River Master", 5)
        .description("Increases casting distance.")
        .weight(COMMON)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), RiverMasterEnchant::new
    ),
    SEASONED_ANGLER(() -> EnchantDefinition.builder("Seasoned Angler", 3)
        .description("Increases amount of XP gained from fishing by " + GENERIC_AMOUNT + "%.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), SeasonedAnglerEnchant::new
    ),
    SURVIVALIST(() -> EnchantDefinition.builder("Survivalist", 1)
        .description("Automatically cooks fish if what is caught is raw.")
        .weight(RARE)
        .items(ItemSetDefaults.FISHING_ROD)
        .build(), () -> EnchantDistribution.treasure(TradeType.SNOW_SPECIAL), SurvivalistEnchant::new
    ),
    BLAST_MINING(() -> EnchantDefinition.builder("Blast Mining", 5)
        .description(TRIGGER_CHANCE + "% chance to mine blocks by explosion.")
        .weight(RARE)
        .items(ItemSetDefaults.PICKAXE)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), BlastMiningEnchant::new
    ),
    GLASSBREAKER(() -> EnchantDefinition.builder("Glass Breaker", 1)
        .description("Breaks glass instantly")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.TOOL)
        .primaryItems(ItemSetDefaults.MINING_TOOLS)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), GlassbreakerEnchant::new
    ),
    HASTE(() -> EnchantDefinition.builder("Haste", 3)
        .description("Grants " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect when mining blocks.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.TOOL)
        .primaryItems(ItemSetDefaults.MINING_TOOLS)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), HasteEnchant::new
    ),
    LUCKY_MINER(() -> EnchantDefinition.builder("Lucky Miner", 3)
        .description(TRIGGER_CHANCE + "% chance to gain " + GENERIC_AMOUNT + "% more XP from ores.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.PICKAXE)
        .build(), () -> EnchantDistribution.regular(TradeType.JUNGLE_COMMON), LuckyMinerEnchant::new
    ),
    REPLANTER(() -> EnchantDefinition.builder("Replanter", 1)
        .description("Automatically replant crops on right click and when harvest.")
        .weight(VERY_RARE)
        .items(ItemSetDefaults.HOE)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), ReplanterEnchant::new
    ),
    SILK_CHEST(() -> EnchantDefinition.builder("Silk Chest", 1)
        .description("Drop chests and saves all its content.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.MINING_TOOLS)
        .primaryItems(ItemSetDefaults.AXE)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), SilkChestEnchant::new, false, true
    ),
    SILK_SPAWNER(() -> EnchantDefinition.builder("Silk Spawner", 1)
        .description(TRIGGER_CHANCE + "% chance to mine spawner.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.MINING_TOOLS)
        .primaryItems(ItemSetDefaults.PICKAXE)
        .build(), () -> EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), SilkSpawnerEnchant::new
    ),
    SMELTER(() -> EnchantDefinition.builder("Smelter", 5)
        .description("Smelts mined blocks with " + TRIGGER_CHANCE + "% chance.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.TOOL)
        .primaryItems(ItemSetDefaults.MINING_TOOLS)
        .exclusives(EnchantsKeys.create(SILK_SPAWNER.getId()), EnchantsKeys.create(SILK_CHEST.getId()), EnchantsKeys.SILK_TOUCH)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), SmelterEnchant::new
    ),
    TELEKINESIS(() -> EnchantDefinition.builder("Telekinesis", 1)
        .description("Moves all blocks loot directly to your inventory.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.TOOL)
        .primaryItems(ItemSetDefaults.MINING_TOOLS)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), TelekinesisEnchant::new
    ),
    TREEFELLER(() -> EnchantDefinition.builder("Treefeller", 1)
        .description("Cuts down an entire tree.")
        .weight(RARE)
        .items(ItemSetDefaults.AXE)
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_SPECIAL), TreefellerEnchant::new
    ),
    TUNNEL(() -> EnchantDefinition.builder("Tunnel", 3)
        .description("Mines multiple blocks at once in a certain shape.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.MINING_TOOLS)
        .primaryItems(ItemSetDefaults.PICKAXE)
        .exclusives(EnchantsKeys.create(BLAST_MINING.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL), TunnelEnchant::new
    ),
    VEINMINER(() -> EnchantDefinition.builder("Veinminer", 3)
        .description("Mines up to " + GENERIC_AMOUNT + " blocks of the ore vein at once.")
        .weight(RARE)
        .items(ItemSetDefaults.PICKAXE)
        .exclusives(EnchantsKeys.create(BLAST_MINING.getId()), EnchantsKeys.create(TUNNEL.getId()))
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), VeinminerEnchant::new
    ),
    CURSE_OF_BREAKING(() -> EnchantDefinition.builder("Curse of Breaking", 3)
        .description(TRIGGER_CHANCE + "% chance to consume extra " + GENERIC_AMOUNT + " durability points.")
        .weight(COMMON)
        .items(ItemSetDefaults.BREAKABLE)
        .exclusives(EnchantsKeys.UNBREAKING)
        .build(), () -> EnchantDistribution.treasure(TradeType.SAVANNA_COMMON), CurseOfBreakingEnchant::new, true
    ),
    CURSE_OF_FRAGILITY(() -> EnchantDefinition.builder("Curse of Fragility", 1)
        .description("Prevents an item from being grindstoned or anviled.")
        .weight(COMMON)
        .items(ItemSetDefaults.BREAKABLE)
        .build(), () -> EnchantDistribution.treasure(TradeType.TAIGA_COMMON), CurseOfFragilityEnchant::new, true
    ),
    CURSE_OF_MEDIOCRITY(() -> EnchantDefinition.builder("Curse of Mediocrity", 3)
        .description(TRIGGER_CHANCE + "% chance to disenchant item drops.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.TOOLS_WEAPONS)
        .primaryItems(ItemSetDefaults.ALL_WEAPON)
        .build(), () -> EnchantDistribution.treasure(TradeType.SNOW_COMMON), CurseOfMediocrityEnchant::new, true
    ),
    CURSE_OF_MISFORTUNE(() -> EnchantDefinition.builder("Curse of Misfortune", 3)
        .description(TRIGGER_CHANCE + "% chance to have no drops from blocks or mobs.")
        .weight(UNCOMMON)
        .items(ItemSetDefaults.TOOLS_WEAPONS)
        .exclusives(EnchantsKeys.FORTUNE, EnchantsKeys.LOOTING)
        .build(), () -> EnchantDistribution.treasure(TradeType.TAIGA_COMMON), CurseOfMisfortuneEnchant::new, true
    ),
    RESTORE(() -> EnchantDefinition.builder("Restore", 3)
        .description(TRIGGER_CHANCE + "% chance to save item from breaking back to " + GENERIC_AMOUNT + "%")
        .weight(RARE)
        .items(ItemSetDefaults.BREAKABLE)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), RestoreEnchant::new
    ),
    SOULBOUND(() -> EnchantDefinition.builder("Soulbound", 1)
        .description("Protects from being dropped on death.")
        .weight(RARE)
        .items(ItemSetDefaults.BREAKABLE)
        .exclusives(EnchantsKeys.VANISHING_CURSE)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), SoulboundEnchant::new
    ),
    BANE_OF_NETHERSPAWN(() -> EnchantDefinition.builder("Bane of Netherspawn", 5)
        .description("Inflicts " + GENERIC_DAMAGE + "❤ more damage to nether mobs.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), BaneOfNetherspawnEnchant::new
    ),
    BLINDNESS(() -> EnchantDefinition.builder("Blindness", 2)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_COMMON), BlindnessEnchant::new
    ),
    CONFUSION(() -> EnchantDefinition.builder("Confusion", 2)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_COMMON), ConfusionEnchant::new
    ),
    CURE(() -> EnchantDefinition.builder("Cure", 3)
        .description(TRIGGER_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), CureEnchant::new
    ),
    CURSE_OF_DEATH(() -> EnchantDefinition.builder("Curse of Death", 3)
        .description("When killing players, you have a chance of dying too.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), CurseOfDeathEnchant::new, true
    ),
    CUTTER(() -> EnchantDefinition.builder("Cutter", 3)
        .description(TRIGGER_CHANCE + "% chance to throw away enemy''s armor and damage it for " + GENERIC_DAMAGE + "%.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), CutterEnchant::new
    ),
    DECAPITATOR(() -> EnchantDefinition.builder("Decapitator", 2)
        .description(TRIGGER_CHANCE + "% chance to obtain player's or mob's head.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.treasure(TradeType.SNOW_SPECIAL), DecapitatorEnchant::new
    ),
    DOUBLE_STRIKE(() -> EnchantDefinition.builder("Double Strike", 2)
        .description(TRIGGER_CHANCE + "% chance to inflict double damage.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_COMMON), DoubleStrikeEnchant::new
    ),
    EXHAUST(() -> EnchantDefinition.builder("Exhaust", 4)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), ExhaustEnchant::new
    ),
    ICE_ASPECT(() -> EnchantDefinition.builder("Ice Aspect", 3)
        .description("Freezes and applies " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_COMMON), IceAspectEnchant::new
    ),
    INFERNUS(() -> EnchantDefinition.builder("Infernus", 3)
        .description("Launched trident will ignite the enemy for " + GENERIC_TIME + "s. on hit.")
        .weight(COMMON)
        .items(ItemSetDefaults.TRIDENT)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), InfernusEnchant::new
    ),
    NIMBLE(() -> EnchantDefinition.builder("Nimble", 1)
        .description("Moves all entity's loot directly to your inventory.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.JUNGLE_COMMON), NimbleEnchant::new
    ),
    PARALYZE(() -> EnchantDefinition.builder("Paralyze", 5)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_COMMON), ParalyzeEnchant::new
    ),
    RAGE(() -> EnchantDefinition.builder("Rage", 2)
        .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), RageEnchant::new
    ),
    ROCKET(() -> EnchantDefinition.builder("Rocket", 3)
        .description(TRIGGER_CHANCE + "% chance to launch your enemy into the space.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.treasure(TradeType.JUNGLE_COMMON), RocketEnchant::new
    ),
    SWIPER(() -> EnchantDefinition.builder("Swiper", 3)
        .description(TRIGGER_CHANCE + "% chance to steal " + GENERIC_AMOUNT + " XP from players.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_COMMON), SwiperEnchant::new
    ),
    TEMPER(() -> EnchantDefinition.builder("Temper", 5)
        .description("Inflicts " + GENERIC_AMOUNT + "% more damage for each " + GENERIC_RADIUS + "❤ missing.")
        .weight(VERY_RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.TAIGA_COMMON), TemperEnchant::new
    ),
    THRIFTY(() -> EnchantDefinition.builder("Thrifty", 3)
        .description(TRIGGER_CHANCE + "% chance for mobs to drop spawn egg.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), ThriftyEnchant::new
    ),
    THUNDER(() -> EnchantDefinition.builder("Thunder", 5)
        .description(TRIGGER_CHANCE + "% chance to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), ThunderEnchant::new
    ),
    VAMPIRE(() -> EnchantDefinition.builder("Vampire", 3)
        .description(TRIGGER_CHANCE + "% chance to heal for " + GENERIC_AMOUNT + "❤ on hit.")
        .weight(RARE)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SAVANNA_COMMON), VampireEnchant::new
    ),
    VENOM(() -> EnchantDefinition.builder("Venom", 2)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SWAMP_COMMON), VenomEnchant::new
    ),
    VILLAGE_DEFENDER(() -> EnchantDefinition.builder("Village Defender", 5)
        .description("Inflicts " + GENERIC_AMOUNT + "❤ more damage to all pillagers.")
        .weight(COMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.PLAINS_COMMON), VillageDefenderEnchant::new
    ),
    WISDOM(() -> EnchantDefinition.builder("Wisdom", 5)
        .description("Mobs drops x" + GENERIC_MODIFIER + " more XP.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.DESERT_COMMON), WisdomEnchant::new
    ),
    WITHER(() -> EnchantDefinition.builder("Wither", 2)
        .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
        .weight(UNCOMMON)
        .supportedItems(ItemSetDefaults.SWORDS_AXES)
        .primaryItems(ItemSetDefaults.SWORD)
        .build(), () -> EnchantDistribution.regular(TradeType.SNOW_COMMON), WitherEnchant::new
    ),
    ;

    public static void loadAll(@NotNull Path dataDir, @NotNull ItemSetRegistry itemSetRegistry, @NotNull BiConsumer<EnchantCatalog, IllegalStateException> onError) {
        Path enchantsDir = Path.of(dataDir.toString(), EnchantsFiles.DIR_ENCHANTS);
        Path disabledDir = Path.of(enchantsDir.toString(), EnchantsFiles.DIR_DISABLED);

        if (!Files.exists(disabledDir)) {
            try {
                Files.createDirectories(disabledDir);
            }
            catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        Set<String> disabledEnchants = FileUtil.findYamlFiles(disabledDir.toString()).stream().map(FileUtil::getNameWithoutExtension).collect(Collectors.toSet());

        for (EnchantCatalog value : values()) {
            if (disabledEnchants.contains(value.getId())) {
                value.disabled = true;
            }
            else {
                try {
                    value.load(enchantsDir, itemSetRegistry);
                }
                catch (IllegalStateException exception) {
                    onError.accept(value, exception);
                }
            }
        }
    }

    @NotNull
    public static Stream<EnchantCatalog> stream() {
        return Stream.of(values());
    }

    @NotNull
    public static Stream<EnchantCatalog> enabled() {
        return stream().filter(EnchantCatalog::isEnabled);
    }

    /*public static boolean isPresentAndEnabled(@NotNull String enchantId) {
        return Enums.parse(enchantId, EnchantCatalog.class).filter(EnchantCatalog::isEnabled).isPresent();
    }*/

    private final String  id;
    private final boolean curse;
    private final boolean paperOnly;

    private final Supplier<EnchantDefinition>   initDefinition;
    private final Supplier<EnchantDistribution> initDistribution;
    private final EnchantFactory<?>             factory;

    private EnchantDefinition   definition;
    private EnchantDistribution distribution;
    private boolean             disabled;

    EnchantCatalog(@NotNull Supplier<EnchantDefinition> initDefinition,
                   @NotNull Supplier<EnchantDistribution> initDistribution,
                   @NotNull EnchantFactory<?> factory) {
        this(initDefinition, initDistribution, factory, false);
    }

    EnchantCatalog(@NotNull Supplier<EnchantDefinition> initDefinition,
                   @NotNull Supplier<EnchantDistribution> initDistribution,
                   @NotNull EnchantFactory<?> factory,
                   boolean curse) {
        this(initDefinition, initDistribution, factory, curse, false);
    }

    EnchantCatalog(@NotNull Supplier<EnchantDefinition> initDefinition,
                   @NotNull Supplier<EnchantDistribution> initDistribution,
                   @NotNull EnchantFactory<?> factory,
                   boolean curse,
                   boolean paperOnly) {
        this.id = LowerCase.INTERNAL.apply(this.name());
        this.curse = curse;
        this.paperOnly = paperOnly;
        this.initDefinition = initDefinition;
        this.initDistribution = initDistribution;
        this.factory = factory;
    }

    public void load(@NotNull Path enchantsDir, @NotNull ItemSetRegistry itemSetRegistry) throws IllegalStateException {
        if (this.paperOnly && Version.isSpigot()) throw new IllegalStateException("The enchantment is available for PaperMC only");

        Path file = Path.of(enchantsDir.toString(), FileConfig.withExtension(this.id));
        boolean exists = Files.exists(file);

        FileConfig config = FileConfig.load(file);
        EnchantDefinition definition = this.initDefinition.get();
        EnchantDistribution distribution = this.initDistribution.get();

        if (!exists) {
            config.set("Definition", definition);
            config.set("Distribution", distribution);
        }

        this.definition = EnchantDefinition.read(config, "Definition", itemSetRegistry);
        this.distribution = EnchantDistribution.read(config, "Distribution");

        config.saveChanges();
    }

    @NotNull
    public CustomEnchantment createEnchantment(@NotNull EnchantsPlugin plugin, @NotNull EnchantManager manager, @NotNull Path file, @NotNull EnchantContext context) {
        return this.factory.create(plugin, manager, file, context);
    }

    @Override
    @NotNull
    public String getId() {
        return this.id;
    }

    @Override
    @NotNull
    public EnchantDefinition getDefinition() {
        if (this.definition == null) throw new IllegalStateException("Definition is not yet initialized");

        return this.definition;
    }

    @Override
    @NotNull
    public EnchantDistribution getDistribution() {
        if (this.distribution == null) throw new IllegalStateException("Distribution is not yet initialized");

        return this.distribution;
    }

    @Override
    public boolean isCurse() {
        return this.curse;
    }

    public boolean isPaperOnly() {
        return this.paperOnly;
    }

    public boolean isDisabled() {
        return this.disabled;
    }

    public boolean isEnabled() {
        return !this.disabled;
    }

    static class Weight {

        static final int COMMON    = 10;
        static final int UNCOMMON  = 5;
        static final int RARE      = 2;
        static final int VERY_RARE = 1;

    }
}
