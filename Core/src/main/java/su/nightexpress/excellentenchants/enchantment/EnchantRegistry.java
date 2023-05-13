package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Reflex;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.armor.*;
import su.nightexpress.excellentenchants.enchantment.impl.bow.*;
import su.nightexpress.excellentenchants.enchantment.impl.fishing.AutoFishEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.tool.*;
import su.nightexpress.excellentenchants.enchantment.impl.universal.EnchantCurseOfFragility;
import su.nightexpress.excellentenchants.enchantment.impl.weapon.*;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EnchantRegistry {

    public static final Map<NamespacedKey, ExcellentEnchant> REGISTRY_MAP = new HashMap<>();

    private final ExcellentEnchants plugin;
    private boolean isLocked;

    public EnchantRegistry(@NotNull ExcellentEnchants plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        // Prevent to register enchantments during the runtime.
        if (this.isLocked) {
            REGISTRY_MAP.values().forEach(ExcellentEnchant::loadSettings);
            return;
        }

        Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);

        // Fising Enchants
        this.register(AutoFishEnchant.ID,() -> new AutoFishEnchant(plugin));

        // Tool enchants
        this.register(EnchantBlastMining.ID, () -> new EnchantBlastMining(plugin));
        this.register(EnchantCurseOfBreaking.ID, () -> new EnchantCurseOfBreaking(plugin));
        this.register(EnchantCurseOfMisfortune.ID, () -> new EnchantCurseOfMisfortune(plugin));
        this.register(EnchantDivineTouch.ID, () -> new EnchantDivineTouch(plugin));
        this.register(EnchantHaste.ID, () -> new EnchantHaste(plugin));
        this.register(EnchantLuckyMiner.ID, () -> new EnchantLuckyMiner(plugin));
        this.register(EnchantReplanter.ID, () -> new EnchantReplanter(plugin));
        this.register(EnchantSilkChest.ID, () -> new EnchantSilkChest(plugin));
        this.register(EnchantSmelter.ID, () -> new EnchantSmelter(plugin));
        this.register(EnchantTelekinesis.ID, () -> new EnchantTelekinesis(plugin));
        this.register(EnchantTreasures.ID, () -> new EnchantTreasures(plugin));
        this.register(EnchantTunnel.ID, () -> new EnchantTunnel(plugin));
        this.register(EnchantVeinminer.ID, () -> new EnchantVeinminer(plugin));

        // Weapon enchants
        this.register(EnchantBaneOfNetherspawn.ID, () -> new EnchantBaneOfNetherspawn(plugin));
        this.register(EnchantBlindness.ID, () -> new EnchantBlindness(plugin));
        this.register(EnchantConfusion.ID, () -> new EnchantConfusion(plugin));
        this.register(EnchantCutter.ID, () -> new EnchantCutter(plugin));
        this.register(EnchantDecapitator.ID, () -> new EnchantDecapitator(plugin));
        this.register(EnchantDoubleStrike.ID, () -> new EnchantDoubleStrike(plugin));
        this.register(EnchantExhaust.ID, () -> new EnchantExhaust(plugin));
        this.register(EnchantExpHunter.ID, () -> new EnchantExpHunter(plugin));
        this.register(EnchantIceAspect.ID, () -> new EnchantIceAspect(plugin));
        this.register(EnchantInfernus.ID, () -> new EnchantInfernus(plugin));
        this.register(EnchantNimble.ID, () -> new EnchantNimble(plugin));
        this.register(EnchantParalyze.ID, () -> new EnchantParalyze(plugin));
        this.register(EnchantCure.ID, () -> new EnchantCure(plugin));
        this.register(EnchantRage.ID, () -> new EnchantRage(plugin));
        this.register(EnchantRocket.ID, () -> new EnchantRocket(plugin));
        this.register(EnchantScavenger.ID, () -> new EnchantScavenger(plugin));
        this.register(EnchantSurprise.ID, () -> new EnchantSurprise(plugin));
        this.register(EnchantTemper.ID, () -> new EnchantTemper(plugin));
        this.register(EnchantThrifty.ID, () -> new EnchantThrifty(plugin));
        this.register(EnchantThunder.ID, () -> new EnchantThunder(plugin));
        this.register(EnchantVampire.ID, () -> new EnchantVampire(plugin));
        this.register(EnchantVenom.ID, () -> new EnchantVenom(plugin));
        this.register(EnchantVillageDefender.ID, () -> new EnchantVillageDefender(plugin));
        this.register(EnchantWither.ID, () -> new EnchantWither(plugin));

        // Armor enchants
        this.register(EnchantAquaman.ID, () -> new EnchantAquaman(plugin));
        this.register(EnchantBunnyHop.ID, () -> new EnchantBunnyHop(plugin));
        this.register(EnchantColdSteel.ID, () -> new EnchantColdSteel(plugin));
        this.register(EnchantIceShield.ID, () -> new EnchantIceShield(plugin));
        this.register(EnchantElementalProtection.ID, () -> new EnchantElementalProtection(plugin));
        this.register(EnchantFireShield.ID, () -> new EnchantFireShield(plugin));
        this.register(EnchantFlameWalker.ID, () -> new EnchantFlameWalker(plugin));
        this.register(EnchantHardened.ID, () -> new EnchantHardened(plugin));
        this.register(EnchantNightVision.ID, () -> new EnchantNightVision(plugin));
        this.register(EnchantRegrowth.ID, () -> new EnchantRegrowth(plugin));
        this.register(EnchantSaturation.ID, () -> new EnchantSaturation(plugin));
        this.register(EnchantSelfDestruction.ID, () -> new EnchantSelfDestruction(plugin));
        this.register(EnchantSonic.ID, () -> new EnchantSonic(plugin));

        // Bow enchants
        this.register(EnchantBomber.ID, () -> new EnchantBomber(plugin));
        this.register(EnchantConfusingArrows.ID, () -> new EnchantConfusingArrows(plugin));
        this.register(EnchantDragonfireArrows.ID, () -> new EnchantDragonfireArrows(plugin));
        this.register(EnchantElectrifiedArrows.ID, () -> new EnchantElectrifiedArrows(plugin));
        this.register(EnchantEnderBow.ID, () -> new EnchantEnderBow(plugin));
        this.register(EnchantExplosiveArrows.ID, () -> new EnchantExplosiveArrows(plugin));
        this.register(EnchantGhast.ID, () -> new EnchantGhast(plugin));
        this.register(EnchantHover.ID, () -> new EnchantHover(plugin));
        this.register(EnchantPoisonedArrows.ID, () -> new EnchantPoisonedArrows(plugin));
        this.register(EnchantWitheredArrows.ID, () -> new EnchantWitheredArrows(plugin));

        // Universal
        this.register(EnchantCurseOfFragility.ID, () -> new EnchantCurseOfFragility(plugin));

        Enchantment.stopAcceptingRegistrations();
        this.plugin.info("Enchantments Registered: " + getRegistered().size());
        this.isLocked = true;
    }

    /*@SuppressWarnings("unchecked")
    public void shutdown() {
        if (this.plugin.isEnabled()) return; // Prevent to unregister enchantments during the runtime.

        Map<NamespacedKey, Enchantment> byKey = (Map<NamespacedKey, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byKey");
        Map<String, Enchantment> byName = (Map<String, Enchantment>) Reflex.getFieldValue(Enchantment.class, "byName");

        if (byKey == null || byName == null) return;

        for (ExcellentEnchant enchant : REGISTRY_MAP.values()) {
            if (enchant instanceof ICleanable cleanable) {
                cleanable.clear();
            }

            byKey.remove(enchant.getKey());
            byName.remove(enchant.getName());
            enchant.unregisterListeners();
        }
        REGISTRY_MAP.clear();
        this.plugin.info("All enchants are unregistered.");
    }*/

    private void register(@NotNull String id, @NotNull Supplier<ExcellentEnchant> supplier) {
        if (Config.ENCHANTMENTS_DISABLED.get().contains(id)) return;

        ExcellentEnchant enchant = supplier.get();
        Enchantment.registerEnchantment(enchant);
        REGISTRY_MAP.put(enchant.getKey(), enchant);
        enchant.loadSettings();
        enchant.getConfig().saveChanges();
        enchant.registerListeners();
        this.plugin.info("Registered enchantment: " + enchant.getId());
    }

    @Nullable
    public static ExcellentEnchant getById(@NotNull String id) {
        return getByKey(EnchantUtils.createKey(id));
    }

    @Nullable
    public static ExcellentEnchant getByKey(@NotNull NamespacedKey key) {
        return REGISTRY_MAP.get(key);
    }

    @NotNull
    public static Collection<ExcellentEnchant> getRegistered() {
        return REGISTRY_MAP.values();
    }

    @NotNull
    public static Set<ExcellentEnchant> getOfTier(@NotNull Tier tier) {
        return getRegistered().stream().filter(enchant -> enchant.getTier() == tier)
            .collect(Collectors.toCollection(HashSet::new));
    }

}
