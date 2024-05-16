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
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.config.Config;
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
import su.nightexpress.nightcore.manager.SimpleManager;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.FileUtil;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantRegistry extends SimpleManager<EnchantsPlugin> {

    public static final Map<NamespacedKey, EnchantmentData> BY_KEY = new HashMap<>();
    public static final Map<String, EnchantmentData>        BY_ID  = new HashMap<>();

    private static final Map<Class<? extends EnchantmentData>, Set<? super EnchantmentData>> ENCHANTS_MAP = new HashMap<>();

    private boolean isLocked;

    public EnchantRegistry(@NotNull EnchantsPlugin plugin) {
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
            getRegistered().forEach(data -> {
                data.clear();
                this.load(data);
            });
            return;
        }

        this.plugin.getEnchantNMS().unfreezeRegistry();

        // Fishing Enchants
        this.register(AutoReelEnchant.ID, file -> new AutoReelEnchant(plugin, file));
        this.register(DoubleCatchEnchant.ID, file -> new DoubleCatchEnchant(plugin, file));
        this.register(SeasonedAnglerEnchant.ID, file -> new SeasonedAnglerEnchant(plugin, file));
        this.register(SurvivalistEnchant.ID, file -> new SurvivalistEnchant(plugin, file));
        this.register(CurseOfDrownedEnchant.ID, file -> new CurseOfDrownedEnchant(plugin, file));
        this.register(RiverMasterEnchant.ID, file -> new RiverMasterEnchant(plugin, file));

        // Tool enchants
        this.register(BlastMiningEnchant.ID, file -> new BlastMiningEnchant(plugin, file));
        this.register(CurseOfBreakingEnchant.ID, file -> new CurseOfBreakingEnchant(plugin, file));
        this.register(CurseOfMisfortuneEnchant.ID, file -> new CurseOfMisfortuneEnchant(plugin, file));
        this.register(SilkSpawnerEnchant.ID, file -> new SilkSpawnerEnchant(plugin, file));
        this.register(HasteEnchant.ID, file -> new HasteEnchant(plugin, file));
        this.register(LuckyMinerEnchant.ID, file -> new LuckyMinerEnchant(plugin, file));
        this.register(ReplanterEnchant.ID, file -> new ReplanterEnchant(plugin, file));
        this.register(SilkChestEnchant.ID, file -> new SilkChestEnchant(plugin, file));
        this.register(SmelterEnchant.ID, file -> new SmelterEnchant(plugin, file));
        this.register(TelekinesisEnchant.ID, file -> new TelekinesisEnchant(plugin, file));
        this.register(TreasureHunterEnchant.ID, file -> new TreasureHunterEnchant(plugin, file));
        this.register(TunnelEnchant.ID, file -> new TunnelEnchant(plugin, file));
        this.register(VeinminerEnchant.ID, file -> new VeinminerEnchant(plugin, file));

        // Weapon enchants
        this.register(BaneOfNetherspawnEnchant.ID, file -> new BaneOfNetherspawnEnchant(plugin, file));
        this.register(BlindnessEnchant.ID, file -> new BlindnessEnchant(plugin, file));
        this.register(ConfusionEnchant.ID, file -> new ConfusionEnchant(plugin, file));
        this.register(CutterEnchant.ID, file -> new CutterEnchant(plugin, file));
        this.register(CurseOfDeathEnchant.ID, file -> new CurseOfDeathEnchant(plugin, file));
        this.register(DecapitatorEnchant.ID, file -> new DecapitatorEnchant(plugin, file));
        this.register(DoubleStrikeEnchant.ID, file -> new DoubleStrikeEnchant(plugin, file));
        this.register(ExhaustEnchant.ID, file -> new ExhaustEnchant(plugin, file));
        this.register(WisdomEnchant.ID, file -> new WisdomEnchant(plugin, file));
        this.register(IceAspectEnchant.ID, file -> new IceAspectEnchant(plugin, file));
        this.register(InfernusEnchant.ID, file -> new InfernusEnchant(plugin, file));
        this.register(NimbleEnchant.ID, file -> new NimbleEnchant(plugin, file));
        this.register(ParalyzeEnchant.ID, file -> new ParalyzeEnchant(plugin, file));
        this.register(CureEnchant.ID, file -> new CureEnchant(plugin, file));
        this.register(RageEnchant.ID, file -> new RageEnchant(plugin, file));
        this.register(RocketEnchant.ID, file -> new RocketEnchant(plugin, file));
        this.register(ScavengerEnchant.ID, file -> new ScavengerEnchant(plugin, file));
        this.register(SurpriseEnchant.ID, file -> new SurpriseEnchant(plugin, file));
        this.register(SwiperEnchant.ID, file -> new SwiperEnchant(plugin, file));
        this.register(TemperEnchant.ID, file -> new TemperEnchant(plugin, file));
        this.register(ThriftyEnchant.ID, file -> new ThriftyEnchant(plugin, file));
        this.register(ThunderEnchant.ID, file -> new ThunderEnchant(plugin, file));
        this.register(VampireEnchant.ID, file -> new VampireEnchant(plugin, file));
        this.register(VenomEnchant.ID, file -> new VenomEnchant(plugin, file));
        this.register(VillageDefenderEnchant.ID, file -> new VillageDefenderEnchant(plugin, file));
        this.register(WitherEnchant.ID, file -> new WitherEnchant(plugin, file));

        // Armor enchants
        this.register(WaterBreathingEnchant.ID, file -> new WaterBreathingEnchant(plugin, file));
        this.register(JumpingEnchant.ID, file -> new JumpingEnchant(plugin, file));
        this.register(ColdSteelEnchant.ID, file -> new ColdSteelEnchant(plugin, file));
        this.register(IceShieldEnchant.ID, file -> new IceShieldEnchant(plugin, file));
        this.register(ElementalProtectionEnchant.ID, file -> new ElementalProtectionEnchant(plugin, file));
        this.register(FireShieldEnchant.ID, file -> new FireShieldEnchant(plugin, file));
        this.register(FlameWalkerEnchant.ID, file -> new FlameWalkerEnchant(plugin, file));
        this.register(HardenedEnchant.ID, file -> new HardenedEnchant(plugin, file));
        this.register(NightVisionEnchant.ID, file -> new NightVisionEnchant(plugin, file));
        this.register(RegrowthEnchant.ID, file -> new RegrowthEnchant(plugin, file));
        this.register(SaturationEnchant.ID, file -> new SaturationEnchant(plugin, file));
        this.register(KamikadzeEnchant.ID, file -> new KamikadzeEnchant(plugin, file));
        this.register(StoppingForceEnchant.ID, file -> new StoppingForceEnchant(plugin, file));
        this.register(SpeedyEnchant.ID, file -> new SpeedyEnchant(plugin, file));

        // Bow enchants
        this.register(BomberEnchant.ID, file -> new BomberEnchant(plugin, file));
        this.register(ConfusingArrowsEnchant.ID, file -> new ConfusingArrowsEnchant(plugin, file));
        this.register(DragonfireArrowsEnchant.ID, file -> new DragonfireArrowsEnchant(plugin, file));
        this.register(ElectrifiedArrowsEnchant.ID, file -> new ElectrifiedArrowsEnchant(plugin, file));
        this.register(EnderBowEnchant.ID, file -> new EnderBowEnchant(plugin, file));
        this.register(ExplosiveArrowsEnchant.ID, file -> new ExplosiveArrowsEnchant(plugin, file));
        this.register(FlareEnchant.ID, file -> new FlareEnchant(plugin, file));
        this.register(GhastEnchant.ID, file -> new GhastEnchant(plugin, file));
        this.register(HoverEnchant.ID, file -> new HoverEnchant(plugin, file));
        this.register(SniperEnchant.ID, file -> new SniperEnchant(plugin, file));
        this.register(PoisonedArrowsEnchant.ID, file -> new PoisonedArrowsEnchant(plugin, file));
        this.register(VampiricArrowsEnchant.ID, file -> new VampiricArrowsEnchant(plugin, file));
        this.register(WitheredArrowsEnchant.ID, file -> new WitheredArrowsEnchant(plugin, file));
        this.register(DarknessArrowsEnchant.ID, file -> new DarknessArrowsEnchant(plugin, file));
        this.register(DarknessCloakEnchant.ID, file -> new DarknessCloakEnchant(plugin, file));

        // Universal
        this.register(CurseOfFragilityEnchant.ID, file -> new CurseOfFragilityEnchant(plugin, file));
        this.register(CurseOfMediocrityEnchant.ID, file -> new CurseOfMediocrityEnchant(plugin, file));
        this.register(SoulboundEnchant.ID, file -> new SoulboundEnchant(plugin, file));
        this.register(RestoreEnchant.ID, file -> new RestoreEnchant(plugin, file));

        this.plugin.getEnchantNMS().freezeRegistry();
        this.plugin.info("Enchantments Registered: " + BY_ID.size());
        this.isLocked = true;
    }

    @Override
    protected void onShutdown() {
        if (!isLocked) {
            getRegistered().forEach(EnchantmentData::clear);
            ENCHANTS_MAP.clear();
        }
    }

    public <T extends EnchantmentData> void registerType(@NotNull Class<T> enchantClass) {
        ENCHANTS_MAP.computeIfAbsent(enchantClass, k -> new HashSet<>());
    }

    public <E extends Event, T extends EnchantmentData> void registerWrapper(@NotNull Class<E> eventClass,
                                                                             @NotNull Class<T> enchantClass,
                                                                             @NotNull DataGather<E, T> dataGather) {

        for (EventPriority priority : EventPriority.values()) {
            WrappedEvent<E, T> event = new WrappedEvent<>(plugin, priority, eventClass, enchantClass, dataGather);
            plugin.getPluginManager().registerEvent(eventClass, event, priority, event, plugin, true);
        }

        this.registerType(enchantClass);
    }

    private <T extends EnchantmentData> boolean registerEnchantType(@NotNull T enchant) {
        Class<? extends EnchantmentData> enchantClass = enchant.getClass();

        Set<Class<?>> assignables = ENCHANTS_MAP.keySet().stream().filter(clazz -> clazz.isAssignableFrom(enchantClass)).collect(Collectors.toSet());
        if (assignables.isEmpty()) {
            this.plugin.warn("Could not register enchantment '" + enchant.getId() + "': Enchantment type is not registered.");
            return false;
        }

        assignables.forEach(clazz -> ENCHANTS_MAP.get(clazz).add(enchant));
        return true;
    }

    private void register(@NotNull String id, @NotNull Function<File, EnchantmentData> supplier) {
        if (Config.ENCHANTMENTS_DISABLED_LIST.get().contains(id)) return;

        File file = new File(plugin.getDataFolder() + Config.DIR_ENCHANTS, id + ".yml");
        FileUtil.create(file);

        EnchantmentData enchantmentData = supplier.apply(file);
        if (BukkitThing.getEnchantment(enchantmentData.getId()) != null) {
            this.plugin.error("Could not register '" + enchantmentData.getId() + "': Such enchantment already registered.");
            return;
        }

        if (!this.registerEnchantType(enchantmentData) || !enchantmentData.checkServerRequirements()) {
            return;
        }

        this.load(enchantmentData);

        this.plugin.getEnchantNMS().registerEnchantment(enchantmentData);

        BY_KEY.put(NamespacedKey.minecraft(enchantmentData.getId()), enchantmentData);
        BY_ID.put(enchantmentData.getId(), enchantmentData);

        this.plugin.info("Registered enchantment: " + enchantmentData.getId());
    }

    private void load(@NotNull EnchantmentData enchantmentData) {
        enchantmentData.load();
    }

    @NotNull
    public static Set<PassiveEnchant> getPeriodicEnchants() {
        return getEnchantments(PassiveEnchant.class);
    }

    @NotNull
    public static <T extends EnchantmentData> Set<T> getEnchantments(@NotNull Class<T> clazz) {
        Set<T> enchants = new HashSet<>();

        ENCHANTS_MAP.getOrDefault(clazz, Collections.emptySet()).forEach(talent -> {
            enchants.add(clazz.cast(talent));
        });
        return enchants;
    }

    @NotNull
    public static Set<EnchantmentData> getEnchantments(@NotNull Rarity rarity) {
        return BY_ID.values().stream()
            .filter(enchantmentData -> enchantmentData.getRarity() == rarity)
            .collect(Collectors.toCollection(HashSet::new));
    }

    public static boolean isRegistered(@NotNull String id) {
        return getById(id) != null;
    }

    @Nullable
    public static EnchantmentData getById(@NotNull String id) {
        return BY_ID.get(id.toLowerCase());
    }

    @Nullable
    public static EnchantmentData getByKey(@NotNull NamespacedKey key) {
        return BY_KEY.get(key);
    }

    @NotNull
    public static Set<EnchantmentData> getByRarity(@NotNull Rarity rarity) {
        return getRegistered().stream().filter(data -> data.getRarity() == rarity).collect(Collectors.toSet());
    }

    @NotNull
    public static Collection<EnchantmentData> getRegistered() {
        return new HashSet<>(BY_ID.values());
    }
}
