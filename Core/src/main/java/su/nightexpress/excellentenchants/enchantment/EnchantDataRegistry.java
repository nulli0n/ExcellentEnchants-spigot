package su.nightexpress.excellentenchants.enchantment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.*;
import su.nightexpress.excellentenchants.api.item.ItemSetId;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;
import su.nightexpress.excellentenchants.api.wrapper.TradeType;
import su.nightexpress.excellentenchants.bridge.DistributionConfig;
import su.nightexpress.excellentenchants.enchantment.armor.*;
import su.nightexpress.excellentenchants.enchantment.bow.*;
import su.nightexpress.excellentenchants.enchantment.fishing.*;
import su.nightexpress.excellentenchants.enchantment.tool.*;
import su.nightexpress.excellentenchants.enchantment.universal.*;
import su.nightexpress.excellentenchants.enchantment.weapon.*;
import su.nightexpress.nightcore.bridge.registry.NightRegistry;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;

public class EnchantDataRegistry {

    private static final NightRegistry<String, EnchantData> REGISTRY = new NightRegistry<>();

    private static final int COMMON    = 10;
    private static final int UNCOMMON  = 5;
    private static final int RARE      = 2;
    private static final int VERY_RARE = 1;

    private final Path        enchantsDir;
    private final Set<String> disabledEnchants;
    private final boolean isPaper;

    private EnchantDataRegistry(@NotNull Path dataDir, boolean isPaper) {
        this.enchantsDir = Path.of(dataDir.toString(), EnchantFiles.DIR_ENCHANTS);
        this.disabledEnchants = new HashSet<>();
        this.isPaper = isPaper;
    }

    public static void initialize(@NotNull Path dataDir, boolean isPaper) {
        if (REGISTRY.isFrozen()) throw new IllegalStateException("Registry is already initialized");

        EnchantDataRegistry dataRegistry = new EnchantDataRegistry(dataDir, isPaper);
        dataRegistry.load();
        REGISTRY.freeze();
    }

    public static void clear() {
        REGISTRY.unfreeze();
        REGISTRY.clear();
    }

    @Nullable
    public static EnchantData getDataById(@NotNull String id) {
        return REGISTRY.byKey(id);
    }

    @NotNull
    public static Map<String, EnchantData> getMap() {
        return REGISTRY.map();
    }

    public static boolean isPresent(@NotNull String id) {
        return REGISTRY.lookup(id).isPresent();
    }

    private void load() {
        this.loadDisabledFolder();

        this.loadDefaults().forEach((id, data) -> {
            if (this.disabledEnchants.contains(id)) return;

            Path file = Path.of(this.enchantsDir.toString(), FileConfig.withExtension(id));
            boolean exists = Files.exists(file);

            FileConfig config = FileConfig.load(file);
            EnchantDefinition definition = data.getDefinition();
            EnchantDistribution distribution = data.getDistribution();

            if (!exists) {
                config.set("Definition", definition);
                config.set("Distribution", distribution);
            }

            definition = EnchantDefinition.read(config, "Definition");
            distribution = EnchantDistribution.read(config, "Distribution");

            REGISTRY.register(id, new EnchantData(definition, distribution, data.getProvider(), data.isCurse()));

            config.saveChanges();
        });
    }

    private void loadDisabledFolder() {
        this.disabledEnchants.addAll(DistributionConfig.DISABLED_GLOBAL.get());

        Path path = Path.of(this.enchantsDir.toString(), EnchantFiles.DIR_DISABLED);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            }
            catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
        }

        FileUtil.findYamlFiles(path.toString()).forEach(file -> {
            String name = file.getFileName().toString();
            this.disabledEnchants.add(name.substring(0, name.length() - FileConfig.EXTENSION.length()));
        });
    }

    @NotNull
    private Map<String, EnchantData> loadDefaults() {
        Map<String, EnchantData> map = new LinkedHashMap<>();
        
        this.loadDefaultArmors(map);
        this.loadDefaultBow(map);
        this.loadDefaultFishing(map);
        this.loadDefaultTool(map);
        this.loadDefaultUniversal(map);
        this.loadDefaultWeapon(map);
        
        return map;
    }
    
    private void addData(@NotNull Map<String, EnchantData> map,
                         @NotNull String id,
                         @NotNull EnchantDefinition definition,
                         @NotNull EnchantDistribution distribution,
                         @NotNull EnchantProvider<?> provider) {
        this.addData(map, id, definition, distribution, provider, false);
    }

    private void addData(@NotNull Map<String, EnchantData> map,
                         @NotNull String id,
                         @NotNull EnchantDefinition definition,
                         @NotNull EnchantDistribution distribution,
                         @NotNull EnchantProvider<?> provider,
                         boolean curse) {
        map.put(id, new EnchantData(definition, distribution, provider, curse));
    }

    private void loadDefaultArmors(@NotNull Map<String, EnchantData> map) {
        this.addData(map, EnchantId.COLD_STEEL, EnchantDefinition.builder("Cold Steel", 3)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), ColdSteelEnchant::new);

        this.addData(map, EnchantId.DARKNESS_CLOAK, EnchantDefinition.builder("Darkness Cloak", 3)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), DarknessCloakEnchant::new);

        this.addData(map, EnchantId.ELEMENTAL_PROTECTION, EnchantDefinition.builder("Elemental Protection", 4)
            .description("Reduces potion and elemental damage by " + GENERIC_AMOUNT + "%.")
            .weight(COMMON)
            .items(ItemSetId.ARMOR)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON), ElementalProtectionEnchant::new);

        this.addData(map, EnchantId.FIRE_SHIELD, EnchantDefinition.builder("Fire Shield", 4)
            .description(TRIGGER_CHANCE + "% chance to ignite attackers for " + GENERIC_DURATION + "s.")
            .weight(RARE)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), FireShieldEnchant::new);

        this.addData(map, EnchantId.FLAME_WALKER, EnchantDefinition.builder("Flame Walker", 2)
            .description("Ability to walk on lava, immunity to magma damage.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOOTS)
            .exclusives(EnchantKeys.FROST_WALKER)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), FlameWalkerEnchant::new);

        this.addData(map, EnchantId.HARDENED, EnchantDefinition.builder("Hardened", 2)
            .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) when damaged.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), HardenedEnchant::new);

        this.addData(map, EnchantId.ICE_SHIELD, EnchantDefinition.builder("Ice Shield", 3)
            .description(TRIGGER_CHANCE + "% chance to freeze and apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on attacker.")
            .weight(COMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), IceShieldEnchant::new);

        this.addData(map, EnchantId.JUMPING, EnchantDefinition.builder("Jumping", 2)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), JumpingEnchant::new);

        this.addData(map, EnchantId.KAMIKADZE, EnchantDefinition.builder("Kamikadze", 3)
            .description(TRIGGER_CHANCE + "% chance to explode on death.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON), KamikadzeEnchant::new);

        this.addData(map, EnchantId.NIGHT_VISION, EnchantDefinition.builder("Night Vision", 1)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(VERY_RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_SPECIAL), NightVisionEnchant::new);

        this.addData(map, EnchantId.REBOUND, EnchantDefinition.builder("Rebound", 1)
            .description("Effect of landing on a slime block.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .exclusives(EnchantKeys.FEATHER_FALLING)
            .build(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON), ReboundEnchant::new);

        this.addData(map, EnchantId.REGROWTH, EnchantDefinition.builder("Regrowth", 4)
            .description("Restores " + GENERIC_AMOUNT + "❤ every few seconds.")
            .weight(RARE)
            .supportedItems(ItemSetId.CHESTPLATE_ELYTRA)
            .primaryItems(ItemSetId.CHESTPLATE)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), RegrowthEnchant::new);

        this.addData(map, EnchantId.SATURATION, EnchantDefinition.builder("Saturation", 2)
            .description("Restores " + GENERIC_AMOUNT + " food points every few seconds.")
            .weight(RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL), SaturationEnchant::new);

        this.addData(map, EnchantId.SPEED, EnchantDefinition.builder("Speed", 2)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .items(ItemSetId.BOOTS)
            .build(), EnchantDistribution.regular(TradeType.DESERT_SPECIAL), SpeedyEnchant::new);

        this.addData(map, EnchantId.STOPPING_FORCE, EnchantDefinition.builder("Stopping Force", 3)
            .description(TRIGGER_CHANCE + "% chance to reduce knockback for " + GENERIC_AMOUNT + "%.")
            .weight(UNCOMMON)
            .items(ItemSetId.LEGGINGS)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), StoppingForceEnchant::new);

        this.addData(map, EnchantId.WATER_BREATHING, EnchantDefinition.builder("Water Breathing", 1)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(VERY_RARE)
            .items(ItemSetId.HELMET)
            .build(), EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL), WaterBreathingEnchant::new);
    }

    private void loadDefaultBow(@NotNull Map<String, EnchantData> map) {
        this.addData(map, EnchantId.BOMBER, EnchantDefinition.builder("Bomber", 3)
            .description(TRIGGER_CHANCE + "% chance to shoot a TNT ignited for " + GENERIC_TIME + "s.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(
                EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST),
                EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER
            )
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), BomberEnchant::new);

        this.addData(map, EnchantId.CONFUSING_ARROWS, EnchantDefinition.builder("Confusing Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON), ConfusingArrowsEnchant::new);

        this.addData(map, EnchantId.DARKNESS_ARROWS, EnchantDefinition.builder("Darkness Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), DarknessArrowsEnchant::new);

        this.addData(map, EnchantId.DRAGONFIRE_ARROWS, EnchantDefinition.builder("Dragonfire Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have Dragonfire Effect (R=" + GENERIC_RADIUS + ", " + GENERIC_DURATION + "s).")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_SPECIAL), DragonfireArrowsEnchant::new);

        this.addData(map, EnchantId.ELECTRIFIED_ARROWS, EnchantDefinition.builder("Electrified Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for an arrow to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), ElectrifiedArrowsEnchant::new);

        this.addData(map, EnchantId.ENDER_BOW, EnchantDefinition.builder("Ender Bow", 1)
            .description("Shoots ender pearls instead of arrows.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW)
            .exclusives(EnchantKeys.create(EnchantId.BOMBER), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER)
            .build(), EnchantDistribution.treasure(TradeType.PLAINS_SPECIAL), EnderBowEnchant::new);

        this.addData(map, EnchantId.EXPLOSIVE_ARROWS, EnchantDefinition.builder("Explosive Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance to shoot an explosive arrow.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON), ExplosiveArrowsEnchant::new);

        this.addData(map, EnchantId.FLARE, EnchantDefinition.builder("Flare", 1)
            .description(TRIGGER_CHANCE + "% chance to create a torch where arrow lands.")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.treasure(TradeType.SNOW_COMMON), FlareEnchant::new);

        this.addData(map, EnchantId.GHAST, EnchantDefinition.builder("Ghast", 1)
            .description("Shoots fireballs instead of arrows.")
            .weight(VERY_RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.FLAME, EnchantKeys.PUNCH, EnchantKeys.POWER)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_COMMON), GhastEnchant::new);

        this.addData(map, EnchantId.HOVER, EnchantDefinition.builder("Hover", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), HoverEnchant::new);

        this.addData(map, EnchantId.LINGERING, EnchantDefinition.builder("Lingering", 3)
            .description(TRIGGER_CHANCE + "% chance for tipped arrows to generate a lingering effect.")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), LingeringEnchant::new);

        this.addData(map, EnchantId.POISONED_ARROWS, EnchantDefinition.builder("Poisoned Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON), PoisonedArrowsEnchant::new);

        this.addData(map, EnchantId.SNIPER, EnchantDefinition.builder("Sniper", 2)
            .description("Increases projectile speed by " + GENERIC_AMOUNT + "%")
            .weight(COMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), SniperEnchant::new);

        this.addData(map, EnchantId.VAMPIRIC_ARROWS, EnchantDefinition.builder("Vampiric Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance to restore " + GENERIC_AMOUNT + "❤ on arrow hit.")
            .weight(RARE)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SWAMP_SPECIAL), VampiricArrowsEnchant::new);

        this.addData(map, EnchantId.WITHERED_ARROWS, EnchantDefinition.builder("Withered Arrows", 3)
            .description(TRIGGER_CHANCE + "% chance for arrow to have " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.)")
            .weight(UNCOMMON)
            .items(ItemSetId.BOW_CROSSBOW)
            .exclusives(EnchantKeys.create(EnchantId.ENDER_BOW), EnchantKeys.create(EnchantId.GHAST), EnchantKeys.create(EnchantId.BOMBER))
            .build(), EnchantDistribution.regular(TradeType.SNOW_SPECIAL), WitheredArrowsEnchant::new);
    }

    private void loadDefaultFishing(@NotNull Map<String, EnchantData> map) {
        if (this.isPaper) {
            this.addData(map, EnchantId.AUTO_REEL, EnchantDefinition.builder("Auto Reel", 1)
                .description("Automatically reels in a hook on bite.")
                .weight(VERY_RARE)
                .items(ItemSetId.FISHING_ROD)
                .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), AutoReelEnchant::new);
        }

        this.addData(map, EnchantId.CURSE_OF_DROWNED, EnchantDefinition.builder("Curse of Drowned", 3)
            .description(TRIGGER_CHANCE + "% chance to fish up a Drowned Zombie.")
            .weight(UNCOMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.treasure(TradeType.SWAMP_COMMON), CurseOfDrownedEnchant::new, true);

        this.addData(map, EnchantId.DOUBLE_CATCH, EnchantDefinition.builder("Double Catch", 3)
            .description("Increases amount of caught item by x2 with " + TRIGGER_CHANCE + "% chance.")
            .weight(RARE)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), DoubleCatchEnchant::new);

        this.addData(map, EnchantId.RIVER_MASTER, EnchantDefinition.builder("River Master", 5)
            .description("Increases casting distance.")
            .weight(COMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), RiverMasterEnchant::new);

        this.addData(map, EnchantId.SEASONED_ANGLER, EnchantDefinition.builder("Seasoned Angler", 3)
            .description("Increases amount of XP gained from fishing by " + GENERIC_AMOUNT + "%.")
            .weight(UNCOMMON)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), SeasonedAnglerEnchant::new);

        this.addData(map, EnchantId.SURVIVALIST, EnchantDefinition.builder("Survivalist", 1)
            .description("Automatically cooks fish if what is caught is raw.")
            .weight(RARE)
            .items(ItemSetId.FISHING_ROD)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_SPECIAL), SurvivalistEnchant::new);
    }

    private void loadDefaultTool(@NotNull Map<String, EnchantData> map) {
        this.addData(map, EnchantId.BLAST_MINING, EnchantDefinition.builder("Blast Mining", 5)
            .description(TRIGGER_CHANCE + "% chance to mine blocks by explosion.")
            .weight(RARE)
            .items(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.create(EnchantId.VEINMINER), EnchantKeys.create(EnchantId.TUNNEL))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), BlastMiningEnchant::new);

        this.addData(map, EnchantId.GLASSBREAKER, EnchantDefinition.builder("Glass Breaker", 1)
            .description("Breaks glass instantly")
            .weight(COMMON)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), GlassbreakerEnchant::new);

        this.addData(map, EnchantId.HASTE, EnchantDefinition.builder("Haste", 3)
            .description("Grants permanent " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " effect.")
            .weight(RARE)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), HasteEnchant::new);

        this.addData(map, EnchantId.LUCKY_MINER, EnchantDefinition.builder("Lucky Miner", 3)
            .description(TRIGGER_CHANCE + "% chance to gain " + GENERIC_AMOUNT + "% more XP from ores.")
            .weight(UNCOMMON)
            .items(ItemSetId.PICKAXE)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON), LuckyMinerEnchant::new);

        this.addData(map, EnchantId.REPLANTER, EnchantDefinition.builder("Replanter", 1)
            .description("Automatically replant crops on right click and when harvest.")
            .weight(VERY_RARE)
            .items(ItemSetId.HOE)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), ReplanterEnchant::new);

        if (this.isPaper) {
            this.addData(map, EnchantId.SILK_CHEST, EnchantDefinition.builder("Silk Chest", 1)
                .description("Drop chests and saves all its content.")
                .weight(VERY_RARE)
                .supportedItems(ItemSetId.MINING_TOOLS)
                .primaryItems(ItemSetId.AXE)
                .exclusives(EnchantKeys.create(EnchantId.SMELTER))
                .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), SilkChestEnchant::new);
        }

        this.addData(map, EnchantId.SILK_SPAWNER, EnchantDefinition.builder("Silk Spawner", 1)
            .description(TRIGGER_CHANCE + "% chance to mine spawner.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.MINING_TOOLS)
            .primaryItems(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.create(EnchantId.SMELTER))
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), SilkSpawnerEnchant::new);

        this.addData(map, EnchantId.SMELTER, EnchantDefinition.builder("Smelter", 5)
            .description("Smelts mined blocks with " + TRIGGER_CHANCE + "% chance.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .exclusives(EnchantKeys.create(EnchantId.SILK_SPAWNER), EnchantKeys.create(EnchantId.SILK_CHEST), EnchantKeys.SILK_TOUCH)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), SmelterEnchant::new);

        this.addData(map, EnchantId.TELEKINESIS, EnchantDefinition.builder("Telekinesis", 1)
            .description("Moves all blocks loot directly to your inventory.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.TOOL)
            .primaryItems(ItemSetId.MINING_TOOLS)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), TelekinesisEnchant::new);

        this.addData(map, EnchantId.TREEFELLER, EnchantDefinition.builder("Treefeller", 1)
            .description("Cuts down an entire tree.")
            .weight(RARE)
            .items(ItemSetId.AXE)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_SPECIAL), TreefellerEnchant::new);

        this.addData(map, EnchantId.TUNNEL, EnchantDefinition.builder("Tunnel", 3)
            .description("Mines multiple blocks at once in a certain shape.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.MINING_TOOLS)
            .primaryItems(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.create(EnchantId.VEINMINER), EnchantKeys.create(EnchantId.BLAST_MINING))
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_SPECIAL), TunnelEnchant::new);

        this.addData(map, EnchantId.VEINMINER, EnchantDefinition.builder("Veinminer", 3)
            .description("Mines up to " + GENERIC_AMOUNT + " blocks of the ore vein at once.")
            .weight(RARE)
            .items(ItemSetId.PICKAXE)
            .exclusives(EnchantKeys.create(EnchantId.BLAST_MINING), EnchantKeys.create(EnchantId.TUNNEL))
            .build(), EnchantDistribution.regular(TradeType.PLAINS_SPECIAL), VeinminerEnchant::new);
    }

    private void loadDefaultUniversal(@NotNull Map<String, EnchantData> map) {
        this.addData(map, EnchantId.CURSE_OF_BREAKING, EnchantDefinition.builder("Curse of Breaking", 3)
            .description(TRIGGER_CHANCE + "% chance to consume extra " + GENERIC_AMOUNT + " durability points.")
            .weight(COMMON)
            .items(ItemSetId.BREAKABLE)
            .exclusives(EnchantKeys.UNBREAKING)
            .build(), EnchantDistribution.treasure(TradeType.SAVANNA_COMMON), CurseOfBreakingEnchant::new, true);

        this.addData(map, EnchantId.CURSE_OF_FRAGILITY, EnchantDefinition.builder("Curse of Fragility", 1)
            .description("Prevents an item from being grindstoned or anviled.")
            .weight(COMMON)
            .items(ItemSetId.BREAKABLE)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_COMMON), CurseOfFragilityEnchant::new, true);

        this.addData(map, EnchantId.CURSE_OF_MEDIOCRITY, EnchantDefinition.builder("Curse of Mediocrity", 3)
            .description(TRIGGER_CHANCE + "% chance to disenchant item drops.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.TOOLS_WEAPONS)
            .primaryItems(ItemSetId.ALL_WEAPON)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_COMMON), CurseOfMediocrityEnchant::new, true);

        this.addData(map, EnchantId.CURSE_OF_MISFORTUNE, EnchantDefinition.builder("Curse of Misfortune", 3)
            .description(TRIGGER_CHANCE + "% chance to have no drops from blocks or mobs.")
            .weight(UNCOMMON)
            .items(ItemSetId.TOOLS_WEAPONS)
            .exclusives(EnchantKeys.FORTUNE, EnchantKeys.LOOTING)
            .build(), EnchantDistribution.treasure(TradeType.TAIGA_COMMON), CurseOfMisfortuneEnchant::new, true);

        this.addData(map, EnchantId.RESTORE, EnchantDefinition.builder("Restore", 3)
            .description(TRIGGER_CHANCE + "% chance to save item from breaking back to " + GENERIC_AMOUNT + "%")
            .weight(RARE)
            .items(ItemSetId.BREAKABLE)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), RestoreEnchant::new);

        this.addData(map, EnchantId.SOULBOUND, EnchantDefinition.builder("Soulbound", 1)
            .description("Protects from being dropped on death.")
            .weight(RARE)
            .items(ItemSetId.BREAKABLE)
            .exclusives(EnchantKeys.VANISHING_CURSE)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), SoulboundEnchant::new);
    }

    private void loadDefaultWeapon(@NotNull Map<String, EnchantData> map) {
        this.addData(map, EnchantId.BANE_OF_NETHERSPAWN, EnchantDefinition.builder("Bane of Netherspawn", 5)
            .description("Inflicts " + GENERIC_DAMAGE + "❤ more damage to nether mobs.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), BaneOfNetherspawnEnchant::new);

        this.addData(map, EnchantId.BLINDNESS, EnchantDefinition.builder("Blindness", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON), BlindnessEnchant::new);

        this.addData(map, EnchantId.CONFUSION, EnchantDefinition.builder("Confusion", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), ConfusionEnchant::new);

        this.addData(map, EnchantId.CURE, EnchantDefinition.builder("Cure", 3)
            .description(TRIGGER_CHANCE + "% chance to cure Zombified Piglins and Zombie Villagers on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), CureEnchant::new);

        this.addData(map, EnchantId.CURSE_OF_DEATH, EnchantDefinition.builder("Curse of Death", 3)
            .description("When killing players, you have a chance of dying too.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.DESERT_SPECIAL), CurseOfDeathEnchant::new, true);

        this.addData(map, EnchantId.CUTTER, EnchantDefinition.builder("Cutter", 3)
            .description(TRIGGER_CHANCE + "% chance to throw away enemy''s armor and damage it for " + GENERIC_DAMAGE + "%.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), CutterEnchant::new);

        this.addData(map, EnchantId.DECAPITATOR, EnchantDefinition.builder("Decapitator", 2)
            .description(TRIGGER_CHANCE + "% chance to obtain player's or mob's head.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.SNOW_SPECIAL), DecapitatorEnchant::new);

        this.addData(map, EnchantId.DOUBLE_STRIKE, EnchantDefinition.builder("Double Strike", 2)
            .description(TRIGGER_CHANCE + "% chance to inflict double damage.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON), DoubleStrikeEnchant::new);

        this.addData(map, EnchantId.EXHAUST, EnchantDefinition.builder("Exhaust", 4)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), ExhaustEnchant::new);

        this.addData(map, EnchantId.ICE_ASPECT, EnchantDefinition.builder("Ice Aspect", 3)
            .description("Freezes and applies " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), IceAspectEnchant::new);

        this.addData(map, EnchantId.INFERNUS, EnchantDefinition.builder("Infernus", 3)
            .description("Launched trident will ignite the enemy for " + GENERIC_TIME + "s. on hit.")
            .weight(COMMON)
            .items(ItemSetId.TRIDENT)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), InfernusEnchant::new);

        this.addData(map, EnchantId.NIMBLE, EnchantDefinition.builder("Nimble", 1)
            .description("Moves all entity's loot directly to your inventory.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON), NimbleEnchant::new);

        this.addData(map, EnchantId.PARALYZE, EnchantDefinition.builder("Paralyze", 5)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON), ParalyzeEnchant::new);

        this.addData(map, EnchantId.RAGE, EnchantDefinition.builder("Rage", 2)
            .description(TRIGGER_CHANCE + "% chance to get " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), RageEnchant::new);

        this.addData(map, EnchantId.ROCKET, EnchantDefinition.builder("Rocket", 3)
            .description(TRIGGER_CHANCE + "% chance to launch your enemy into the space.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_COMMON), RocketEnchant::new);

//        this.addData(map, EnchantId.SURPRISE, EnchantDefinition.builder("Surprise", 3)
//            .description(TRIGGER_CHANCE + "% chance to apply random potion effect to enemy on hit.")
//            .weight(UNCOMMON)
//            .supportedItems(ItemSetId.SWORDS_AXES)
//            .primaryItems(ItemSetId.SWORD)
//            .build(), EnchantDistribution.regular(TradeType.JUNGLE_COMMON));

        this.addData(map, EnchantId.SWIPER, EnchantDefinition.builder("Swiper", 3)
            .description(TRIGGER_CHANCE + "% chance to steal " + GENERIC_AMOUNT + " XP from players.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON), SwiperEnchant::new);

        this.addData(map, EnchantId.TEMPER, EnchantDefinition.builder("Temper", 5)
            .description("Inflicts " + GENERIC_AMOUNT + "% more damage for each " + GENERIC_RADIUS + "❤ missing.")
            .weight(VERY_RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.TAIGA_COMMON), TemperEnchant::new);

        this.addData(map, EnchantId.THRIFTY, EnchantDefinition.builder("Thrifty", 3)
            .description(TRIGGER_CHANCE + "% chance for mobs to drop spawn egg.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL), ThriftyEnchant::new);

        this.addData(map, EnchantId.THUNDER, EnchantDefinition.builder("Thunder", 5)
            .description(TRIGGER_CHANCE + "% chance to strike lightning with " + GENERIC_DAMAGE + "❤ extra damage.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), ThunderEnchant::new);

        this.addData(map, EnchantId.VAMPIRE, EnchantDefinition.builder("Vampire", 3)
            .description(TRIGGER_CHANCE + "% chance to heal for " + GENERIC_AMOUNT + "❤ on hit.")
            .weight(RARE)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SAVANNA_COMMON), VampireEnchant::new);

        this.addData(map, EnchantId.VENOM, EnchantDefinition.builder("Venom", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SWAMP_COMMON), VenomEnchant::new);

        this.addData(map, EnchantId.VILLAGE_DEFENDER, EnchantDefinition.builder("Village Defender", 5)
            .description("Inflicts " + GENERIC_AMOUNT + "❤ more damage to all pillagers.")
            .weight(COMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.PLAINS_COMMON), VillageDefenderEnchant::new);

        this.addData(map, EnchantId.WISDOM, EnchantDefinition.builder("Wisdom", 5)
            .description("Mobs drops x" + GENERIC_MODIFIER + " more XP.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.DESERT_COMMON), WisdomEnchant::new);

        this.addData(map, EnchantId.WITHER, EnchantDefinition.builder("Wither", 2)
            .description(TRIGGER_CHANCE + "% chance to apply " + EFFECT_TYPE + " " + EFFECT_AMPLIFIER + " (" + EFFECT_DURATION + "s.) on hit.")
            .weight(UNCOMMON)
            .supportedItems(ItemSetId.SWORDS_AXES)
            .primaryItems(ItemSetId.SWORD)
            .build(), EnchantDistribution.regular(TradeType.SNOW_COMMON), WitherEnchant::new);
    }
}
