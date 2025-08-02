package su.nightexpress.excellentenchants.manager;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantId;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.armor.*;
import su.nightexpress.excellentenchants.enchantment.armor.KamikadzeEnchant;
import su.nightexpress.excellentenchants.enchantment.bow.*;
import su.nightexpress.excellentenchants.enchantment.fishing.*;
import su.nightexpress.excellentenchants.enchantment.tool.*;
import su.nightexpress.excellentenchants.enchantment.universal.*;
import su.nightexpress.excellentenchants.enchantment.weapon.*;
import su.nightexpress.excellentenchants.enchantment.weapon.InfernusEnchant;
import su.nightexpress.nightcore.util.Version;

public class EnchantProviders {

    public static void load(@NotNull EnchantsPlugin plugin) {
        if (Version.isPaper()) {
            if (Version.isAtLeast(Version.MC_1_21_5)) {
                EnchantRegistry.addProvider(EnchantId.AUTO_REEL, (file, data) -> new AutoReelEnchant(plugin, file, data));
            }
            EnchantRegistry.addProvider(EnchantId.SILK_CHEST, (file, data) -> new SilkChestEnchant(plugin, file, data));
        }

        EnchantRegistry.addProvider(EnchantId.COLD_STEEL, (file, data) -> new ColdSteelEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DARKNESS_CLOAK, (file, data) -> new DarknessCloakEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ELEMENTAL_PROTECTION, (file, data) -> new ElementalProtectionEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.FIRE_SHIELD, (file, data) -> new FireShieldEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.FLAME_WALKER, (file, data) -> new FlameWalkerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.HARDENED, (file, data) -> new HardenedEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ICE_SHIELD, (file, data) -> new IceShieldEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.JUMPING, (file, data) -> new JumpingEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.KAMIKADZE, (file, data) -> new KamikadzeEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.NIGHT_VISION, (file, data) -> new NightVisionEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.REBOUND, (file, data) -> new ReboundEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.REGROWTH, (file, data) -> new RegrowthEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SATURATION, (file, data) -> new SaturationEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SPEED, (file, data) -> new SpeedyEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.STOPPING_FORCE, (file, data) -> new StoppingForceEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.WATER_BREATHING, (file, data) -> new WaterBreathingEnchant(plugin, file, data));

        EnchantRegistry.addProvider(EnchantId.BOMBER, (file, data) -> new BomberEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CONFUSING_ARROWS, (file, data) -> new ConfusingArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DARKNESS_ARROWS, (file, data) -> new DarknessArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DRAGONFIRE_ARROWS, (file, data) -> new DragonfireArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ELECTRIFIED_ARROWS, (file, data) -> new ElectrifiedArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ENDER_BOW, (file, data) -> new EnderBowEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.EXPLOSIVE_ARROWS, (file, data) -> new ExplosiveArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.FLARE, (file, data) -> new FlareEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.GHAST, (file, data) -> new GhastEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.HOVER, (file, data) -> new HoverEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.LINGERING, (file, data) -> new LingeringEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.POISONED_ARROWS, (file, data) -> new PoisonedArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SNIPER, (file, data) -> new SniperEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.VAMPIRIC_ARROWS, (file, data) -> new VampiricArrowsEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.WITHERED_ARROWS, (file, data) -> new WitheredArrowsEnchant(plugin, file, data));

        EnchantRegistry.addProvider(EnchantId.CURSE_OF_DROWNED, (file, data) -> new CurseOfDrownedEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DOUBLE_CATCH, (file, data) -> new DoubleCatchEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.RIVER_MASTER, (file, data) -> new RiverMasterEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SEASONED_ANGLER, (file, data) -> new SeasonedAnglerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SURVIVALIST, (file, data) -> new SurvivalistEnchant(plugin, file, data));

        EnchantRegistry.addProvider(EnchantId.BLAST_MINING, (file, data) -> new BlastMiningEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CURSE_OF_BREAKING, (file, data) -> new CurseOfBreakingEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CURSE_OF_MEDIOCRITY, (file, data) -> new CurseOfMediocrityEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CURSE_OF_MISFORTUNE, (file, data) -> new CurseOfMisfortuneEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.GLASSBREAKER, (file, data) -> new GlassbreakerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.HASTE, (file, data) -> new HasteEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.LUCKY_MINER, (file, data) -> new LuckyMinerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.REPLANTER, (file, data) -> new ReplanterEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SILK_SPAWNER, (file, data) -> new SilkSpawnerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SMELTER, (file, data) -> new SmelterEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.TELEKINESIS, (file, data) -> new TelekinesisEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.TUNNEL, (file, data) -> new TunnelEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.TREEFELLER, (file, data) -> new TreefellerEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.VEINMINER, (file, data) -> new VeinminerEnchant(plugin, file, data));

        EnchantRegistry.addProvider(EnchantId.CURSE_OF_FRAGILITY, (file, data) -> new CurseOfFragilityEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.RESTORE, (file, data) -> new RestoreEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SOULBOUND, (file, data) -> new SoulboundEnchant(plugin, file, data));

        EnchantRegistry.addProvider(EnchantId.BANE_OF_NETHERSPAWN, (file, data) -> new BaneOfNetherspawnEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.BLINDNESS, (file, data) -> new BlindnessEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CONFUSION, (file, data) -> new ConfusionEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CURE, (file, data) -> new CureEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CURSE_OF_DEATH, (file, data) -> new CurseOfDeathEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.CUTTER, (file, data) -> new CutterEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DECAPITATOR, (file, data) -> new DecapitatorEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.DOUBLE_STRIKE, (file, data) -> new DoubleStrikeEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.EXHAUST, (file, data) -> new ExhaustEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ICE_ASPECT, (file, data) -> new IceAspectEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.INFERNUS, (file, data) -> new InfernusEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.NIMBLE, (file, data) -> new NimbleEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.PARALYZE, (file, data) -> new ParalyzeEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.RAGE, (file, data) -> new RageEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.ROCKET, (file, data) -> new RocketEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.SWIPER, (file, data) -> new SwiperEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.TEMPER, (file, data) -> new TemperEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.THRIFTY, (file, data) -> new ThriftyEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.THUNDER, (file, data) -> new ThunderEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.VAMPIRE, (file, data) -> new VampireEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.VENOM, (file, data) -> new VenomEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.VILLAGE_DEFENDER, (file, data) -> new VillageDefenderEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.WISDOM, (file, data) -> new WisdomEnchant(plugin, file, data));
        EnchantRegistry.addProvider(EnchantId.WITHER, (file, data) -> new WitherEnchant(plugin, file, data));
    }
}
