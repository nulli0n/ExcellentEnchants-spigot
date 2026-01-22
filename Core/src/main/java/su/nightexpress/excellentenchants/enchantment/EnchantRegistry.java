package su.nightexpress.excellentenchants.enchantment;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.type.*;
import su.nightexpress.nightcore.util.LowerCase;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnchantRegistry {

    private static final Map<NamespacedKey, CustomEnchantment> BY_KEY = new HashMap<>();
    private static final Map<String, CustomEnchantment>        BY_ID  = new HashMap<>();

    private static final Map<String, EnchantHolder<?>>   HOLDERS   = new HashMap<>();

    public static final EnchantHolder<MiningEnchant>     MINING     = registerHolder("mining", MiningEnchant.class, MiningEnchant::getBreakPriority);
    public static final EnchantHolder<BlockDropEnchant>  BLOCK_DROP = registerHolder("drop", BlockDropEnchant.class, BlockDropEnchant::getDropPriority);
    public static final EnchantHolder<BowEnchant>        BOW        = registerHolder("bow", BowEnchant.class, BowEnchant::getShootPriority);
    public static final EnchantHolder<ArrowEnchant>      ARROW      = registerHolder("arrow", ArrowEnchant.class, BowEnchant::getShootPriority);
    public static final EnchantHolder<TridentEnchant>    TRIDENT    = registerHolder("trident", TridentEnchant.class, TridentEnchant::getLaunchPriority);
    public static final EnchantHolder<AttackEnchant>     ATTACK     = registerHolder("attack", AttackEnchant.class, AttackEnchant::getAttackPriority);
    public static final EnchantHolder<DefendEnchant>     DEFEND     = registerCachedHolder("defend", DefendEnchant.class, DefendEnchant::getProtectPriority);
    public static final EnchantHolder<ProtectionEnchant> PROTECTION = registerCachedHolder("protection", ProtectionEnchant.class, ProtectionEnchant::getProtectionPriority);
    public static final EnchantHolder<ContainerEnchant>  CONTAINER  = registerHolder("container", ContainerEnchant.class, ContainerEnchant::getClickPriority);
    public static final EnchantHolder<MoveEnchant>       MOVE       = registerCachedHolder("move", MoveEnchant.class, MoveEnchant::getMovePriority);
    public static final EnchantHolder<KillEnchant>       KILL       = registerHolder("kill", KillEnchant.class, KillEnchant::getKillPriority);
    public static final EnchantHolder<DeathEnchant>      DEATH      = registerHolder("death", DeathEnchant.class, DeathEnchant::getDeathPriority);
    public static final EnchantHolder<ResurrectEnchant>  RESURRECT  = registerHolder("resurrect", ResurrectEnchant.class, ResurrectEnchant::getResurrectPriority);
    public static final EnchantHolder<FishingEnchant>    FISHING    = registerHolder("fishing", FishingEnchant.class, FishingEnchant::getFishingPriority);
    public static final EnchantHolder<InteractEnchant>   INTERACT   = registerHolder("interact", InteractEnchant.class, InteractEnchant::getInteractPriority);
    public static final EnchantHolder<DurabilityEnchant> DURABILITY = registerCachedHolder("durability", DurabilityEnchant.class, DurabilityEnchant::getItemDamagePriority);
    public static final EnchantHolder<BlockChangeEnchant> BLOCK_CHANGE = registerCachedHolder("block_change", BlockChangeEnchant.class, e -> EnchantPriority.NORMAL);

    public static final EnchantHolder<InventoryEnchant> INVENTORY = registerHolder("inventory", InventoryEnchant.class, e -> EnchantPriority.NORMAL);
    public static final EnchantHolder<BlockEnchant>     BLOCK     = registerHolder("block", BlockEnchant.class, e -> EnchantPriority.NORMAL);
    public static final EnchantHolder<PassiveEnchant>   PASSIVE   = registerCachedHolder("passive", PassiveEnchant.class, e -> EnchantPriority.NORMAL);

    public static void registerEnchant(@NotNull CustomEnchantment enchantment) {
        getHolders().forEach(holder -> holder.accept(enchantment));

        BY_KEY.put(EnchantsKeys.create(enchantment.getId()), enchantment);
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
        return BY_ID.get(LowerCase.INTERNAL.apply(id));
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
    public static <T extends CustomEnchantment> EnchantHolder<T> registerHolder(@NotNull String name, @NotNull Class<T> type, @NotNull Function<T, EnchantPriority> priority) {
        return registerHolder(name, type, priority, EnchantHolder::withNoCache);
    }

    @NotNull
    public static <T extends CustomEnchantment> EnchantHolder<T> registerCachedHolder(@NotNull String name, @NotNull Class<T> type, @NotNull Function<T, EnchantPriority> priority) {
        return registerHolder(name, type, priority, EnchantHolder::cached);
    }

    @NotNull
    private static <T extends CustomEnchantment> EnchantHolder<T> registerHolder(@NotNull String name,
                                                                                 @NotNull Class<T> type,
                                                                                 @NotNull Function<T, EnchantPriority> priority,
                                                                                 @NotNull BiFunction<Class<T>, Function<T, EnchantPriority>, EnchantHolder<T>> function) {
        EnchantHolder<T> holder = function.apply(type, priority);
        registerHolder(name, holder);
        return holder;
    }

    public static <T extends CustomEnchantment> void registerHolder(@NotNull String name, @NotNull EnchantHolder<T> holder) {
        HOLDERS.put(LowerCase.INTERNAL.apply(name), holder);
    }

    @NotNull
    public static Set<EnchantHolder<?>> getHolders() {
        return new HashSet<>(HOLDERS.values());
    }
}
