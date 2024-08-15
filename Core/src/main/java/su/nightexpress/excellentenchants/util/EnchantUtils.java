package su.nightexpress.excellentenchants.util;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Keys;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.text.NightMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.excellentenchants.Placeholders.GENERIC_CHARGES;
import static su.nightexpress.nightcore.util.Placeholders.GENERIC_VALUE;

public class EnchantUtils {

    private static final Set<UUID>                      IGNORE_DISPLAY_UPDATE    = new HashSet<>();
    private static final Map<UUID, EnchantedProjectile> ENCHANTED_PROJECTILE_MAP = new ConcurrentHashMap<>();

    public static final int LEVEL_CAP = 255;
    public static final int WEIGHT_CAP = 1024;

    private static boolean busyBreak;

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

    public static boolean canUpdateDisplay(@NotNull Player player) {
        return !IGNORE_DISPLAY_UPDATE.contains(player.getUniqueId()) && player.getGameMode() != GameMode.CREATIVE;
    }

    public static void runInDisabledDisplayUpdate(@NotNull Player player, @NotNull Runnable runnable) {
        stopDisplayUpdate(player);
        runnable.run();
        allowDisplayUpdate(player);
    }

    public static void stopDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.add(player.getUniqueId());
    }

    public static void allowDisplayUpdate(@NotNull Player player) {
        IGNORE_DISPLAY_UPDATE.remove(player.getUniqueId());
    }

    @Nullable
    public static String getLocalized(@NotNull String keyRaw) {
        Enchantment enchantment = BukkitThing.getEnchantment(keyRaw);
        return enchantment == null ? null : getLocalized(enchantment);
    }

    @NotNull
    public static String getLocalized(@NotNull Enchantment enchantment) {
        CustomEnchantment enchant = EnchantRegistry.getByKey(enchantment.getKey());
        if (enchant != null) {
            return enchant.getDisplayName();
        }
        return LangAssets.get(enchantment);
    }

    @NotNull
    public static String replaceComponents(@NotNull GameEnchantment enchantment, @NotNull String string, int level, int charges) {
        String chargesFormat = "";
        boolean showLevel = enchantment.getDefinition().getMaxLevel() > 1;
        boolean showCharges = enchantment.hasCharges() && charges >= 0;

        if (showCharges) {
            int chargesMax = enchantment.getCharges().getMaxAmount(level);
            int percent = (int) Math.ceil((double) charges / (double) chargesMax * 100D);
            Map.Entry<Integer, String> entry = Config.ENCHANTMENTS_CHARGES_FORMAT.get().floorEntry(percent);
            if (entry != null) {
                chargesFormat = entry.getValue().replace(GENERIC_AMOUNT, String.valueOf(charges));
            }
        }

        String compName = Config.ENCHANTMENTS_DISPLAY_NAME_COMPONENT.get().replace(GENERIC_VALUE, enchantment.getDisplayName());
        String compLevel = showLevel ? Config.ENCHANTMENTS_DISPLAY_LEVEL_COMPONENT.get().replace(GENERIC_VALUE, NumberUtil.toRoman(level)) : "";
        String compChrages = showCharges ? Config.ENCHANTMENTS_DISPLAY_CHARGES_COMPONENT.get().replace(GENERIC_VALUE, chargesFormat) : "";

        return string
            .replace(GENERIC_NAME, compName)
            .replace(GENERIC_LEVEL, compLevel)
            .replace(GENERIC_CHARGES, compChrages);
    }

    @Nullable
    public static ItemStack addDescription(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir() || !canHaveDescription(item)) return item;

        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null || meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) return item;

        Map<CustomEnchantment, Integer> enchants = getCustomEnchantments(meta);
        if (enchants.isEmpty()) return item;

        List<String> metaLore = meta.getLore();
        List<String> lore = metaLore == null ? new ArrayList<>() : metaLore;

        enchants.forEach((enchant, level) -> {
            int chargesAmount = enchant.getCharges(meta);
            lore.addAll(NightMessage.asLegacy(enchant.getDescription(level, chargesAmount)));
        });

        meta.setLore(lore);
        copy.setItemMeta(meta);
        return copy;
    }

    public static boolean isEnchantedBook(@NotNull ItemStack item) {
        return item.getType() == Material.ENCHANTED_BOOK;
    }

    public static int randomLevel(@NotNull Enchantment enchantment) {
        return Rnd.get(1, enchantment.getMaxLevel());
    }

    public static boolean add(@NotNull ItemStack item, @NotNull Enchantment enchantment, int level, boolean force) {
        if (!force && (!enchantment.canEnchantItem(item) && !isEnchantedBook(item))) return false;

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

    public static void removeAll(@NotNull ItemStack item) {
        ItemUtil.editMeta(item, meta -> {
            getEnchantments(meta).keySet().forEach(enchantment -> remove(meta, enchantment));
        });
    }

    public static boolean canHaveDescription(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_DISPLAY_DESCRIPTION_BOOKS_ONLY.get()) {
            return isEnchantedBook(item);
        }
        return true;
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
        CustomEnchantment enchant = EnchantRegistry.getById(id);
        if (enchant == null) return false;

        return contains(item, enchant.getBukkitEnchantment());
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && contains(meta, enchantment);
    }

    public static boolean contains(@NotNull ItemMeta meta, @NotNull Enchantment enchantment) {
        return (meta instanceof EnchantmentStorageMeta storageMeta) ? storageMeta.hasStoredEnchant(enchantment) : meta.hasEnchant(enchantment);
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
    public static Map<CustomEnchantment, Integer> getCustomEnchantments(@NotNull ItemStack item) {
        return toCustomEnchantments(getEnchantments(item));
    }

    @NotNull
    public static Map<CustomEnchantment, Integer> getCustomEnchantments(@NotNull ItemMeta meta) {
        return toCustomEnchantments(getEnchantments(meta));
    }

    @NotNull
    private static Map<CustomEnchantment, Integer> toCustomEnchantments(@NotNull Map<Enchantment, Integer> enchants) {
        Map<CustomEnchantment, Integer> map = new LinkedHashMap<>();
        enchants.forEach((enchantment, level) -> {
            CustomEnchantment excellent = EnchantRegistry.getByKey(enchantment.getKey());
            if (excellent != null) {
                map.put(excellent, level);
            }
        });
        return map;
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<T, Integer> getCustomEnchantments(@NotNull ItemStack item, @NotNull Class<T> clazz) {
        Map<T, Integer> map = new HashMap<>();
        getEnchantments(item).forEach((enchantment, level) -> {
            CustomEnchantment enchantmentData = EnchantRegistry.getByKey(enchantment.getKey());
            if (enchantmentData == null || !clazz.isAssignableFrom(enchantmentData.getClass())) return;

            map.put(clazz.cast(enchantmentData), level);
        });
        return map;
    }

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

    @Nullable
    public static ItemStack getEquipped(@NotNull LivingEntity entity, @NotNull EquipmentSlot slot) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return null;

        return equipment.getItem(slot);
    }

    public static int getEquippedLevel(@NotNull LivingEntity entity, @NotNull Enchantment enchantment, @NotNull EquipmentSlot slot) {
        ItemStack itemStack = getEquipped(entity, slot);
        return itemStack == null ? 0 : getLevel(itemStack, enchantment);
    }

    @NotNull
    public static <T extends CustomEnchantment> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity,
                                                                                            @NotNull Class<T> clazz,
                                                                                            @NotNull EquipmentSlot... slots) {
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity, slots).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getCustomEnchantments(item, clazz));
        });
        return map;
    }

    @NotNull
    public static Map<ItemStack, Integer> getEquipped(@NotNull LivingEntity entity, @NotNull CustomEnchantment enchantment) {
        Map<ItemStack, Integer> map = new HashMap<>();
        getEnchantedEquipment(entity, enchantment.getDefinition().getSupportedItems().getSlots()).values().forEach(item -> {
            int level = getLevel(item, enchantment.getBukkitEnchantment());
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
