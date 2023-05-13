package su.nightexpress.excellentenchants.enchantment.impl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.IListener;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.IEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.config.EnchantDefaults;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class ExcellentEnchant extends Enchantment implements IEnchantment, IListener {

    protected final ExcellentEnchants plugin;
    protected final JYML              cfg;
    protected final String            id;
    protected final EnchantPriority priority;
    protected final EnchantDefaults defaults;
    protected final NamespacedKey   chargesKey;
    protected final Map<Integer, PlaceholderMap> placeholdersMap;

    public ExcellentEnchant(@NotNull ExcellentEnchants plugin, @NotNull String id, @NotNull EnchantPriority priority) {
        super(NamespacedKey.minecraft(id.toLowerCase()));
        this.plugin = plugin;
        this.id = this.getKey().getKey();
        this.cfg = new JYML(plugin.getDataFolder() + EnchantManager.DIR_ENCHANTS, id + ".yml");
        this.priority = priority;
        this.chargesKey = new NamespacedKey(plugin, this.getId() + ".charges");
        this.defaults = new EnchantDefaults(this);
        this.placeholdersMap = new HashMap<>();
    }

    public void loadSettings() {
        this.cfg.reload();
        this.placeholdersMap.clear();

        this.getDefaults().load(this);

        for (int i = this.getStartLevel(); i < this.getMaxLevel() + 1; i++) {
            int level = i;

            PlaceholderMap map = new PlaceholderMap()
                .add(Placeholders.ENCHANTMENT_DESCRIPTION, () -> String.join("\n", this.getDescription()))
                .add(Placeholders.ENCHANTMENT_NAME, this::getDisplayName)
                .add(Placeholders.ENCHANTMENT_NAME_FORMATTED, () -> this.getNameFormatted(level))
                .add(Placeholders.ENCHANTMENT_LEVEL, () -> NumberUtil.toRoman(level))
                .add(Placeholders.ENCHANTMENT_LEVEL_MIN, () -> String.valueOf(this.getStartLevel()))
                .add(Placeholders.ENCHANTMENT_LEVEL_MAX, () -> String.valueOf(this.getMaxLevel()))
                .add(Placeholders.ENCHANTMENT_TIER, () -> this.getTier().getName())
                .add(Placeholders.ENCHANTMENT_FIT_ITEM_TYPES, () -> String.join(", ", Stream.of(this.getFitItemTypes()).map(type -> plugin.getLangManager().getEnum(type)).toList()))
                .add(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_ENCHANTING, () -> NumberUtil.format(this.getObtainChance(ObtainType.ENCHANTING)))
                .add(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_VILLAGER, () -> NumberUtil.format(this.getObtainChance(ObtainType.VILLAGER)))
                .add(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_LOOT_GENERATION, () -> NumberUtil.format(this.getObtainChance(ObtainType.LOOT_GENERATION)))
                .add(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_FISHING, () -> NumberUtil.format(this.getObtainChance(ObtainType.FISHING)))
                .add(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_MOB_SPAWNING, () -> NumberUtil.format(this.getObtainChance(ObtainType.MOB_SPAWNING)))
                .add(Placeholders.ENCHANTMENT_CHARGES_MAX_AMOUNT, () -> NumberUtil.format(this.getChargesMax(level)))
                .add(Placeholders.ENCHANTMENT_CHARGES_CONSUME_AMOUNT, () -> NumberUtil.format(this.getChargesConsumeAmount(level)))
                .add(Placeholders.ENCHANTMENT_CHARGES_RECHARGE_AMOUNT, () -> NumberUtil.format(this.getChargesRechargeAmount(level)))
                .add(Placeholders.ENCHANTMENT_CHARGES_FUEL_ITEM, () -> ItemUtil.getItemName(this.getChargesFuel()));

            if (this instanceof Chanced chanced) {
                map.add(Placeholders.ENCHANTMENT_CHANCE, () -> NumberUtil.format(chanced.getTriggerChance(level)));
            }
            if (this instanceof Potioned potioned) {
                map.add(Placeholders.ENCHANTMENT_POTION_LEVEL, () -> NumberUtil.toRoman(potioned.getEffectAmplifier(level)));
                map.add(Placeholders.ENCHANTMENT_POTION_DURATION, () -> NumberUtil.format(potioned.getEffectDuration(level) / 20D));
                map.add(Placeholders.ENCHANTMENT_POTION_TYPE, () -> LangManager.getPotionType(potioned.getEffectType()));
            }

            this.placeholdersMap.put(level, map);
        }
    }

    @NotNull
    public PlaceholderMap getPlaceholders(int level) {
        return this.placeholdersMap.get(level);
    }

    public void addPlaceholder(@NotNull String key, @NotNull Function<Integer, String> replacer) {
        for (int level = this.getStartLevel(); level < this.getMaxLevel() + 1; level++) {
            this.getPlaceholders(level).add(key, replacer.apply(level));
        }
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, plugin);
    }

    @NotNull
    public FitItemType[] getFitItemTypes() {
        FitItemType itemType = FitItemType.getByEnchantmentTarget(this.getItemTarget());
        return itemType == null ? new FitItemType[0] : new FitItemType[]{itemType};
    }

    public boolean isDisabledInWorld(@NotNull World world) {
        Set<String> disabled = Config.ENCHANTMENTS_DISABLED_IN_WORLDS.get().getOrDefault(world.getName(), Collections.emptySet());
        return disabled.contains(this.getKey().getKey()) || disabled.contains(Placeholders.WILDCARD);
    }

    public boolean isAvailableToUse(@NotNull LivingEntity entity) {
        return !this.isDisabledInWorld(entity.getWorld());
    }

    @NotNull
    public JYML getConfig() {
        return this.cfg;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public EnchantPriority getPriority() {
        return priority;
    }

    @NotNull
    public EnchantDefaults getDefaults() {
        return defaults;
    }

    @NotNull
    @Override
    public String getName() {
        return getId().toUpperCase();
    }

    @NotNull
    public String getDisplayName() {
        return this.getDefaults().getDisplayName();
    }

    @NotNull
    public String getNameFormatted(int level) {
        return this.getTier().getColor() + this.getDisplayName() + " " + NumberUtil.toRoman(level);
    }

    @NotNull
    public String getNameFormatted(int level, int charges) {
        if (!this.isChargesEnabled() || charges < 0) return this.getNameFormatted(level);

        int chargesMax = this.getChargesMax(level);
        int percent = (int) Math.ceil((double) charges / (double) chargesMax * 100D);
        Map.Entry<Integer, String> entry = Config.ENCHANTMENTS_CHARGES_FORMAT.get().floorEntry(percent);
        if (entry == null) return this.getNameFormatted(level);

        String format = entry.getValue().replace(Placeholders.GENERIC_AMOUNT, String.valueOf(charges));
        return this.getNameFormatted(level) + " " + format;
    }

    @NotNull
    public List<String> getDescription() {
        return this.getDefaults().getDescription();
    }

    @NotNull
    public List<String> getDescription(int level) {
        List<String> description = new ArrayList<>(this.getDescription());
        description.replaceAll(this.getPlaceholders(level).replacer());
        return description;
    }

    @NotNull
    public List<String> formatDescription(int level) {
        return new ArrayList<>(this.getDescription(level).stream()
            .map(line -> Config.ENCHANTMENTS_DESCRIPTION_FORMAT.get().replace(Placeholders.GENERIC_DESCRIPTION, line))
            .toList());
    }

    @NotNull
    public Set<String> getConflicts() {
        return this.getDefaults().getConflicts();
    }

    @NotNull
    public Tier getTier() {
        return this.getDefaults().getTier();
    }

    @Override
    public int getMaxLevel() {
        return this.getDefaults().getLevelMax();
    }

    @Override
    public int getStartLevel() {
        return this.getDefaults().getLevelMin();
    }

    public int getLevelByEnchantCost(int expLevel) {
        int get = this.getDefaults().getLevelByEnchantCost().getValues().entrySet().stream()
            .filter(en -> expLevel >= en.getValue().intValue()).max(Comparator.comparingInt(Map.Entry::getKey))
            .map(Map.Entry::getKey).orElse(0);

        return get != 0 ? this.fineLevel(get, ObtainType.ENCHANTING) : 0;
    }

    public double getObtainChance(@NotNull ObtainType obtainType) {
        return this.getDefaults().getObtainChance().getOrDefault(obtainType, 0D);
    }

    public int getObtainLevelMin(@NotNull ObtainType obtainType) {
        return this.getDefaults().getObtainLevelCap().getOrDefault(obtainType, new int[]{-1, -1})[0];
    }

    public int getObtainLevelMax(@NotNull ObtainType obtainType) {
        return this.getDefaults().getObtainLevelCap().getOrDefault(obtainType, new int[]{-1, -1})[1];
    }

    public int fineLevel(int level, @NotNull ObtainType obtainType) {
        int levelCapMin = this.getObtainLevelMin(obtainType);
        int levelCapMax = this.getObtainLevelMax(obtainType);

        if (levelCapMin > 0 && level < levelCapMin) level = levelCapMin;
        if (levelCapMax > 0 && level > levelCapMax) level = levelCapMax;

        return level;
    }

    public int generateLevel() {
        return Rnd.get(this.getStartLevel(), this.getMaxLevel());
    }

    public int generateLevel(@NotNull ObtainType obtainType) {
        int levelCapMin = this.getObtainLevelMin(obtainType);
        int levelCapMax = this.getObtainLevelMax(obtainType);

        if (levelCapMin <= 0 || levelCapMin < this.getStartLevel()) levelCapMin = this.getStartLevel();
        if (levelCapMax <= 0 || levelCapMax > this.getMaxLevel()) levelCapMax = this.getMaxLevel();

        return Rnd.get(levelCapMin, levelCapMax);
    }

    public int getAnvilMergeCost(int level) {
        return (int) this.getDefaults().getAnvilMergeCost().getValue(level);
    }

    @Override
    public final boolean conflictsWith(@NotNull Enchantment enchantment) {
        return this.getConflicts().contains(enchantment.getKey().getKey());
    }

    @Override
    public final boolean canEnchantItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (EnchantUtils.getAll(item).keySet().stream().anyMatch(e -> e.conflictsWith(this) || this.conflictsWith(e))) return false;
        if (EnchantUtils.getLevel(item, this) <= 0 && EnchantUtils.getExcellentAmount(item) >= Config.ENCHANTMENTS_ITEM_CUSTOM_MAX.get()) {
            return false;
        }
        if (item.getType() == Material.BOOK || item.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }
        return Stream.of(this.getFitItemTypes()).anyMatch(fitItemType -> fitItemType.isIncluded(item));
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public final boolean isTreasure() {
        return this.getDefaults().isTreasure();
    }

    public boolean hasVisualEffects() {
        return this.getDefaults().isVisualEffects();
    }

    public boolean isChargesEnabled() {
        return Config.ENCHANTMENTS_CHARGES_ENABLED.get() && this.getDefaults().isChargesEnabled();
    }

    public boolean isChargesCustomFuel() {
        return this.getDefaults().isChargesCustomFuel();
    }

    public int getChargesMax(int level) {
        return this.isChargesEnabled() ? (int) this.getDefaults().getChargesMax().getValue(level) : 0;
    }

    public int getChargesConsumeAmount(int level) {
        return this.isChargesEnabled() ? (int) this.getDefaults().getChargesConsumeAmount().getValue(level) : 0;
    }

    public int getChargesRechargeAmount(int level) {
        return this.isChargesEnabled() ? (int) this.getDefaults().getChargesRechargeAmount().getValue(level) : 0;
    }

    @NotNull
    public ItemStack getChargesFuel() {
        ItemStack fuelHas = this.getDefaults().getChargesFuel();
        if (!this.isChargesCustomFuel() || fuelHas == null || fuelHas.getType().isAir()) {
            return Config.ENCHANTMENTS_CHARGES_FUEL_ITEM.get();
        }
        return new ItemStack(fuelHas);
    }

    public boolean isChargesFuel(@NotNull ItemStack item) {
        return item.isSimilar(this.getChargesFuel());
    }

    @NotNull
    public NamespacedKey getChargesKey() {
        return chargesKey;
    }

    @Override
    public boolean isOutOfCharges(@NotNull ItemStack item) {
        return EnchantUtils.isOutOfCharges(item, this);
    }

    @Override
    public boolean isFullOfCharges(@NotNull ItemStack item) {
        return EnchantUtils.isFullOfCharges(item, this);
    }

    @Override
    public int getCharges(@NotNull ItemStack item) {
        return EnchantUtils.getCharges(item, this);
    }

    @Override
    public void consumeCharges(@NotNull ItemStack item) {
        EnchantUtils.consumeCharges(item, this);
        EnchantUtils.updateDisplay(item);
    }
}
