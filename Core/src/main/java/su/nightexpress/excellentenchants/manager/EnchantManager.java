package su.nightexpress.excellentenchants.manager;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantPotionTemplate;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.ObtainSettings;
import su.nightexpress.excellentenchants.manager.listeners.EnchantGenericListener;
import su.nightexpress.excellentenchants.manager.listeners.EnchantHandlerListener;
import su.nightexpress.excellentenchants.manager.object.EnchantListGUI;
import su.nightexpress.excellentenchants.manager.object.EnchantPopulator;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;
import su.nightexpress.excellentenchants.manager.tasks.ArrowTrailsTask;
import su.nightexpress.excellentenchants.manager.tasks.EnchantEffectPassiveTask;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager extends AbstractManager<ExcellentEnchants> {

    private EnchantListGUI      enchantListGUI;
    private ArrowTrailsTask          arrowTrailsTask;
    private EnchantEffectPassiveTask enchantEffectPassiveTask;

    public EnchantManager(@NotNull ExcellentEnchants plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        EnchantRegister.setup();

        this.enchantListGUI = new EnchantListGUI(this.plugin);
        this.addListener(new EnchantHandlerListener(this));
        this.addListener(new EnchantGenericListener(this));

        this.arrowTrailsTask = new ArrowTrailsTask(this.plugin);
        this.arrowTrailsTask.start();

        this.enchantEffectPassiveTask = new EnchantEffectPassiveTask(this.plugin);
        this.enchantEffectPassiveTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.enchantListGUI != null) {
            this.enchantListGUI.clear();
            this.enchantListGUI = null;
        }
        if (this.arrowTrailsTask != null) {
            this.arrowTrailsTask.stop();
            this.arrowTrailsTask = null;
        }
        if (this.enchantEffectPassiveTask != null) {
            this.enchantEffectPassiveTask.stop();
            this.enchantEffectPassiveTask = null;
        }
        EnchantRegister.shutdown();
    }

    @NotNull
    public EnchantListGUI getEnchantsListGUI() {
        return enchantListGUI;
    }

    public static boolean isEnchantable(@NotNull ItemStack item) {
        if (item.getType().isAir()) return false;

        return item.getType() == Material.ENCHANTED_BOOK || Stream.of(EnchantmentTarget.values()).anyMatch(target -> target.includes(item))
        /*|| ItemUtil.isWeapon(item) || ItemUtil.isArmor(item) || ItemUtil.isTool(item) || ItemUtil.isBow(item)*/;
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantsToPopulate(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        return getEnchantsToPopulate(item, obtainType, new HashMap<>(), (enchant) -> enchant.generateLevel(obtainType));
    }

    @NotNull
    public static Map<Enchantment, Integer> getEnchantsToPopulate(@NotNull ItemStack item, @NotNull ObtainType obtainType,
                                                                  @NotNull Map<Enchantment, Integer> enchantsPrepared,
                                                                  @NotNull Function<ExcellentEnchant, Integer> levelFunc) {
        Map<Enchantment, Integer> enchantsToAdd = new HashMap<>(enchantsPrepared);

        ObtainSettings settings = Config.getObtainSettings(obtainType);
        if (settings == null || !Rnd.chance(settings.getEnchantsCustomGenerationChance())) return enchantsToAdd;

        int enchMax = settings.getEnchantsTotalMax();
        int enchRoll = Rnd.get(settings.getEnchantsCustomMin(), settings.getEnchantsCustomMax());

        // Класс для исключения неудачных попыток.
        EnchantPopulator populator = new EnchantPopulator(obtainType, item);

        // Херачим до талого, пока нужное количество не будет добавлено
        // или не закончатся чары и/или тиры.
        while (!populator.isEmpty() && enchRoll > 0) {
            // Достигнут максимум чар (любых) для итема, заканчиваем.
            if (enchantsToAdd.size() >= enchMax) break;

            EnchantTier tier = populator.getTierByChance();
            if (tier == null) break; // Нет тира?

            ExcellentEnchant enchant = populator.getEnchantByChance(tier);
            // В тире нет подходящих чар (вообще) для итема, исключаем и идем дальше.
            if (enchant == null) {
                populator.getEnchants().remove(tier);
                continue;
            }

            // Среди уже добавленных чар есть конфликты с тем, что нашли.
            // Исключаем, идем дальше.
            if (enchantsToAdd.keySet().stream().anyMatch(has -> has.conflictsWith(enchant) || enchant.conflictsWith(has))) {
                populator.getEnchants(tier).remove(enchant);
                continue;
            }

            // Не получилось сгенерировать подходящий уровень.
            // Исключаем, идем дальше.
            int level = levelFunc.apply(enchant);
            if (level < enchant.getStartLevel()) {
                populator.getEnchants(tier).remove(enchant);
                continue;
            }

            // Добавляем чар, засчитываем попытку.
            populator.getEnchants(tier).remove(enchant);
            enchantsToAdd.put(enchant, level);
            enchRoll--;
        }
        return enchantsToAdd;
    }

    public static boolean populateEnchantments(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        int enchantsHad = EnchantManager.getItemCustomEnchantsAmount(item);

        EnchantManager.getEnchantsToPopulate(item, obtainType).forEach((enchantment, level) -> {
            EnchantManager.addEnchant(item, enchantment, level, false);
        });

        return EnchantManager.getItemCustomEnchantsAmount(item) != enchantsHad;
    }

    @Deprecated
    public static void updateItemLoreEnchants(@NotNull ItemStack item) {
        EnchantRegister.ENCHANT_LIST.forEach(ench -> {
            //ItemUtil.delLore(item, ench.getId());
            //ItemUtil.delLore(item, ench.getId() + "_info");
        });

        // Filter custom enchants and define map order.
        Map<ExcellentEnchant, Integer> excellents = getItemCustomEnchants(item).entrySet().stream()
                .sorted((e1,e2) -> e2.getKey().getTier().getPriority() - e1.getKey().getTier().getPriority())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (has, add) -> add, LinkedHashMap::new));

        excellents.forEach((excellent, level) -> {
            //ItemUtil.addLore(item, excellent.getId(), excellent.getNameFormatted(level), 0);
        });

        // Add enchantment description at the end of item lore.
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED) {
            List<ExcellentEnchant> list = new ArrayList<>(excellents.keySet());
            Collections.reverse(list);

            list.forEach(excellent -> {
                List<String> desc = excellent.getDescription(excellents.get(excellent));
                if (desc.isEmpty()) return;

                //ItemUtil.addLore(item, excellent.getId() + "_info", Config.formatDescription(desc), -1);
            });
        }
    }

    public static boolean addEnchant(@NotNull ItemStack item, @NotNull Enchantment enchantment, int level, boolean force) {
        if (!force && !enchantment.canEnchantItem(item)) return false;

        EnchantManager.removeEnchant(item, enchantment);

        //if (enchantment instanceof ExcellentEnchant excellentEnchant) {
            //ItemUtil.addLore(item, excellentEnchant.getId(), excellentEnchant.getNameFormatted(level), 0);
        //}

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

    public static void removeEnchant(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        //if (enchantment instanceof ExcellentEnchant excellentEnchant) {
            //ItemUtil.delLore(item, excellentEnchant.getId());
        //}

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            storageMeta.removeStoredEnchant(enchantment);
        }
        else {
            meta.removeEnchant(enchantment);
        }
        item.setItemMeta(meta);
    }

    @NotNull
    public static Map<ExcellentEnchant, Integer> getItemCustomEnchants(@NotNull ItemStack item) {
        return EnchantManager.getItemEnchants(item).entrySet().stream()
                .filter(entry -> entry.getKey() instanceof ExcellentEnchant)
                .map(entry -> new AbstractMap.SimpleEntry<>((ExcellentEnchant) entry.getKey(), entry.getValue()))
                .sorted((e1,e2) -> e2.getKey().getPriority().ordinal() - e1.getKey().getPriority().ordinal())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static <T> Map<T, Integer> getItemCustomEnchants(@NotNull ItemStack item, @NotNull Class<T> clazz) {
        return EnchantManager.getItemCustomEnchants(item).entrySet().stream()
                .filter(entry -> clazz.isAssignableFrom(entry.getKey().getClass()))
                .sorted((e1,e2) -> e2.getKey().getPriority().ordinal() - e1.getKey().getPriority().ordinal())
                .collect(Collectors.toMap(k -> (T) k.getKey(), Map.Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));
    }

    public static int getItemCustomEnchantsAmount(@NotNull ItemStack item) {
        return EnchantManager.getItemCustomEnchants(item).size();
    }

    @Deprecated
    public static int getItemEnchantLevel(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        return getItemEnchants(item).getOrDefault(enchantment, 0);
    }

    @NotNull
    public static Map<Enchantment, Integer> getItemEnchants(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Collections.emptyMap();

        return (meta instanceof EnchantmentStorageMeta meta2) ? meta2.getStoredEnchants() : meta.getEnchants();
    }

    public static int getItemEnchantsAmount(@NotNull ItemStack item) {
        return EnchantManager.getItemEnchants(item).size();
    }

    public static boolean hasEnchantment(@NotNull ItemStack item, @NotNull Enchantment enchantment) {
        return getEnchantmentLevel(item, enchantment) > 0;
    }

    public static int getEnchantmentLevel(@NotNull ItemStack item, @NotNull Enchantment enchant) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        return meta.getEnchantLevel(enchant);
    }

    @Nullable
    public static ExcellentEnchant getEnchantmentByEffect(@NotNull LivingEntity entity, @NotNull PotionEffect effect) {
        Enchantment enchantment = ExcellentEnchantsAPI.PLUGIN.getEnchantNMS().getEnchantmentByEffect(entity, effect);
        if (enchantment instanceof ExcellentEnchant enchant) return enchant;

        return null;
    }

    public static boolean isEnchantmentEffect(@NotNull LivingEntity entity, @NotNull PotionEffect effect) {
        return getEnchantmentByEffect(entity, effect) != null;
    }

    public static boolean hasEnchantmentEffect(@NotNull LivingEntity entity, @NotNull ExcellentEnchant enchant) {
        return entity.getActivePotionEffects().stream().anyMatch(effect -> enchant.equals(getEnchantmentByEffect(entity, effect)));
    }

    @NotNull
    public static Map<ExcellentEnchant, Integer> getEquippedEnchantsMax(@NotNull LivingEntity entity) {
        return getEquippedEnchants(entity, Math::max);
    }

    @NotNull
    public static Map<ExcellentEnchant, Integer> getEquippedEnchantsTotal(@NotNull LivingEntity entity) {
        return getEquippedEnchants(entity, Integer::sum);
    }

    @NotNull
    private static Map<ExcellentEnchant, Integer> getEquippedEnchants(@NotNull LivingEntity entity, @NotNull BiFunction<Integer, Integer, Integer> remap) {
        Map<ExcellentEnchant, Integer> map = new HashMap<>();

        Map<EquipmentSlot, ItemStack> equipment = EntityUtil.getEquippedItems(entity);
        equipment.entrySet().stream().filter(entry -> {
            if (entry.getValue() == null) return false;
            if (entry.getValue().getType() == Material.ENCHANTED_BOOK) return false;
            if ((entry.getKey() == EquipmentSlot.HAND || entry.getKey() == EquipmentSlot.OFF_HAND) && ItemUtil.isArmor(entry.getValue())) return false;
            return true;
        }).map(Map.Entry::getValue).map(EnchantManager::getItemCustomEnchants).forEach(itemEnchants -> {
            itemEnchants.forEach((enchant, level) -> map.merge(enchant, level, remap));
        });
        return map;
    }

    public static int getEquippedEnchantLevelMax(@NotNull LivingEntity entity, @NotNull ExcellentEnchant enchant) {
        return getEquippedEnchantsMax(entity).getOrDefault(enchant, 0);
    }

    public static int getEquippedEnchantLevelTotal(@NotNull LivingEntity entity, @NotNull ExcellentEnchant enchant) {
        return getEquippedEnchantsTotal(entity).getOrDefault(enchant, 0);
    }

    public static void updateEquippedEnchantEffects(@NotNull LivingEntity entity) {
        getEquippedEnchantsMax(entity).forEach((enchant, level) -> {
            if (enchant instanceof PassiveEnchant passiveEnchant && enchant instanceof IEnchantPotionTemplate) {
                passiveEnchant.use(entity, level);
            }
        });
    }

    @Nullable
    public static EnchantTier getTierById(@NotNull String id) {
        return Config.getTierById(id);
    }

    @NotNull
    public static Collection<EnchantTier> getTiers() {
        return Config.getTiers();
    }

    @NotNull
    public static List<String> getTierIds() {
        return Config.getTierIds();
    }

    @Nullable
    public static EnchantTier getTierByChance(@NotNull ObtainType obtainType) {
        return Config.getTierByChance(obtainType);
    }
}
