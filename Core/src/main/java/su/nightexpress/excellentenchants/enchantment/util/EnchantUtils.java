package su.nightexpress.excellentenchants.enchantment.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.PDCUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.ObtainSettings;
import su.nightexpress.excellentenchants.enchantment.EnchantPopulator;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantUtils {

    public static final NamespacedKey KEY_LORE_SIZE = new NamespacedKey(ExcellentEnchantsAPI.PLUGIN, "lore_size");

    @NotNull
    public static NamespacedKey createKey(@NotNull String id) {
        return NamespacedKey.minecraft(id.toLowerCase());
    }

    @NotNull
    public static String getLocalized(@NotNull String keyRaw) {
        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(keyRaw));
        return enchantment == null ? "null" : getLocalized(enchantment);
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

    public static boolean populate(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        int enchantsHad = getAmount(item);

        getPopulationCandidates(item, obtainType).forEach((enchantment, level) -> {
            add(item, enchantment, level, false);
        });
        updateDisplay(item);

        return getAmount(item) != enchantsHad;
    }

    @NotNull
    public static Set<ExcellentEnchant> populateFilter(@NotNull Set<ExcellentEnchant> enchants,
                                                       @NotNull ObtainType obtainType, @Nullable ItemStack item) {
        Set<ExcellentEnchant> set = enchants.stream()
            .filter(enchant -> enchant.getObtainChance(obtainType) > 0)
            .filter(enchant -> item == null || enchant.canEnchantItem(item))
            .collect(Collectors.toCollection(HashSet::new));
        set.removeIf(enchant -> obtainType == ObtainType.ENCHANTING && (enchant.isTreasure() || enchant.isCursed()));
        return set;
    }

    @NotNull
    public static Map<Enchantment, Integer> getPopulationCandidates(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        return getPopulationCandidates(item, obtainType, new HashMap<>(), (enchant) -> enchant.generateLevel(obtainType));
    }

    @NotNull
    public static Map<Enchantment, Integer> getPopulationCandidates(@NotNull ItemStack item, @NotNull ObtainType obtainType,
                                                                    @NotNull Map<Enchantment, Integer> enchantsPrepared,
                                                                    @NotNull Function<ExcellentEnchant, Integer> levelFunc) {
        Map<Enchantment, Integer> enchantsToAdd = new HashMap<>(enchantsPrepared);

        ObtainSettings settings = Config.getObtainSettings(obtainType).orElse(null);
        if (settings == null || !Rnd.chance(settings.getEnchantsCustomGenerationChance())) return enchantsToAdd;

        int enchMax = settings.getEnchantsTotalMax();
        int enchRoll = Rnd.get(settings.getEnchantsCustomMin(), settings.getEnchantsCustomMax());

        // Класс для исключения неудачных попыток.
        EnchantPopulator populator = new EnchantPopulator(obtainType, item);

        // Добавляем сколько можем, пока нужное количество не будет добавлено или не закончатся чары и/или тиры.
        while (!populator.isEmpty() && enchRoll > 0) {
            // Достигнут максимум чар (любых) для итема, заканчиваем.
            if (enchantsToAdd.size() >= enchMax) break;

            Tier tier = populator.getTierByChance();
            if (tier == null) break; // Нет тира?

            ExcellentEnchant enchant = populator.getEnchantByChance(tier);
            // В тире нет подходящих чар (вообще) для итема, исключаем и идем дальше.
            if (enchant == null) {
                populator.purge(tier);
                continue;
            }

            // Среди уже добавленных чар есть конфликты с тем, что нашли.
            // Исключаем, идем дальше.
            if (enchantsToAdd.keySet().stream().anyMatch(has -> has.conflictsWith(enchant) || enchant.conflictsWith(has))) {
                populator.purge(tier, enchant);
                continue;
            }

            // Не получилось сгенерировать подходящий уровень.
            // Исключаем, идем дальше.
            int level = levelFunc.apply(enchant);
            if (level < enchant.getStartLevel()) {
                populator.purge(tier, enchant);
                continue;
            }

            // Добавляем чар, засчитываем попытку.
            populator.purge(tier, enchant);
            enchantsToAdd.put(enchant, level);
            enchRoll--;
        }
        return enchantsToAdd;
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

    public static boolean updateDisplay(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_DISPLAY_MODE.get() != 1) return false;

        if (!isEnchantable(item)) {
            PDCUtil.remove(item, KEY_LORE_SIZE);
            return false;
        }

        Map<ExcellentEnchant, Integer> enchants = getExcellents(item);

        int sizeHas = PDCUtil.getInt(item, KEY_LORE_SIZE).orElse(0);
        int sizeReal = enchants.size();

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        for (int index = 0; index < sizeHas && !lore.isEmpty(); index++) {
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

    public static void consumeCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        if (!enchant.isChargesEnabled()) return;

        int level = getLevel(item, enchant);
        int has = getCharges(item, enchant);
        int use = enchant.getChargesConsumeAmount(level);
        setCharges(item, enchant, has - use);
    }

    public static void restoreCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        if (!enchant.isChargesEnabled()) return;

        int level = getLevel(item, enchant);
        int max = enchant.getChargesMax(level);
        setCharges(item, enchant, max);
    }

    public static void rechargeCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant) {
        if (!enchant.isChargesEnabled()) return;

        int level = getLevel(item, enchant);
        int recharge = enchant.getChargesRechargeAmount(level);
        int has = getCharges(item, enchant);
        setCharges(item, enchant, has + recharge);
    }

    public static void setCharges(@NotNull ItemStack item, @NotNull ExcellentEnchant enchant, int charges) {
        if (!enchant.isChargesEnabled()) return;

        int level = getLevel(item, enchant);
        int max = enchant.getChargesMax(level);
        PDCUtil.set(item, enchant.getChargesKey(), Math.max(0, Math.min(charges, max)));
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
        return CollectionsUtil.sort(map, Comparator.comparing(p -> p.getKey().getPriority(), Comparator.reverseOrder()));
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEnchantedEquipment(@NotNull LivingEntity entity) {
        Map<EquipmentSlot, ItemStack> equipment = EntityUtil.getEquippedItems(entity);
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
        Map<ItemStack, Map<T, Integer>> map = new HashMap<>();
        getEnchantedEquipment(entity).values().forEach(item -> {
            map.computeIfAbsent(item, k -> new LinkedHashMap<>()).putAll(getExcellents(item, clazz));
        });
        return map;
    }

    public static void updateEquippedEffects(@NotNull LivingEntity entity) {
        getEquipped(entity, PassiveEnchant.class).forEach((item, enchants) -> {
            enchants.forEach((enchant, level) -> {
                if (enchant instanceof Potioned potioned) {
                    if (enchant.isOutOfCharges(item)) return;
                    if (enchant.onTrigger(entity, item, level)) {
                        enchant.consumeCharges(item);
                    }
                }
            });
        });
    }
}
