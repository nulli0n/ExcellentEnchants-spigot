package su.nightexpress.excellentenchants.enchantment.util;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EnchantUtils {

    //@Deprecated
    //public static final EquipmentSlot[] EQUIPMENT_SLOTS = {EquipmentSlot.HAND, EquipmentSlot.OFF_HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static final Set<UUID>                      IGNORE_DISPLAY_UPDATE    = new HashSet<>();
    private static final Map<UUID, EnchantedProjectile> ENCHANTED_PROJECTILE_MAP = new ConcurrentHashMap<>();

    private static EnchantsPlugin plugin;
    private static boolean busyBreak;

    public static void hook(@NotNull EnchantsPlugin plugin) {
        EnchantUtils.plugin = plugin;
        EnchantUtils.busyBreak = false;
    }

    public static boolean isBusyByOthers() {
        return false;
    }

    public static boolean isBusyByEnchant() {
        return busyBreak;
    }

    public static boolean isBusy() {
        return isBusyByEnchant() || isBusyByOthers();
    }

    public static void busyBreak(@NotNull Player player, @NotNull Block block) {
        busyBreak = true;
        player.breakBlock(block);
        busyBreak = false;
    }

    public static void safeBusyBreak(@NotNull Player player, @NotNull Block block) {
        if (!isBusy()) {
            busyBreak(player, block);
        }
    }

    public static boolean isIgnoringDisplayUpdate(@NotNull Player player) {
        return IGNORE_DISPLAY_UPDATE.contains(player.getUniqueId()) || player.getGameMode() == GameMode.CREATIVE;
    }

    public static void doIgnoreDisplayUpdate(@NotNull Player player, @NotNull Runnable runnable) {
        addIgnoreDisplayUpdate(player);
        runnable.run();
        removeIgnoreDisplayUpdate(player);
    }

    public static void addIgnoreDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.add(player.getUniqueId());
    }

    public static void removeIgnoreDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.remove(player.getUniqueId());
    }

    /*@NotNull
    public static NamespacedKey createKey(@NotNull String id) {
        return NamespacedKey.minecraft(id.toLowerCase());
    }*/

    @Nullable
    public static String getLocalized(@NotNull String keyRaw) {
        Enchantment enchantment = BukkitThing.getEnchantment(keyRaw);
        return enchantment == null ? null : getLocalized(enchantment);
    }

    @NotNull
    public static String getLocalized(@NotNull Enchantment enchantment) {
        EnchantmentData enchant = EnchantRegistry.getByKey(enchantment.getKey());
        if (enchant != null) {
            return enchant.getName();
        }
        return LangAssets.get(enchantment);
    }

    public static boolean isEnchantable(@NotNull ItemStack item) {
        return plugin.getEnchantNMS().isEnchantable(item);
//        if (item.getType().isAir()) return false;
//
//        return item.getType() == Material.ENCHANTED_BOOK || Stream.of(EnchantmentTarget.values()).anyMatch(target -> target.includes(item));
    }

    public static boolean isEnchantedBook(@NotNull ItemStack item) {
        return /*item.getType() == Material.BOOK || */item.getType() == Material.ENCHANTED_BOOK;
    }

    public static boolean add(@NotNull ItemStack item, @NotNull Enchantment enchantment, int level, boolean force) {
        if (!force && (!enchantment.canEnchantItem(item) && !isEnchantedBook(item))) return false;

        //remove(item, enchantment);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            if (!storageMeta.addStoredEnchant(enchantment, level, true)) return false;
        }
        else {
            if (!meta.addEnchant(enchantment, level, true)) return false;
        }
        item.setItemMeta(meta);

        return true;
    }

    public static void removeAll(@NotNull ItemStack item) {
        //ItemMeta meta = item.getItemMeta();
        //if (meta == null) return;

        ItemUtil.editMeta(item, meta -> {
            getEnchantments(meta).keySet().forEach(enchantment -> remove(meta, enchantment));
        });
//        if (Version.isAtLeast(Version.V1_20_R3)) {
//            item.removeEnchantments();
//        }
//        else {
        //getEnchantments(meta).keySet().forEach(enchantment -> remove(meta, enchantment));
//        }
    }

    public static void remove(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        ItemUtil.editMeta(item, meta -> {
            remove(meta, enchantment);
        });
    }

    public static void remove(@NotNull ItemMeta meta, @NotNull Enchantment enchantment) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchantment);
        }
        else {
            meta.removeEnchant(enchantment);
        }
    }

    /*public static void updateChargesDisplay(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
            updateDisplay(item);
        }
    }*/

    public static boolean canHaveDescription(@NotNull ItemStack item) {
        if (!Config.ENCHANTMENTS_DISPLAY_DESCRIPTION_ENABLED.get()) return false;

        if (Config.ENCHANTMENTS_DISPLAY_DESCRIPTION_BOOKS_ONLY.get()) {
            return isEnchantedBook(item);
        }

        return true;
    }

    @Deprecated
    public static boolean updateDisplay(@NotNull ItemStack item) {
        /*if (Config.ENCHANTMENTS_DISPLAY_MODE.get() != 1) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (!isEnchantable(item)) {
            PDCUtil.remove(item, Keys.loreSize);
            return false;
        }

        Map<EnchantmentData, Integer> enchantDataMap = Lists.sort(getCustomEnchantments(item),
            Comparator.comparing(
                (Map.Entry<EnchantmentData, Integer> entry) -> entry.getKey().getRarity().getWeight()
            )
            .thenComparing(entry -> entry.getKey().getName())
        );

        int sizeCached = PDCUtil.getInt(item, Keys.loreSize).orElse(0);
        int sizeReal = enchantDataMap.size();
        if (sizeCached == 0 && sizeReal == 0) return false;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        for (int index = 0; index < sizeCached && !lore.isEmpty(); index++) {
            lore.remove(0);
        }

        if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            if (canHaveDescription(item)) {
                for (var entry : enchantDataMap.entrySet()) {
                    List<String> description = entry.getKey().getDescriptionReplaced(entry.getValue());

                    lore.addAll(0, NightMessage.asLegacy(description));
                    sizeReal += description.size();
                }
            }
            enchantDataMap.forEach((enchant, level) -> {
                lore.add(0, NightMessage.asLegacy(enchant.getNameFormatted(level, enchant.getCharges(meta))));
            });
        }
        else sizeReal = 0;

        meta.setLore(lore);

        if (sizeReal > 0) {
            PDCUtil.set(meta, Keys.loreSize, sizeReal);
        }
        else PDCUtil.remove(meta, Keys.loreSize);

        item.setItemMeta(meta);*/
        return true;
    }

    @Nullable
    public static ItemStack getHandItem(@NotNull Player player, @NotNull Material material) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND}) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack != null && itemStack.getType() == material) {
                return itemStack;
            }
        }

        return null;
    }

    @Nullable
    public static EquipmentSlot getItemHand(@NotNull Player player, @NotNull Material material) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND}) {
            ItemStack itemStack = player.getInventory().getItem(slot);
            if (itemStack != null && itemStack.getType() == material) {
                return slot;
            }
        }

        return null;
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? Collections.emptyMap() : getEnchantments(meta);
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantments(@NotNull ItemMeta meta) {
        return (meta instanceof EnchantmentStorageMeta storageMeta) ? storageMeta.getStoredEnchants() : meta.getEnchants();
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull String id) {
        EnchantmentData enchant = EnchantRegistry.getById(id);
        if (enchant == null) return false;

        return contains(item, enchant.getEnchantment());
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && contains(meta, enchantment);
    }

    public static boolean contains(@NotNull ItemMeta meta, @NotNull Enchantment enchantment) {
        return (meta instanceof EnchantmentStorageMeta storageMeta) ? storageMeta.hasStoredEnchant(enchantment) : meta.hasEnchant(enchantment);
    }

    public static boolean hasMaximumEnchants(@NotNull ItemStack item) {
        return countCustomEnchantments(item) >= Config.CORE_ITEM_ENCHANT_LIMIT.get();
    }

    public static int getLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? 0 : getLevel(meta, enchant);
    }

    public static int getLevel(@NotNull ItemMeta meta, @NotNull Enchantment enchant) {
        return meta instanceof EnchantmentStorageMeta storageMeta ? storageMeta.getStoredEnchantLevel(enchant) : meta.getEnchantLevel(enchant);
    }

    public static int countEnchantments(@NotNull ItemStack item) {
        return getEnchantments(item).size();
    }

    public static int countCustomEnchantments(@NotNull ItemStack item) {
        return getCustomEnchantments(item).size();
    }

    @NotNull
    public static Map<EnchantmentData, Integer> getCustomEnchantments(@NotNull ItemStack item) {
        return toCustomEnchantments(getEnchantments(item));
    }

    @NotNull
    public static Map<EnchantmentData, Integer> getCustomEnchantments(@NotNull ItemMeta meta) {
        return toCustomEnchantments(getEnchantments(meta));
    }

    @NotNull
    private static Map<EnchantmentData, Integer> toCustomEnchantments(@NotNull Map<Enchantment, Integer> enchants) {
        Map<EnchantmentData, Integer> map = new HashMap<>();
        enchants.forEach((enchantment, level) -> {
            EnchantmentData excellent = EnchantRegistry.getByKey(enchantment.getKey());
            if (excellent != null) {
                map.put(excellent, level);
            }
        });
        return map;
    }

    @NotNull
    public static <T extends EnchantmentData> Map<T, Integer> getCustomEnchantments(@NotNull ItemStack item, @NotNull Class<T> clazz) {
        Map<T, Integer> map = new HashMap<>();
        getEnchantments(item).forEach((enchantment, level) -> {
            EnchantmentData enchantmentData = EnchantRegistry.getByKey(enchantment.getKey());
            if (enchantmentData == null || !clazz.isAssignableFrom(enchantmentData.getClass())) return;

            map.put(clazz.cast(enchantmentData), level);
        });
        return map;
    }

    /*@NotNull
    private static Map<EquipmentSlot, ItemStack> getEnchantedEquipment(@NotNull LivingEntity entity) {
        return getEnchantedEquipment(entity, EQUIPMENT_SLOTS);
    }*/

    @NotNull
    private static Map<EquipmentSlot, ItemStack> getEnchantedEquipment(@NotNull LivingEntity entity, @NotNull EquipmentSlot... slots) {
        Map<EquipmentSlot, ItemStack> equipment = EntityUtil.getEquippedItems(entity, slots);
        equipment.values().removeIf(item -> item == null || isEnchantedBook(item) || !item.hasItemMeta());

//        equipment.entrySet().removeIf(entry -> {
//            ItemStack item = entry.getValue();
//            EquipmentSlot slot = entry.getKey();
//            if (item == null || item.getType().isAir() || item.getType() == Material.ENCHANTED_BOOK) return true;
//            if ((slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) && ItemUtil.isArmor(item)) return true;
//            return !item.hasItemMeta();
//        });
        return equipment;
    }

    /*@NotNull
    private static Map<ItemStack, Map<EnchantmentData, Integer>> getEquipped(@NotNull LivingEntity entity) {
        Map<ItemStack, Map<EnchantmentData, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getCustomEnchantments(item));
        });
        return map;
    }

    @NotNull
    public static <T extends EnchantmentData> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity, @NotNull Class<T> clazz) {
        return getEquipped(entity, clazz, EQUIPMENT_SLOTS);
    }*/

    @NotNull
    public static <T extends EnchantmentData> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity,
                                                                                          @NotNull Class<T> clazz,
                                                                                          @NotNull EquipmentSlot... slots) {
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity, slots).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getCustomEnchantments(item, clazz));
        });
        return map;
    }

    @NotNull
    public static Map<ItemStack, Integer> getEquipped(@NotNull LivingEntity entity, @NotNull EnchantmentData data) {
        Map<ItemStack, Integer> map = new HashMap<>();
        getEnchantedEquipment(entity, data.getSupportedItems().getSlots()).values().forEach(item -> {
            int level = getLevel(item, data.getEnchantment());
            if (level > 0) {
                map.put(item, level);
            }
        });
        return map;
    }

    public static void addEnchantedProjectile(@NotNull Projectile projectile, @Nullable ItemStack item) {
        EnchantedProjectile enchantedProjectile = new EnchantedProjectile(projectile, item);
        ENCHANTED_PROJECTILE_MAP.put(projectile.getUniqueId(), enchantedProjectile);
    }

    @Nullable
    public static EnchantedProjectile getEnchantedProjectile(@NotNull Projectile projectile) {
        return ENCHANTED_PROJECTILE_MAP.get(projectile.getUniqueId());
    }

    public static void removeEnchantedProjectile(@NotNull Projectile projectile) {
        ENCHANTED_PROJECTILE_MAP.remove(projectile.getUniqueId());
    }

    @NotNull
    public static Collection<EnchantedProjectile> getEnchantedProjectiles() {
        return ENCHANTED_PROJECTILE_MAP.values();
    }

    public static void setSpawnReason(@NotNull Entity entity, @NotNull CreatureSpawnEvent.SpawnReason reason) {
        PDCUtil.set(entity, Keys.entitySpawnReason, reason.name());
    }

    @Nullable
    public static CreatureSpawnEvent.SpawnReason getSpawnReason(@NotNull Entity entity) {
        String name = PDCUtil.getString(entity, Keys.entitySpawnReason).orElse(null);
        return name == null ? null : StringUtil.getEnum(name, CreatureSpawnEvent.SpawnReason.class).orElse(null);
    }
}
