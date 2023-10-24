package su.nightexpress.excellentenchants.enchantment.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;

import java.util.*;
import java.util.stream.Stream;

public class EnchantUtils {

    public static final NamespacedKey KEY_LORE_SIZE = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "lore_size");
    private static final String META_PROJECTILE_WEAPON = "sourceWeapon";

    private static boolean busyBreak = false;

    public static void popResource(@NotNull BlockDropItemEvent event, @NotNull ItemStack itemStack) {
        Item item = ExcellentEnchantsAPI.PLUGIN.getEnchantNMS().popResource(event.getBlock(), itemStack);
        event.getItems().add(item);
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

    @NotNull
    public static NamespacedKey createKey(@NotNull String id) {
        return NamespacedKey.minecraft(id.toLowerCase());
    }

    @Nullable
    public static String getLocalized(@NotNull String keyRaw) {
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(keyRaw));
        return enchantment == null ? null : getLocalized(enchantment);
    }

    @NotNull
    public static String getLocalized(@NotNull Enchantment enchantment) {
        if (enchantment instanceof ExcellentEnchant excellentEnchant) {
            return excellentEnchant.getDisplayName();
        }
        return LangManager.getEnchantment(enchantment);
    }

    public static boolean isEnchantable(@NotNull ItemStack item) {
        if (item.getType().isAir()) return false;

        return item.getType() == Material.ENCHANTED_BOOK || Stream.of(EnchantmentTarget.values()).anyMatch(target -> target.includes(item));
    }

    public static boolean add(@NotNull ItemStack item, @NotNull Enchantment enchantment, int level, boolean force) {
        if (!force && !enchantment.canEnchantItem(item)) return false;

        remove(item, enchantment);

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
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchantment);
        }
        else {
            meta.removeEnchant(enchantment);
        }
        item.setItemMeta(meta);
    }

    public static void updateChargesDisplay(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
            updateDisplay(item);
        }
    }

    public static boolean updateDisplay(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_DISPLAY_MODE.get() != 1) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (!isEnchantable(item)) {
            PDCUtil.remove(item, KEY_LORE_SIZE);
            return false;
        }

        Map<ExcellentEnchant, Integer> enchants = getExcellents(item);

        int sizeCached = PDCUtil.getInt(item, KEY_LORE_SIZE).orElse(0);
        int sizeReal = enchants.size();
        if (sizeCached == 0 && sizeReal == 0) return false;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        for (int index = 0; index < sizeCached && !lore.isEmpty(); index++) {
            lore.remove(0);
        }
        //lore.removeIf(str -> enchants.keySet().stream().anyMatch(enchant -> str.contains(enchant.getDisplayName())));

        if (!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED.get()) {
                enchants.forEach((enchant, level) -> {
                    lore.addAll(0, enchant.formatDescription(level));
                });
                sizeReal += enchants.keySet().stream().map(ExcellentEnchant::getDescription).mapToInt(List::size).sum();
            }
            enchants.forEach((enchant, level) -> {
                lore.add(0, enchant.getNameFormatted(level, getCharges(meta, enchant)));
            });
        }
        else sizeReal = 0;

        meta.setLore(lore);
        if (sizeReal > 0) {
            PDCUtil.set(meta, KEY_LORE_SIZE, sizeReal);
        }
        else PDCUtil.remove(meta, KEY_LORE_SIZE);
        item.setItemMeta(meta);
        return true;
    }

    @Nullable
    public static ItemStack getFishingRod(@NotNull Player player) {
        ItemStack main = player.getInventory().getItem(EquipmentSlot.HAND);
        if (main != null && main.getType() == Material.FISHING_ROD) return main;

        ItemStack off = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
        if (off != null && off.getType() == Material.FISHING_ROD) return off;

        return null;
    }

    @NotNull
    public static Map<Enchantment, Integer> getAll(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta == null ? Collections.emptyMap() : getAll(meta);
    }

    @NotNull
    public static Map<Enchantment, Integer> getAll(@NotNull ItemMeta meta) {
        return (meta instanceof EnchantmentStorageMeta meta2) ? meta2.getStoredEnchants() : meta.getEnchants();
    }

    public static int getAmount(@NotNull ItemStack item) {
        return getAll(item).size();
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull String id) {
        ExcellentEnchant enchant = EnchantRegistry.getById(id);
        if (enchant == null) return false;

        return contains(item, enchant);
    }

    public static boolean contains(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        return getLevel(item, enchantment) > 0;
    }

    public static int getLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        return getAll(item).getOrDefault(enchant, 0);
    }

    public static int getCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        return enchant.isChargesEnabled() ? PDCUtil.getInt(item, enchant.getChargesKey()).orElse(0) : -1;
    }

    public static int getCharges(@NotNull ItemMeta meta, @NotNull ExcellentEnchant enchant) {
        return enchant.isChargesEnabled() ? PDCUtil.getInt(meta, enchant.getChargesKey()).orElse(0) : -1;
    }

    public static boolean isOutOfCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        return enchant.isChargesEnabled() && getCharges(item, enchant) == 0;
    }

    public static boolean isFullOfCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        if (!enchant.isChargesEnabled()) return true;

        int level = getLevel(item, enchant);
        int max = enchant.getChargesMax(level);
        return getCharges(item, enchant) == max;
    }

    public static void consumeCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int level) {
        int has = getCharges(item, enchant);
        int use = enchant.getChargesConsumeAmount(level);
        setCharges(item, enchant, level, has < use ? 0 : has - use);
    }

    /*public static void restoreCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        int level = getLevel(item, enchant);
        restoreCharges(item, enchant, level);
    }*/

    public static void restoreCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int level) {
        setCharges(item, enchant, level, Integer.MAX_VALUE);
    }

    public static void rechargeCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int level) {
        //if (!enchant.isChargesEnabled()) return;

        //int level = getLevel(item, enchant);
        int recharge = enchant.getChargesRechargeAmount(level);
        int has = getCharges(item, enchant);
        int set = has + recharge;
        setCharges(item, enchant, level, set);
    }

    /*public static void setCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int charges) {
        int level = getLevel(item, enchant);
        setCharges(item, enchant, level, charges);
    }*/

    public static void setCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int level, int charges) {
        if (!enchant.isChargesEnabled()) return;

        int max = enchant.getChargesMax(level);
        int set = Math.min(Math.abs(charges), max);
        PDCUtil.set(item, enchant.getChargesKey(), set);
    }

    public static int getExcellentAmount(@NotNull ItemStack item) {
        return getExcellents(item).size();
    }

    @NotNull
    public static Map<ExcellentEnchant, Integer> getExcellents(@NotNull ItemStack item) {
        return getExcellents(getAll(item));
    }

    @NotNull
    public static Map<ExcellentEnchant, Integer> getExcellents(@NotNull ItemMeta meta) {
        return getExcellents(getAll(meta));
    }

    @NotNull
    private static Map<ExcellentEnchant, Integer> getExcellents(@NotNull Map<Enchantment, Integer> enchants) {
        Map<ExcellentEnchant, Integer> map = new HashMap<>();
        enchants.forEach((enchantment, level) -> {
            ExcellentEnchant excellent = EnchantRegistry.getByKey(enchantment.getKey());
            if (excellent != null) {
                map.put(excellent, level);
            }
        });
        return map;
    }

    @NotNull
    public static <T extends IEnchantment> Map<T, Integer> getExcellents(@NotNull ItemStack item, @NotNull Class<T> clazz) {
        Map<T, Integer> map = new HashMap<>();
        getAll(item).forEach((enchantment, level) -> {
            ExcellentEnchant excellent = EnchantRegistry.getByKey(enchantment.getKey());
            if (excellent == null || !clazz.isAssignableFrom(excellent.getClass())) return;

            map.put(clazz.cast(excellent), level);
        });
        return map;//CollectionsUtil.sort(map, Comparator.comparing(p -> p.getKey().getPriority(), Comparator.reverseOrder()));
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEnchantedEquipment(@NotNull LivingEntity entity) {
        return getEnchantedEquipment(entity, EquipmentSlot.values());
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEnchantedEquipment(@NotNull LivingEntity entity, @NotNull EquipmentSlot... slots) {
        Map<EquipmentSlot, ItemStack> equipment = EntityUtil.getEquippedItems(entity, slots);
        equipment.entrySet().removeIf(entry -> {
            ItemStack item = entry.getValue();
            EquipmentSlot slot = entry.getKey();
            if (item == null || item.getType().isAir() || item.getType() == Material.ENCHANTED_BOOK) return true;
            if ((slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND) && ItemUtil.isArmor(item)) return true;
            return !item.hasItemMeta();
        });
        return equipment;
    }

    @NotNull
    public static Map<ItemStack, Map<ExcellentEnchant, Integer>> getEquipped(@NotNull LivingEntity entity) {
        Map<ItemStack, Map<ExcellentEnchant, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getExcellents(item));
        });
        return map;
    }

    @NotNull
    public static <T extends IEnchantment> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity,
                                                                                       @NotNull Class<T> clazz) {
        return getEquipped(entity, clazz, EquipmentSlot.values());
    }

    @NotNull
    public static <T extends IEnchantment> Map<ItemStack, Map<T, Integer>> getEquipped(@NotNull LivingEntity entity,
                                                                                       @NotNull Class<T> clazz,
                                                                                       @NotNull EquipmentSlot... slots) {
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity, slots).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getExcellents(item, clazz));
        });
        return map;
    }

    @NotNull
    public static Map<ItemStack, Integer> getEquipped(@NotNull LivingEntity entity, @NotNull ExcellentEnchant enchantment) {
        Map<ItemStack, Integer> map = new HashMap<>();
        getEnchantedEquipment(entity).values().forEach(item -> {
            int level = getLevel(item, enchantment);
            if (level > 0) {
                map.put(item, level);
            }
        });
        return map;
    }

    public static void setSourceWeapon(@NotNull Projectile projectile, @Nullable ItemStack item) {
        if (item == null) return;

        projectile.setMetadata(META_PROJECTILE_WEAPON, new FixedMetadataValue(ExcellentEnchantsAPI.PLUGIN, item));
    }

    @Nullable
    public static ItemStack getSourceWeapon(@NotNull Projectile projectile) {
        return projectile.hasMetadata(META_PROJECTILE_WEAPON) ? (ItemStack) projectile.getMetadata(META_PROJECTILE_WEAPON).get(0).value() : null;
    }

    public static void removeSourceWeapon(@NotNull Projectile projectile) {
        projectile.removeMetadata(META_PROJECTILE_WEAPON, ExcellentEnchantsAPI.PLUGIN);
    }
}
