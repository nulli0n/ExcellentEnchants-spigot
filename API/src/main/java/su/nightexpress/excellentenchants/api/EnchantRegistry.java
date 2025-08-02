package su.nightexpress.excellentenchants.api;

import com.google.common.collect.ImmutableMap;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.config.DistributionConfig;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDefinition;
import su.nightexpress.excellentenchants.api.wrapper.EnchantDistribution;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantRegistry {

    private static final Map<NamespacedKey, CustomEnchantment> BY_KEY = new HashMap<>();
    private static final Map<String, CustomEnchantment>        BY_ID  = new HashMap<>();

    private static final Map<String, EnchantHolder<?>>   HOLDERS   = new HashMap<>();
    private static final Map<String, EnchantData>        DATAS     = new HashMap<>();
    private static final Map<String, EnchantProvider<?>> PROVIDERS = new HashMap<>();

    public static final EnchantHolder<MiningEnchant>     MINING     = registerHolder("mining", MiningEnchant.class, MiningEnchant::getBreakPriority);
    public static final EnchantHolder<BlockDropEnchant>  BLOCK_DROP = registerHolder("drop", BlockDropEnchant.class, BlockDropEnchant::getDropPriority);
    public static final EnchantHolder<BowEnchant>        BOW        = registerHolder("bow", BowEnchant.class, BowEnchant::getShootPriority);
    public static final EnchantHolder<ArrowEnchant>      ARROW      = registerHolder("arrow", ArrowEnchant.class, BowEnchant::getShootPriority);
    public static final EnchantHolder<TridentEnchant>    TRIDENT    = registerHolder("trident", TridentEnchant.class, TridentEnchant::getLaunchPriority);
    public static final EnchantHolder<AttackEnchant>     ATTACK     = registerHolder("attack", AttackEnchant.class, AttackEnchant::getAttackPriority);
    public static final EnchantHolder<DefendEnchant>     DEFEND     = registerHolder("defend", DefendEnchant.class, DefendEnchant::getProtectPriority);
    public static final EnchantHolder<ProtectionEnchant> PROTECTION = registerHolder("protection", ProtectionEnchant.class, ProtectionEnchant::getProtectionPriority);
    public static final EnchantHolder<ContainerEnchant>  CONTAINER  = registerHolder("inventory", ContainerEnchant.class, ContainerEnchant::getClickPriority);
    public static final EnchantHolder<MoveEnchant>       MOVE       = registerHolder("move", MoveEnchant.class, MoveEnchant::getMovePriority);
    public static final EnchantHolder<KillEnchant>       KILL       = registerHolder("kill", KillEnchant.class, KillEnchant::getKillPriority);
    public static final EnchantHolder<DeathEnchant>      DEATH      = registerHolder("death", DeathEnchant.class, DeathEnchant::getDeathPriority);
    public static final EnchantHolder<ResurrectEnchant>  RESURRECT  = registerHolder("resurrect", ResurrectEnchant.class, ResurrectEnchant::getResurrectPriority);
    public static final EnchantHolder<FishingEnchant>    FISHING    = registerHolder("fishing", FishingEnchant.class, FishingEnchant::getFishingPriority);
    public static final EnchantHolder<InteractEnchant>   INTERACT   = registerHolder("interact", InteractEnchant.class, InteractEnchant::getInteractPriority);
    public static final EnchantHolder<DurabilityEnchant> DURABILITY = registerHolder("durability", DurabilityEnchant.class, DurabilityEnchant::getItemDamagePriority);

    public static final EnchantHolder<InventoryEnchant> INVENTORY = registerHolder("inventory", InventoryEnchant.class, e -> EnchantPriority.NORMAL);
    public static final EnchantHolder<BlockEnchant>     BLOCK     = registerHolder("block", BlockEnchant.class, e -> EnchantPriority.NORMAL);
    public static final EnchantHolder<PassiveEnchant>   PASSIVE   = registerHolder("passive", PassiveEnchant.class, e -> EnchantPriority.NORMAL);

    private static boolean locked;

//    public static void clear() {
//        HOLDERS.values().forEach(EnchantHolder::clear);
//        HOLDERS.clear();
//    }

    public static void lock() {
        locked = true;
        DATAS.clear();
        PROVIDERS.clear();
    }

    public static boolean isLocked() {
        return locked;
    }

    public static void registerEnchant(@NotNull CustomEnchantment enchantment) {
        getHolders().forEach(holder -> holder.accept(enchantment));

        PROVIDERS.remove(enchantment.getId());
        DATAS.remove(enchantment.getId());

        BY_KEY.put(EnchantKeys.custom(enchantment.getId()), enchantment);
        BY_ID.put(enchantment.getId(), enchantment);
    }

    public static boolean isRegistered(@NotNull String id) {
        return getById(id) != null;
    }

    public static boolean isRegistered(@NotNull Enchantment enchantment) {
        return getByBukkit(enchantment) != null;
    }

    @Nullable
    public static CustomEnchantment getById(@NotNull String id) {
        return BY_ID.get(id.toLowerCase());
    }

    @Nullable
    public static CustomEnchantment getByKey(@NotNull NamespacedKey key) {
        return BY_KEY.get(key);
    }

    @Nullable
    public static CustomEnchantment getByBukkit(@NotNull Enchantment enchantment) {
        return getByKey(enchantment.getKey());
    }

    @NotNull
    public static Set<CustomEnchantment> getRegistered() {
        return new HashSet<>(BY_ID.values());
    }

    @NotNull
    public static Set<Enchantment> getRegisteredBukkit() {
        return getRegistered().stream().map(CustomEnchantment::getBukkitEnchantment).collect(Collectors.toSet());
    }

    @NotNull
    public static List<String> getRegisteredNames() {
        return new ArrayList<>(BY_ID.keySet());
    }

    @NotNull
    public static <T extends CustomEnchantment> EnchantHolder<T> registerHolder(@NotNull String name, @NotNull Class<T> clazz, @NotNull Function<T, EnchantPriority> priority) {
        EnchantHolder<T> holder = EnchantHolder.create(clazz, priority);
        registerHolder(name, holder);
        return holder;
    }

    public static <T extends CustomEnchantment> void registerHolder(@NotNull String name, @NotNull EnchantHolder<T> holder) {
        HOLDERS.put(name.toLowerCase(), holder);
    }

    @NotNull
    public static Set<EnchantHolder<?>> getHolders() {
        return new HashSet<>(HOLDERS.values());
    }

    public static void addData(@NotNull String id, @NotNull EnchantDefinition definition, @NotNull EnchantDistribution distribution) {
        addData(id, definition, distribution, false);
    }

    public static void addData(@NotNull String id, @NotNull EnchantDefinition definition, @NotNull EnchantDistribution distribution, boolean curse) {
        DATAS.put(id, new EnchantData(definition, distribution, curse));
    }

    @NotNull
    public static Map<String, EnchantData> getDataMap() {
        return ImmutableMap.copyOf(DATAS);
    }

    @NotNull
    public static Set<EnchantData> getDatas() {
        return new HashSet<>(DATAS.values());
    }

    @Nullable
    public static EnchantData getDataById(@NotNull String id) {
        return DATAS.get(id.toLowerCase());
    }

    public static <T extends CustomEnchantment> void addProvider(@NotNull String id, @NotNull EnchantProvider<T> provider) {
        // Skip disabled enchants.
        if (DistributionConfig.isDisabled(id)) return;

        PROVIDERS.put(id, provider);
    }

    @NotNull
    public static Map<String, EnchantProvider<?>> getProviderMap() {
        return ImmutableMap.copyOf(PROVIDERS);
    }

    @NotNull
    public static Set<EnchantProvider<?>> getProviders() {
        return new HashSet<>(PROVIDERS.values());
    }

    @Nullable
    public static EnchantProvider<?> getProviderById(@NotNull String id) {
        return PROVIDERS.get(id.toLowerCase());
    }
}
