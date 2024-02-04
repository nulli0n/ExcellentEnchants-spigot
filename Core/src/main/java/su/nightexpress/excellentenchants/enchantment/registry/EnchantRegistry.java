package su.nightexpress.excellentenchants.enchantment.registry;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.armor.*;
import su.nightexpress.excellentenchants.enchantment.impl.bow.*;
import su.nightexpress.excellentenchants.enchantment.impl.fishing.*;
import su.nightexpress.excellentenchants.enchantment.impl.tool.*;
import su.nightexpress.excellentenchants.enchantment.impl.universal.CurseOfFragilityEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.universal.RestoreEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.universal.SoulboundEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.weapon.*;
import su.nightexpress.excellentenchants.enchantment.registry.wrapper.DataGather;
import su.nightexpress.excellentenchants.enchantment.registry.wrapper.DataGathers;
import su.nightexpress.excellentenchants.enchantment.registry.wrapper.WrappedEvent;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class EnchantRegistry extends AbstractManager<ExcellentEnchants> {

    public static final Map<NamespacedKey, ExcellentEnchant> REGISTRY_MAP = new HashMap<>();
    private static final Map<Class<? extends IEnchantment>, Set<? super IEnchantment>> ENCHANTS_MAP = new HashMap<>();

    private boolean isLocked;

    public EnchantRegistry(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.registerType(GenericEnchant.class);
        this.registerType(PassiveEnchant.class);

        this.registerWrapper(BlockBreakEvent.class, BlockBreakEnchant.class, DataGathers.BLOCK_BREAK);
        this.registerWrapper(BlockDropItemEvent.class, BlockDropEnchant.class, DataGathers.BLOCK_DROP);
        this.registerWrapper(EntityShootBowEvent.class, BowEnchant.class, DataGathers.BOW_SHOOT);
        this.registerWrapper(ProjectileHitEvent.class, BowEnchant.class, DataGathers.PROJECTILE_HIT);
        this.registerWrapper(EntityDamageByEntityEvent.class, BowEnchant.class, DataGathers.ENTITY_DAMAGE_SHOOT);
        this.registerWrapper(EntityDamageByEntityEvent.class, CombatEnchant.class, DataGathers.ENTITY_DAMAGE_ATTACK);
        this.registerWrapper(EntityDamageByEntityEvent.class, CombatEnchant.class, DataGathers.ENTITY_DAMAGE_DEFENSE);
        this.registerWrapper(EntityDeathEvent.class, DeathEnchant.class, DataGathers.ENTITY_KILL);
        this.registerWrapper(EntityDeathEvent.class, DeathEnchant.class, DataGathers.ENTITY_DEATH);
        this.registerWrapper(EntityResurrectEvent.class, DeathEnchant.class, DataGathers.ENTITY_RESURRECT);
        this.registerWrapper(PlayerFishEvent.class, FishingEnchant.class, DataGathers.FISHING);
        this.registerWrapper(PlayerInteractEvent.class, InteractEnchant.class, DataGathers.INTERACT);

        // Prevent to register enchantments during the runtime.
        if (this.isLocked) {
            REGISTRY_MAP.values().forEach(enchant -> {
                enchant.loadSettings();
                enchant.registerListeners();
            });
            return;
        }

        //if (Version.isAtLeast(Version.V1_20_R3)) {
            this.plugin.getEnchantNMS().unfreezeRegistry();
        //}
        //else {
        //    Reflex.setFieldValue(Enchantment.class, "acceptingNew", true);
        //}

        // Fishing Enchants
        this.register(AutoReelEnchant.ID,() -> new AutoReelEnchant(plugin));
        this.register(DoubleCatchEnchant.ID, () -> new DoubleCatchEnchant(plugin));
        this.register(SeasonedAnglerEnchant.ID, () -> new SeasonedAnglerEnchant(plugin));
        this.register(SurvivalistEnchant.ID, () -> new SurvivalistEnchant(plugin));
        this.register(CurseOfDrownedEnchant.ID, () -> new CurseOfDrownedEnchant(plugin));
        this.register(RiverMasterEnchant.ID, () -> new RiverMasterEnchant(plugin));

        // Tool enchants
        this.register(BlastMiningEnchant.ID, () -> new BlastMiningEnchant(plugin));
        this.register(CurseOfBreakingEnchant.ID, () -> new CurseOfBreakingEnchant(plugin));
        this.register(CurseOfMisfortuneEnchant.ID, () -> new CurseOfMisfortuneEnchant(plugin));
        this.register(DivineTouchEnchant.ID, () -> new DivineTouchEnchant(plugin));
        this.register(HasteEnchant.ID, () -> new HasteEnchant(plugin));
        this.register(LuckyMinerEnchant.ID, () -> new LuckyMinerEnchant(plugin));
        this.register(ReplanterEnchant.ID, () -> new ReplanterEnchant(plugin));
        this.register(SilkChestEnchant.ID, () -> new SilkChestEnchant(plugin));
        this.register(SmelterEnchant.ID, () -> new SmelterEnchant(plugin));
        this.register(TelekinesisEnchant.ID, () -> new TelekinesisEnchant(plugin));
        this.register(TreasuresEnchant.ID, () -> new TreasuresEnchant(plugin));
        this.register(TunnelEnchant.ID, () -> new TunnelEnchant(plugin));
        this.register(VeinminerEnchant.ID, () -> new VeinminerEnchant(plugin));

        // Weapon enchants
        this.register(EnchantBaneOfNetherspawn.ID, () -> new EnchantBaneOfNetherspawn(plugin));
        this.register(EnchantBlindness.ID, () -> new EnchantBlindness(plugin));
        this.register(EnchantConfusion.ID, () -> new EnchantConfusion(plugin));
        this.register(EnchantCutter.ID, () -> new EnchantCutter(plugin));
        this.register(CurseOfDeathEnchant.ID, () -> new CurseOfDeathEnchant(plugin));
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
        this.register(SwiperEnchant.ID, () -> new SwiperEnchant(plugin));
        this.register(EnchantTemper.ID, () -> new EnchantTemper(plugin));
        this.register(EnchantThrifty.ID, () -> new EnchantThrifty(plugin));
        this.register(EnchantThunder.ID, () -> new EnchantThunder(plugin));
        this.register(EnchantVampire.ID, () -> new EnchantVampire(plugin));
        this.register(EnchantVenom.ID, () -> new EnchantVenom(plugin));
        this.register(EnchantVillageDefender.ID, () -> new EnchantVillageDefender(plugin));
        this.register(EnchantWither.ID, () -> new EnchantWither(plugin));

        // Armor enchants
        this.register(AquamanEnchant.ID, () -> new AquamanEnchant(plugin));
        this.register(JumpingEnchant.ID, () -> new JumpingEnchant(plugin));
        this.register(ColdSteelEnchant.ID, () -> new ColdSteelEnchant(plugin));
        this.register(IceShieldEnchant.ID, () -> new IceShieldEnchant(plugin));
        this.register(ElementalProtectionEnchant.ID, () -> new ElementalProtectionEnchant(plugin));
        this.register(FireShieldEnchant.ID, () -> new FireShieldEnchant(plugin));
        this.register(FlameWalkerEnchant.ID, () -> new FlameWalkerEnchant(plugin));
        this.register(HardenedEnchant.ID, () -> new HardenedEnchant(plugin));
        this.register(NightVisionEnchant.ID, () -> new NightVisionEnchant(plugin));
        this.register(RegrowthEnchant.ID, () -> new RegrowthEnchant(plugin));
        this.register(SaturationEnchant.ID, () -> new SaturationEnchant(plugin));
        this.register(KamikadzeEnchant.ID, () -> new KamikadzeEnchant(plugin));
        this.register(StoppingForceEnchant.ID, () -> new StoppingForceEnchant(plugin));
        this.register(SpeedyEnchant.ID, () -> new SpeedyEnchant(plugin));

        // Bow enchants
        this.register(EnchantBomber.ID, () -> new EnchantBomber(plugin));
        this.register(EnchantConfusingArrows.ID, () -> new EnchantConfusingArrows(plugin));
        this.register(EnchantDragonfireArrows.ID, () -> new EnchantDragonfireArrows(plugin));
        this.register(EnchantElectrifiedArrows.ID, () -> new EnchantElectrifiedArrows(plugin));
        this.register(EnchantEnderBow.ID, () -> new EnchantEnderBow(plugin));
        this.register(EnchantExplosiveArrows.ID, () -> new EnchantExplosiveArrows(plugin));
        this.register(FlareEnchant.ID, () -> new FlareEnchant(plugin));
        this.register(EnchantGhast.ID, () -> new EnchantGhast(plugin));
        this.register(EnchantHover.ID, () -> new EnchantHover(plugin));
        this.register(SniperEnchant.ID, () -> new SniperEnchant(plugin));
        this.register(EnchantPoisonedArrows.ID, () -> new EnchantPoisonedArrows(plugin));
        this.register(VampiricArrowsEnchant.ID, () -> new VampiricArrowsEnchant(plugin));
        this.register(EnchantWitheredArrows.ID, () -> new EnchantWitheredArrows(plugin));
        if (Version.isAbove(Version.V1_18_R2)) {
            this.register(DarknessArrowsEnchant.ID, () -> new DarknessArrowsEnchant(plugin));
            this.register(DarknessCloakEnchant.ID, () -> new DarknessCloakEnchant(plugin));
        }

        // Universal
        this.register(CurseOfFragilityEnchant.ID, () -> new CurseOfFragilityEnchant(plugin));
        this.register(CurseOfMediocrityEnchant.ID, () -> new CurseOfMediocrityEnchant(plugin));
        this.register(SoulboundEnchant.ID, () -> new SoulboundEnchant(plugin));
        this.register(RestoreEnchant.ID, () -> new RestoreEnchant(plugin));

        //if (Version.isAtLeast(Version.V1_20_R3)) {
            this.plugin.getEnchantNMS().freezeRegistry();
        //}
        //else {
        //    Enchantment.stopAcceptingRegistrations();
        //}
        this.plugin.info("Enchantments Registered: " + EnchantRegistry.getRegistered().size());
        this.isLocked = true;
    }

    @Override
    protected void onShutdown() {
        if (!isLocked) {
            ENCHANTS_MAP.clear();
        }
    }

    public <T extends IEnchantment> void registerType(@NotNull Class<T> enchantClass) {
        ENCHANTS_MAP.computeIfAbsent(enchantClass, k -> new HashSet<>());
    }

    public <E extends Event, T extends IEnchantment> void registerWrapper(@NotNull Class<E> eventClass,
                                                                             @NotNull Class<T> enchantClass,
                                                                             @NotNull DataGather<E, T> dataGather) {

        for (EventPriority priority : EventPriority.values()) {
            WrappedEvent<E, T> event = new WrappedEvent<>(plugin, priority, eventClass, enchantClass, dataGather);
            plugin.getPluginManager().registerEvent(eventClass, event, priority, event, plugin, true);
        }

        this.registerType(enchantClass);
    }

    private <T extends IEnchantment> boolean registerEnchantType(@NotNull T enchant) {
        Class<? extends IEnchantment> enchantClass = enchant.getClass();

        Set<Class<?>> assignables = ENCHANTS_MAP.keySet().stream().filter(clazz -> clazz.isAssignableFrom(enchantClass)).collect(Collectors.toSet());
        if (assignables.isEmpty()) {
            this.plugin.warn("Could not register enchantment '" + enchant.getId() + "': Enchantment type is not registered.");
            return false;
        }

        assignables.forEach(clazz -> ENCHANTS_MAP.get(clazz).add(enchant));
        return true;
    }

    private void register(@NotNull String id, @NotNull Supplier<ExcellentEnchant> supplier) {
        if (Config.ENCHANTMENTS_DISABLED.get().contains(id)) return;


        ExcellentEnchant enchant = supplier.get();
        if (EnchantUtils.getEnchantment(enchant.getKey()) /*Enchantment.getByKey(enchant.getKey())*/ != null) {
            this.plugin.error("Could not register '" + enchant.getId() + "': Such enchantment already registered.");
            return;
        }

        if (!this.registerEnchantType(enchant)) {
            return;
        }

        //if (Version.isAtLeast(Version.V1_20_R3)) {
            this.plugin.getEnchantNMS().registerEnchantment(enchant);
        //}
        //else {
            //Enchantment.registerEnchantment(enchant);
        //}

        REGISTRY_MAP.put(enchant.getKey(), enchant);
        enchant.loadSettings();
        enchant.getConfig().saveChanges();
        enchant.registerListeners();
        this.plugin.info("Registered enchantment: " + enchant.getId());
    }

    @NotNull
    public static Set<PassiveEnchant> getPeriodicEnchants() {
        return getEnchantments(PassiveEnchant.class);
    }

    @NotNull
    public static <T extends IEnchantment> Set<T> getEnchantments(@NotNull Class<T> clazz) {
        Set<T> enchants = new HashSet<>();

        ENCHANTS_MAP.getOrDefault(clazz, Collections.emptySet()).forEach(talent -> {
            enchants.add(clazz.cast(talent));
        });
        return enchants;
        //Set<? super T> set = new HashSet<>(ENCHANTS_MAP.getOrDefault(clazz, Collections.emptySet()));
        //return (Set<T>) set;
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
        return getRegistered().stream().filter(enchant -> enchant.getTier() == tier).collect(Collectors.toCollection(HashSet::new));
    }
}
