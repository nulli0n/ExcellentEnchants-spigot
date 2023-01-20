package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.armor.*;
import su.nightexpress.excellentenchants.enchantment.impl.bow.*;
import su.nightexpress.excellentenchants.enchantment.impl.tool.*;
import su.nightexpress.excellentenchants.enchantment.impl.universal.EnchantCurseOfFragility;
import su.nightexpress.excellentenchants.enchantment.impl.weapon.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EnchantRegister {

    private static final ExcellentEnchants                   PLUGIN;
    public static final Map<NamespacedKey, ExcellentEnchant> ENCHANT_REGISTRY;

    public static final EnchantBlastMining       BLAST_MINING;
    public static final EnchantCurseOfBreaking   CURSE_OF_BREAKING;
    public static final EnchantCurseOfMisfortune CURSE_OF_MISFORTUNE;
    public static final EnchantDivineTouch       DIVINE_TOUCH;
    public static final EnchantHaste             HASTE;
    public static final EnchantLuckyMiner        LUCKY_MINER;
    public static final EnchantReplanter         REPLANTER;
    public static final EnchantSilkChest         SILK_CHEST;
    public static final EnchantSmelter           SMELTER;
    public static final EnchantTelekinesis       TELEKINESIS;
    public static final EnchantTreasures         TREASURES;
    public static final EnchantTunnel            TUNNEL;
    public static final EnchantVeinminer         VEINMINER;

    public static final EnchantBaneOfNetherspawn BANE_OF_NETHERSPAWN;
    public static final EnchantIceAspect         ICE_ASPECT;
    public static final EnchantInfernus          INFERNUS;
    public static final EnchantVenom             VENOM;
    public static final EnchantExhaust           EXHAUST;
    public static final EnchantWither            WITHER;
    public static final EnchantParalyze          PARALYZE;
    public static final EnchantExpHunter         EXP_HUNTER;
    public static final EnchantDecapitator       DECAPITATOR;
    public static final EnchantCutter            CUTTER;
    public static final EnchantConfusion         CONFUSION;
    public static final EnchantDoubleStrike      DOUBLE_STRIKE;
    public static final EnchantNimble            NIMBLE;
    public static final EnchantBlindness         BLINDNESS;
    public static final EnchantVampire           VAMPIRE;
    public static final EnchantCure              CURE;
    public static final EnchantRage              RAGE;
    public static final EnchantScavenger         SCAVENGER;
    public static final EnchantSurprise          SURPRISE;
    public static final EnchantTemper            TEMPER;
    public static final EnchantThrifty           THRIFTY;
    public static final EnchantThunder           THUNDER;
    public static final EnchantVillageDefender   VILLAGE_DEFENDER;
    public static final EnchantRocket            ROCKET;

    public static final EnchantElementalProtection ELEMENTAL_PROTECTION;
    public static final EnchantFireShield          FIRE_SHIELD;
    public static final EnchantFlameWalker         FLAME_WALKER;
    public static final EnchantHardened            HARDENED;
    public static final EnchantIceShield           ICE_SHIELD;
    public static final EnchantColdSteel           COLD_STEEL;
    public static final EnchantSelfDestruction     SELF_DESTRUCTION;
    public static final EnchantSaturation          SATURATION;
    public static final EnchantAquaman             AQUAMAN;
    public static final EnchantNightVision         NIGHT_VISION;
    public static final EnchantBunnyHop            BUNNY_HOP;
    public static final EnchantSonic               SONIC;
    public static final EnchantRegrowth            REGROWTH;

    public static final EnchantBomber            BOMBER;
    public static final EnchantConfusingArrows   CONFUSING_ARROWS;
    public static final EnchantDragonfireArrows  DRAGONFIRE_ARROWS;
    public static final EnchantElectrifiedArrows ELECTRIFIED_ARROWS;
    public static final EnchantEnderBow          ENDER_BOW;
    public static final EnchantGhast             GHAST;
    public static final EnchantHover             HOVER;
    public static final EnchantPoisonedArrows    POISONED_ARROWS;
    public static final EnchantWitheredArrows    WITHERED_ARROWS;
    public static final EnchantExplosiveArrows   EXPLOSIVE_ARROWS;

    public static final EnchantCurseOfFragility CURSE_OF_FRAGILITY;

    static {
        PLUGIN = ExcellentEnchants.getPlugin(ExcellentEnchants.class);
        PLUGIN.getConfigManager().extractResources("/enchants/");
        ENCHANT_REGISTRY = new HashMap<>();

        // Tool enchants
        BLAST_MINING = init(EnchantBlastMining.class, EnchantBlastMining.ID);
        CURSE_OF_BREAKING = init(EnchantCurseOfBreaking.class, EnchantCurseOfBreaking.ID);
        CURSE_OF_MISFORTUNE = init(EnchantCurseOfMisfortune.class, EnchantCurseOfMisfortune.ID);
        DIVINE_TOUCH = init(EnchantDivineTouch.class, EnchantDivineTouch.ID);
        HASTE = init(EnchantHaste.class, EnchantHaste.ID);
        LUCKY_MINER = init(EnchantLuckyMiner.class, EnchantLuckyMiner.ID);
        REPLANTER = init(EnchantReplanter.class, EnchantReplanter.ID);
        SILK_CHEST = init(EnchantSilkChest.class, EnchantSilkChest.ID);
        SMELTER = init(EnchantSmelter.class, EnchantSmelter.ID);
        TELEKINESIS = init(EnchantTelekinesis.class, EnchantTelekinesis.ID);
        TREASURES = init(EnchantTreasures.class, EnchantTreasures.ID);
        TUNNEL = init(EnchantTunnel.class, EnchantTunnel.ID);
        VEINMINER = init(EnchantVeinminer.class, EnchantVeinminer.ID);

        // Weapon enchants
        BANE_OF_NETHERSPAWN = init(EnchantBaneOfNetherspawn.class, EnchantBaneOfNetherspawn.ID);
        BLINDNESS = init(EnchantBlindness.class, EnchantBlindness.ID);
        CONFUSION = init(EnchantConfusion.class, EnchantConfusion.ID);
        CUTTER = init(EnchantCutter.class, EnchantCutter.ID);
        DECAPITATOR = init(EnchantDecapitator.class, EnchantDecapitator.ID);
        DOUBLE_STRIKE = init(EnchantDoubleStrike.class, EnchantDoubleStrike.ID);
        EXHAUST = init(EnchantExhaust.class, EnchantExhaust.ID);
        EXP_HUNTER = init(EnchantExpHunter.class, EnchantExpHunter.ID);
        ICE_ASPECT = init(EnchantIceAspect.class, EnchantIceAspect.ID);
        INFERNUS = init(EnchantInfernus.class, EnchantInfernus.ID);
        NIMBLE = init(EnchantNimble.class, EnchantNimble.ID);
        PARALYZE = init(EnchantParalyze.class, EnchantParalyze.ID);
        CURE = init(EnchantCure.class, EnchantCure.ID);
        RAGE = init(EnchantRage.class, EnchantRage.ID);
        ROCKET = init(EnchantRocket.class, EnchantRocket.ID);
        SCAVENGER = init(EnchantScavenger.class, EnchantScavenger.ID);
        SURPRISE = init(EnchantSurprise.class, EnchantSurprise.ID);
        TEMPER = init(EnchantTemper.class, EnchantTemper.ID);
        THRIFTY = init(EnchantThrifty.class, EnchantThrifty.ID);
        THUNDER = init(EnchantThunder.class, EnchantThunder.ID);
        VAMPIRE = init(EnchantVampire.class, EnchantVampire.ID);
        VENOM = init(EnchantVenom.class, EnchantVenom.ID);
        VILLAGE_DEFENDER = init(EnchantVillageDefender.class, EnchantVillageDefender.ID);
        WITHER = init(EnchantWither.class, EnchantWither.ID);

        // Armor enchants
        AQUAMAN = init(EnchantAquaman.class, EnchantAquaman.ID);
        BUNNY_HOP = init(EnchantBunnyHop.class, EnchantBunnyHop.ID);
        COLD_STEEL = init(EnchantColdSteel.class, EnchantColdSteel.ID);
        ICE_SHIELD = init(EnchantIceShield.class, EnchantIceShield.ID);
        ELEMENTAL_PROTECTION = init(EnchantElementalProtection.class, EnchantElementalProtection.ID);
        FIRE_SHIELD = init(EnchantFireShield.class, EnchantFireShield.ID);
        FLAME_WALKER = init(EnchantFlameWalker.class, EnchantFlameWalker.ID);
        HARDENED = init(EnchantHardened.class, EnchantHardened.ID);
        NIGHT_VISION = init(EnchantNightVision.class, EnchantNightVision.ID);
        REGROWTH = init(EnchantRegrowth.class, EnchantRegrowth.ID);
        SATURATION = init(EnchantSaturation.class, EnchantSaturation.ID);
        SELF_DESTRUCTION = init(EnchantSelfDestruction.class, EnchantSelfDestruction.ID);
        SONIC = init(EnchantSonic.class, EnchantSonic.ID);

        // Bow enchants
        BOMBER = init(EnchantBomber.class, EnchantBomber.ID);
        CONFUSING_ARROWS = init(EnchantConfusingArrows.class, EnchantConfusingArrows.ID);
        DRAGONFIRE_ARROWS = init(EnchantDragonfireArrows.class, EnchantDragonfireArrows.ID);
        ELECTRIFIED_ARROWS = init(EnchantElectrifiedArrows.class, EnchantElectrifiedArrows.ID);
        ENDER_BOW = init(EnchantEnderBow.class, EnchantEnderBow.ID);
        EXPLOSIVE_ARROWS = init(EnchantExplosiveArrows.class, EnchantExplosiveArrows.ID);
        GHAST = init(EnchantGhast.class, EnchantGhast.ID);
        HOVER = init(EnchantHover.class, EnchantHover.ID);
        POISONED_ARROWS = init(EnchantPoisonedArrows.class, EnchantPoisonedArrows.ID);
        WITHERED_ARROWS = init(EnchantWitheredArrows.class, EnchantWitheredArrows.ID);

        // Universal
        CURSE_OF_FRAGILITY = init(EnchantCurseOfFragility.class, EnchantCurseOfFragility.ID);
    }

    public static void setup() {
        // Prevent to register enchantments during the runtime.
        if (ExcellentEnchants.isLoaded) {
            ENCHANT_REGISTRY.values().forEach(ExcellentEnchant::loadConfig);
            return;
        }

        Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);
        for (Field field : EnchantRegister.class.getFields()) {
            if (!ExcellentEnchant.class.isAssignableFrom(field.getType())) continue;

            try {
                ExcellentEnchant enchant = (ExcellentEnchant) field.get(null);
                EnchantRegister.register(enchant);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        Enchantment.stopAcceptingRegistrations();
        PLUGIN.info("Enchantments Registered: " + ENCHANT_REGISTRY.size());
        ExcellentEnchants.isLoaded = true;
    }

    @SuppressWarnings("unchecked")
    public static void shutdown() {
        if (PLUGIN.isEnabled()) return; // Prevent to unregister enchantments during the runtime.

        Map<NamespacedKey, Enchantment> byKey = (Map<NamespacedKey, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byKey");
        Map<String, Enchantment> byName = (Map<String, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byName");

        if (byKey == null || byName == null) return;

        for (ExcellentEnchant enchant : ENCHANT_REGISTRY.values()) {
            if (enchant instanceof ICleanable cleanable) {
                cleanable.clear();
            }

            byKey.remove(enchant.getKey());
            byName.remove(enchant.getName());
            enchant.unregisterListeners();
        }
        ENCHANT_REGISTRY.clear();
        PLUGIN.info("All enchants are unregistered.");
    }

    @Nullable
    public static ExcellentEnchant get(@NotNull NamespacedKey key) {
        return ENCHANT_REGISTRY.get(key);
    }

    @Nullable
    private static <T extends ExcellentEnchant> T init(@NotNull Class<T> clazz, @NotNull String id) {
        if (Config.ENCHANTMENTS_DISABLED.get().contains(id)) return null;

        try {
            return clazz.getConstructor(ExcellentEnchants.class).newInstance(PLUGIN);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static void register(@Nullable ExcellentEnchant enchant) {
        if (enchant == null) return;

        Enchantment.registerEnchantment(enchant);
        ENCHANT_REGISTRY.put(enchant.getKey(), enchant);
        enchant.loadConfig();
        enchant.getConfig().saveChanges();
        enchant.registerListeners();
        PLUGIN.info("Registered enchantment: " + enchant.getId());
    }
}
