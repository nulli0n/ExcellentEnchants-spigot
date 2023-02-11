package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.IListener;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.Scaler;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Chanced;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.util.EnchantPriority;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;
import su.nightexpress.excellentenchants.enchantment.config.EnchantScaler;
import su.nightexpress.excellentenchants.enchantment.impl.meta.ChanceImplementation;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;
import su.nightexpress.excellentenchants.enchantment.type.FitItemType;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.tier.Tier;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public abstract class ExcellentEnchant extends Enchantment implements IEnchantment, IListener {

    protected final ExcellentEnchants plugin;
    protected final JYML              cfg;
    protected final String            id;
    protected final EnchantPriority   priority;

    protected String       displayName;
    protected Tier         tier;
    protected List<String> description;
    protected boolean                 isTreasure;
    protected int                     levelMin;
    protected int                     levelMax;
    protected Scaler                  levelByEnchantCost;
    protected Scaler                  anvilMergeCost;
    protected Map<ObtainType, Double> obtainChance;
    protected Set<String>             conflicts;
    protected Map<ObtainType, int[]> obtainLevelCap;
    protected boolean                hasVisualEffects;

    protected boolean chargesEnabled;
    protected boolean chargesCustomFuel;
    protected EnchantScaler chargesMax;
    protected EnchantScaler chargesConsumeAmount;
    protected EnchantScaler chargesRechargeAmount;
    protected ItemStack     chargesFuel;
    protected final NamespacedKey chargesKey;

    public ExcellentEnchant(@NotNull ExcellentEnchants plugin, @NotNull String id, @NotNull EnchantPriority priority) {
        super(NamespacedKey.minecraft(id.toLowerCase()));
        this.plugin = plugin;
        this.id = this.getKey().getKey();
        this.cfg = JYML.loadOrExtract(plugin, "/enchants/" + id + ".yml");
        this.priority = priority;
        this.conflicts = new HashSet<>();
        this.chargesKey = new NamespacedKey(plugin, this.getId() + ".charges");
    }

    public void loadConfig() {
        this.cfg.reload();

        this.displayName = JOption.create("Name", StringUtil.capitalizeFully(this.getId().replace("_", " ")),
            "Enchantment display name. It will be shown in item lore.").read(cfg);

        this.tier = plugin.getTierManager().getTierById(JOption.create("Tier", Placeholders.DEFAULT,
            "Enchantment tier. Must be a valid tier identifier from the 'tiers.yml'.").read(cfg));
        if (this.tier == null) {
            this.tier = Tier.DEFAULT;
        }
        this.tier.getEnchants().add(this);

        this.description = JOption.create("Description", new ArrayList<>(),
            "Enchantment description. It will be shown in item lore under enchantment name.",
            "You can use 'Enchantment' placeholders: " + Placeholders.URL_PLACEHOLDERS).read(cfg);

        this.isTreasure = JOption.create("Is_Treasure", false,
            "Sets whether this enchantment is a treasure enchantment.",
            "Treasure enchantments can only be received via looting, trading, or fishing.").read(cfg);

        this.levelMin = Math.max(1, JOption.create("Level.Min", 1,
            "Sets the minimal (start) enchantment level. Can not be less than 1.").read(cfg));

        this.levelMax = JOption.create("Level.Max", 3,
            "Sets the maximal enchantment level. Can not be less than min. level.",
            "Note: While you can 'bypass' this value by enchant commands, all level-dependant enchantment",
            "settings will have a limit up to this setting.").read(cfg);

        this.levelByEnchantCost = EnchantScaler.read(this, ObtainType.ENCHANTING.getPathName() + ".Level_By_Exp_Cost",
            (int)(30D / this.levelMax) + " * " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets how much XP levels must be used in enchanting table to obtain this enchantment.",
            "With a default formula '9 * %enchantment_level%' it will be [9, 18, 27] XP levels for [1, 2, 3] enchantment levels.");

        this.anvilMergeCost = EnchantScaler.read(this, "Anvil.Merge_Cost", Placeholders.ENCHANTMENT_LEVEL,
            "Sets how much XP levels will be added to the anvil cost when combining custom enchantments.");

        this.obtainChance = new HashMap<>();
        this.obtainLevelCap = new HashMap<>();
        for (ObtainType obtainType : ObtainType.values()) {
            double obtainChance = JOption.create(obtainType.getPathName() + ".Chance", 50D,
                "Chance for this enchantment to be obtained via " + obtainType.getPathName()).read(cfg);
            this.obtainChance.put(obtainType, obtainChance);

            int levelMin = JOption.create(obtainType.getPathName() + ".Level.Min", -1,
                "Minimal level when obtained via " + obtainType.getPathName(),
                "Can not be less than enchantment min. level. Set -1 to use enchantment min. level.").read(cfg);
            int levelMax = JOption.create(obtainType.getPathName() + ".Level.Max", -1,
                "Maximal level when obtained via " + obtainType.getPathName(),
                "Can not be greater than enchantment max. level. Set -1 to use enchantment max. level.").read(cfg);
            this.obtainLevelCap.put(obtainType, new int[]{levelMin, levelMax});
        }


        this.conflicts = JOption.create("Conflicts", new HashSet<>(),
            "A list of conflicting enchantment names.",
            "Conflicting enchantments can not be combined on anvils and obtained together on the same item.").read(cfg);

        this.hasVisualEffects = JOption.create("Settings.Visual_Effects", true,
            "Enables/Disables enchantment visual effects, such as particles.").read(cfg);


        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
            this.chargesEnabled = JOption.create("Settings.Charges.Enabled", false,
                "When 'true' enables the Charges system for this enchantment.",
                "When enchanted the first time on enchanting table, it will have maximum charges amount.").read(cfg);
            this.chargesCustomFuel = JOption.create("Settings.Charges.Custom_Fuel", false,
                "When 'true' uses different (non-default) fuel item (from the 'Fuel_Item' setting) to recharge.").read(cfg);
            this.chargesMax = EnchantScaler.read(this, "Settings.Charges.Maximum", "100",
                "Maximum amount of charges for the enchantment.");
            this.chargesConsumeAmount = EnchantScaler.read(this, "Settings.Charges.Consume_Amount", "1",
                "How many charges will be consumed when enchantment is triggered?");
            this.chargesRechargeAmount = EnchantScaler.read(this, "Settings.Charges.Recharge_Amount", "25",
                "How many charges will be restored when using 'Fuel Item' in anvil?");
            this.chargesFuel = JOption.create("Settings.Charges.Fuel_Item", new ItemStack(Material.LAPIS_LAZULI),
                "An item, that will be used to restore enchantment charges on anvils.",
                "Item Options:" + Placeholders.URL_ENGINE_ITEMS)
                .setWriter(JYML::setItem).read(cfg);
        }
    }

    @NotNull
    public UnaryOperator<String> replaceAllPlaceholders(int level) {
        return str -> this.replacePlaceholders(level).apply(str)
            .replace(Placeholders.ENCHANTMENT_NAME, this.getDisplayName())
            .replace(Placeholders.ENCHANTMENT_NAME_FORMATTED, this.getNameFormatted(level))
            .replace(Placeholders.ENCHANTMENT_LEVEL, NumberUtil.toRoman(level))
            .replace(Placeholders.ENCHANTMENT_LEVEL_MIN, String.valueOf(this.getStartLevel()))
            .replace(Placeholders.ENCHANTMENT_LEVEL_MAX, String.valueOf(this.getMaxLevel()))
            .replace(Placeholders.ENCHANTMENT_TIER, this.getTier().getName())
            .replace(Placeholders.ENCHANTMENT_FIT_ITEM_TYPES, String.join(", ", Stream.of(this.getFitItemTypes()).map(type -> plugin.getLangManager().getEnum(type)).toList()))
            .replace(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_ENCHANTING, NumberUtil.format(this.getObtainChance(ObtainType.ENCHANTING)))
            .replace(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_VILLAGER, NumberUtil.format(this.getObtainChance(ObtainType.VILLAGER)))
            .replace(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_LOOT_GENERATION, NumberUtil.format(this.getObtainChance(ObtainType.LOOT_GENERATION)))
            .replace(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_FISHING, NumberUtil.format(this.getObtainChance(ObtainType.FISHING)))
            .replace(Placeholders.ENCHANTMENT_OBTAIN_CHANCE_MOB_SPAWNING, NumberUtil.format(this.getObtainChance(ObtainType.MOB_SPAWNING)))
            .replace(Placeholders.ENCHANTMENT_CHARGES_MAX_AMOUNT, String.valueOf(this.getChargesMax(level)))
            .replace(Placeholders.ENCHANTMENT_CHARGES_CONSUME_AMOUNT, String.valueOf(this.getChargesConsumeAmount(level)))
            .replace(Placeholders.ENCHANTMENT_CHARGES_RECHARGE_AMOUNT, String.valueOf(this.getChargesRechargeAmount(level)))
            .replace(Placeholders.ENCHANTMENT_CHARGES_FUEL_ITEM, ItemUtil.getItemName(this.getChargesFuel()))
            ;
    }

    @NotNull
    public UnaryOperator<String> replacePlaceholders(int level) {
        return str -> {
            str = str.replace(Placeholders.ENCHANTMENT_DESCRIPTION, String.join("\n", this.getDescription()));

            if (this instanceof Chanced chanced) {
                str = str.replace(ChanceImplementation.PLACEHOLDER_CHANCE, NumberUtil.format(chanced.getTriggerChance(level)));
            }
            if (this instanceof Potioned potioned) {
                str = str
                    .replace(PotionImplementation.PLACEHOLDER_POTION_LEVEL, NumberUtil.toRoman(potioned.getEffectAmplifier(level)))
                    .replace(PotionImplementation.PLACEHOLDER_POTION_DURATION, NumberUtil.format((double) potioned.getEffectDuration(level) / 20D))
                    .replace(PotionImplementation.PLACEHOLDER_POTION_TYPE, LangManager.getPotionType(potioned.getEffectType()));
            }
            return str;
        };
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
    @Override
    public String getName() {
        return getId().toUpperCase();
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NotNull
    public String getNameFormatted(int level) {
        return this.getTier().getColor() + this.getDisplayName() + " " + NumberUtil.toRoman(level);
    }

    @NotNull
    public String getNameFormatted(int level, int charges) {
        if (!this.isChargesEnabled() || charges < 0) return this.getNameFormatted(level);

        int chargesMax = this.getChargesMax(level);
        double percent = (double) charges / (double) chargesMax * 100D;
        Map.Entry<Double, String> entry = Config.ENCHANTMENTS_CHARGES_FORMAT.get().floorEntry(percent);
        if (entry == null) return this.getNameFormatted(level);

        String format = entry.getValue().replace(Placeholders.GENERIC_AMOUNT, String.valueOf(charges));
        return this.getNameFormatted(level) + " " + format;
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    @NotNull
    public List<String> getDescription(int level) {
        List<String> description = new ArrayList<>(this.description);
        description.replaceAll(this.replacePlaceholders(level));
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
        return conflicts;
    }

    @NotNull
    public Tier getTier() {
        return this.tier;
    }

    @Override
    public int getMaxLevel() {
        return this.levelMax;
    }

    @Override
    public int getStartLevel() {
        return this.levelMin;
    }

    public int getLevelByEnchantCost(int expLevel) {
        int get = this.levelByEnchantCost.getValues().entrySet().stream()
            .filter(en -> expLevel >= en.getValue().intValue()).max(Comparator.comparingInt(Map.Entry::getKey))
            .map(Map.Entry::getKey).orElse(0);

        return get != 0 ? this.fineLevel(get, ObtainType.ENCHANTING) : 0;
    }

    public double getObtainChance(@NotNull ObtainType obtainType) {
        return this.obtainChance.getOrDefault(obtainType, 0D);
    }

    public int getObtainLevelMin(@NotNull ObtainType obtainType) {
        return this.obtainLevelCap.getOrDefault(obtainType, new int[]{-1, -1})[0];
    }

    public int getObtainLevelMax(@NotNull ObtainType obtainType) {
        return this.obtainLevelCap.getOrDefault(obtainType, new int[]{-1, -1})[1];
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
        return (int) this.anvilMergeCost.getValue(level);
    }

    @Override
    public final boolean conflictsWith(@NotNull Enchantment enchantment) {
        return this.conflicts.contains(enchantment.getKey().getKey());
    }

    @Override
    public final boolean canEnchantItem(@Nullable ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        if (EnchantManager.getEnchantments(item).keySet().stream().anyMatch(e -> e.conflictsWith(this) || this.conflictsWith(e))) return false;
        if (EnchantManager.getEnchantmentLevel(item, this) <= 0 && EnchantManager.getExcellentEnchantmentsAmount(item) >= Config.ENCHANTMENTS_ITEM_CUSTOM_MAX.get()) {
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
        return this.isTreasure;
    }

    public boolean hasVisualEffects() {
        return this.hasVisualEffects;
    }

    public boolean isChargesEnabled() {
        return Config.ENCHANTMENTS_CHARGES_ENABLED.get() && this.chargesEnabled;
    }

    public boolean isChargesCustomFuel() {
        return chargesCustomFuel;
    }

    public int getChargesMax(int level) {
        return this.isChargesEnabled() ? (int) this.chargesMax.getValue(level) : 0;
    }

    public int getChargesConsumeAmount(int level) {
        return this.isChargesEnabled() ? (int) this.chargesConsumeAmount.getValue(level) : 0;
    }

    public int getChargesRechargeAmount(int level) {
        return this.isChargesEnabled() ? (int) this.chargesRechargeAmount.getValue(level) : 0;
    }

    @NotNull
    public ItemStack getChargesFuel() {
        ItemStack fuelHas = this.chargesFuel;
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
        return EnchantManager.isEnchantmentOutOfCharges(item, this);
    }

    @Override
    public boolean isFullOfCharges(@NotNull ItemStack item) {
        return EnchantManager.isEnchantmentFullOfCharges(item, this);
    }

    @Override
    public int getCharges(@NotNull ItemStack item) {
        return EnchantManager.getEnchantmentCharges(item, this);
    }

    @Override
    public void consumeCharges(@NotNull ItemStack item) {
        EnchantManager.consumeEnchantmentCharges(item, this);
    }
}
