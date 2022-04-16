package su.nightexpress.excellentenchants.manager;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.ObtainSettings;
import su.nightexpress.excellentenchants.manager.listeners.EnchantGenericListener;
import su.nightexpress.excellentenchants.manager.listeners.EnchantHandlerListener;
import su.nightexpress.excellentenchants.manager.object.EnchantListGUI;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;
import su.nightexpress.excellentenchants.manager.tasks.ArrowTrailsTask;
import su.nightexpress.excellentenchants.manager.tasks.EnchantEffectPassiveTask;
import su.nightexpress.excellentenchants.manager.type.ObtainType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EnchantManager extends AbstractManager<ExcellentEnchants> {

    private EnchantListGUI      enchantListGUI;
    private ArrowTrailsTask          arrowTrailsTask;
    private EnchantEffectPassiveTask enchantEffectPassiveTask;

    private static final Map<UUID, ItemStack> PROJECTILE_WEAPON = new HashMap<>();

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

    public static void addPotionEffect(@NotNull LivingEntity entity, @NotNull PotionEffect effect, boolean compat) {
        if (compat) {
            PotionEffect has = entity.getPotionEffect(effect.getType());
            if (has != null && has.getAmplifier() > effect.getAmplifier()) {
                return;
            }
        }
        else {
            entity.removePotionEffect(effect.getType());
        }
        entity.addPotionEffect(effect);
    }

    public static boolean isEnchantable(@NotNull ItemStack item) {
        if (item.getType().isAir()) return false;

        return item.getType() == Material.ENCHANTED_BOOK || Stream.of(EnchantmentTarget.values()).anyMatch(target -> target.includes(item))
        /*|| ItemUtil.isWeapon(item) || ItemUtil.isArmor(item) || ItemUtil.isTool(item) || ItemUtil.isBow(item)*/;
    }

    public static boolean populateEnchantments(@NotNull ItemStack item, @NotNull ObtainType obtainType) {
        ObtainSettings settings = Config.getObtainSettings(obtainType);
        if (settings == null) return false;

        if (Rnd.get(true) > settings.getEnchantsCustomGenerationChance()) return false;

        int enchHas = EnchantManager.getItemEnchantsAmount(item);
        int enchMax = settings.getEnchantsTotalMax();
        int enchRoll = Rnd.get(settings.getEnchantsCustomMin(), settings.getEnchantsCustomMax());

        for (int count = 0; (count < enchRoll && count + enchHas < enchMax); count++) {
            EnchantTier tier = EnchantManager.getTierByChance(obtainType);
            if (tier == null) continue;

            ExcellentEnchant enchant = tier.getEnchant(obtainType, item);
            if (enchant == null) continue;

            int level = Rnd.get(enchant.getStartLevel(), enchant.getMaxLevel());
            EnchantManager.addEnchant(item, enchant, level, false);
        }
        EnchantManager.updateItemLoreEnchants(item);
        return true;
    }

    public static void updateItemLoreEnchants(@NotNull ItemStack item) {
        EnchantRegister.ENCHANT_LIST.forEach(ench -> {
            ItemUtil.delLore(item, ench.getId());
            ItemUtil.delLore(item, ench.getId() + "_info");
        });

        // Filter custom enchants and define map order.
        Map<ExcellentEnchant, Integer> excellents = getItemCustomEnchants(item).entrySet().stream()
                .sorted((e1,e2) -> e2.getKey().getTier().getPriority() - e1.getKey().getTier().getPriority())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (has, add) -> add, LinkedHashMap::new));

        excellents.forEach((excellent, level) -> {
            ItemUtil.addLore(item, excellent.getId(), excellent.getNameFormatted(level), 0);
        });

        // Add enchantment description at the end of item lore.
        if (Config.ENCHANTMENTS_DESCRIPTION_ENABLED) {
            List<ExcellentEnchant> list = new ArrayList<>(excellents.keySet());
            Collections.reverse(list);

            list.forEach(excellent -> {
                List<String> desc = excellent.getDescription(excellents.get(excellent));
                if (desc.isEmpty()) return;

                ItemUtil.addLore(item, excellent.getId() + "_info", Config.formatDescription(desc), -1);
            });
        }
    }

    public static boolean addEnchant(@NotNull ItemStack item, @NotNull ExcellentEnchant ench, int level, boolean force) {
        if (!force && !ench.canEnchantItem(item)) return false;

        EnchantManager.removeEnchant(item, ench);
        ItemUtil.addLore(item, ench.getId(), ench.getNameFormatted(level), 0);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        if (meta instanceof EnchantmentStorageMeta meta2) {
            if (!meta2.addStoredEnchant(ench, level, true)) return false;
        }
        else {
            if (!meta.addEnchant(ench, level, true)) return false;
        }
        item.setItemMeta(meta);

        return true;
    }

    public static void removeEnchant(@NotNull ItemStack item, @NotNull ExcellentEnchant en) {
        ItemUtil.delLore(item, en.getId());

        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta meta2) {
            meta2.removeStoredEnchant(en);
        }
        else {
            meta.removeEnchant(en);
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

    @NotNull
    public static Map<Enchantment, Integer> getItemEnchants(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return (meta instanceof EnchantmentStorageMeta meta2) ? meta2.getStoredEnchants() : meta.getEnchants();
    }

    public static int getItemEnchantsAmount(@NotNull ItemStack item) {
        return EnchantManager.getItemEnchants(item).size();
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

    public static void setArrowWeapon(@NotNull Projectile projectile, @NotNull ItemStack bow) {
        PROJECTILE_WEAPON.put(projectile.getUniqueId(), bow);
    }

    @Nullable
    public static ItemStack getArrowWeapon(@NotNull Projectile projectile) {
        return PROJECTILE_WEAPON.get(projectile.getUniqueId());
    }
}
