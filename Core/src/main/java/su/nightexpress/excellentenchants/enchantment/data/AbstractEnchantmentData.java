package su.nightexpress.excellentenchants.enchantment.data;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.DistributionWay;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Cost;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.ItemCategory;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.ChanceData;
import su.nightexpress.excellentenchants.api.enchantment.data.PeriodicData;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionData;
import su.nightexpress.excellentenchants.api.enchantment.distribution.DistributionOptions;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.util.EnchantPlaceholders;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.manager.AbstractFileData;
import su.nightexpress.nightcore.manager.SimpeListener;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderMap;
import su.nightexpress.nightcore.util.random.Rnd;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static su.nightexpress.excellentenchants.Placeholders.*;

public abstract class AbstractEnchantmentData extends AbstractFileData<EnchantsPlugin> implements EnchantmentData {

    protected Enchantment enchantment;

    private Rarity       rarity;
    private String       displayName;
    private List<String> description;

    private boolean hiddenFromList;
    private boolean treasure;
    private boolean visualEffects;

    private int maxLevel;

    private Cost minCost;
    private Cost maxCost;
    private int anvilCost;

    private boolean   chargesEnabled;
    private boolean   chargesCustomFuel;
    private Modifier  chargesMax;
    private Modifier  chargesConsumeAmount;
    private Modifier  chargesRechargeAmount;
    private ItemStack chargesFuel;

    private final DistributionOptions distributionOptions;
    private final Set<String>         conflicts;
    private final NamespacedKey       chargesKey;
    private final EnchantPlaceholders placeholders;

    public AbstractEnchantmentData(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription(new ArrayList<>());
        this.setRarity(Rarity.COMMON);
        this.setMaxLevel(3);

        this.conflicts = new HashSet<>();
        this.chargesKey = new NamespacedKey(plugin, this.getId() + ".charges");
        this.placeholders = new EnchantPlaceholders();

        if (!Config.isVanillaDistribution()) {
            this.distributionOptions = new CustomDistribution(this);
        }
        else {
            this.distributionOptions = new VanillaDistribution();
        }
    }

    public void registerListeners() {
        if (this instanceof SimpeListener listener) {
            this.plugin.getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public boolean checkServerRequirements() {
        return true;
    }

    @Override
    protected boolean onLoad(@NotNull FileConfig cfg) {
        this.setRarity(ConfigValue.create("Settings.Rarity", Rarity.class, this.rarity,
            "The rarity is an attribute of an enchantment.",
                "It affects the chance of getting an enchantment from enchanting or loots as well as the combination cost in anvil.",
            "[*] Only for versions BELOW 1.20.6!"
        ).read(cfg));

        // TODO Custom rarity class for 1.21

        this.setDisplayName(ConfigValue.create("Settings.Name",
            StringUtil.capitalizeUnderscored(this.getId()),
            "Enchantment name."
        ).read(cfg));

        this.setDescription(ConfigValue.create("Settings.Description",
            this.getDescription(),
            "Enchantment description.",
            "You can use 'Enchantment' placeholders: " + URL_PLACEHOLDERS
        ).read(cfg));

        this.setMaxLevel(ConfigValue.create("Settings.Level.Max",
            this.getMaxLevel(),
            "Max. enchantment level."
        ).read(cfg));

        this.setHiddenFromList(ConfigValue.create("Settings.Hide_From_List",
            false,
            "Sets whether or not this enchantment will be hidden from Enchants GUI."
        ).read(cfg));

        this.setConflicts(ConfigValue.create("Settings.Conflicts",
            this.getConflicts(),
            "Makes this enchantment exclusive for ones in the list.",
            "Conflicting enchantments can not be combined on anvils and obtained together on the same item."
        ).read(cfg));

        this.setVisualEffects(ConfigValue.create("Settings.VisualEffects.Enabled",
            true,
            "Enables enchantment visual effects (mostly particles)."
        ).read(cfg));



        this.setTreasure(ConfigValue.create("Distribution.Treasure",
            false,
            "Sets whether this enchantment is a treasure enchantment.",
            "Treasure enchantments can only be received via looting, trading, or fishing."
        ).read(cfg));

        //int costAdjust = Math.max(7, 42 - this.getMaxLevel() * 7);
        int costBase = Config.isVanillaDistribution() ? 45 : 30;
        int costStart = Rnd.get(5) + 1;
        int costMod = Rnd.get(10) + 1;
        int costPerLevel = (int) ((double) costBase / (double) this.getMaxLevel());

        this.setMinCost(Cost.read(cfg, "Distribution." + DistributionWay.ENCHANTING.getPathName() + ".Cost.Min",
            new Cost(costStart, costPerLevel),
            VANILLA_DISTRIBUTION_HEADER,
            "Sets min. **modified** level cost for this enchantment to be selected in enchanting table.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            CUSTOM_DISTRIBUTION_HEADER,
            "Sets min. **plain** level cost for this enchantment to be selected in enchanting table."
            )
        );

        this.setMaxCost(Cost.read(cfg, "Distribution." + DistributionWay.ENCHANTING.getPathName() + ".Cost.Max",
            new Cost(costStart * costMod, costPerLevel),
            VANILLA_DISTRIBUTION_HEADER,
            "Sets max. **modified** level cost for this enchantment to be selected in enchanting table.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            CUSTOM_DISTRIBUTION_HEADER,
            "Sets max. **plain** level cost for this enchantment to be selected in enchanting table.")
        );

        // TODO Check what actually does
        this.setAnvilCost(ConfigValue.create("Anvil.Cost",
            Rnd.get(8) + 1,
            "Sets enchantment anvil cost.",
            "[*] Works for 1.20.6+ only!"
        ).read(cfg));

        this.distributionOptions.load(cfg);



        /*this.setAnvilMergeCost(Modifier.read(cfg, "Anvil.Merge.Cost",
            Modifier.add(1, 1, 1),
            "Sets XP cost to apply or transfer this enchantment using anvils."
        ));

        this.setMaxMergeLevel(ConfigValue.create("Anvil.Merge.Max_Level",
            -1,
            "Max. enchantment level that can be obtained by combining 2 items with this enchantment.",
            "Set to '-1' to remove limit and cap to max. enchantment level."
        ).read(cfg));*/



        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get() && !this.isCurse()) {
            this.setChargesEnabled(ConfigValue.create("Charges.Enabled",
                false,
                "When 'true' enables the Charges system for this enchantment.",
                "[*] Enchantments in enchanting table are generated with maximum charges."
            ).read(cfg));

            this.setChargesCustomFuel(ConfigValue.create("Charges.Custom_Fuel",
                false,
                "When 'true' uses different (non-default) fuel item (from the 'Fuel_Item' setting) to recharge."
            ).read(cfg));

            this.setChargesMax(Modifier.read(cfg, "Charges.Maximum",
                Modifier.add(100, 25, 1),
                "Maximum amount of charges for the enchantment."
            ));

            this.setChargesConsumeAmount(Modifier.read(cfg, "Charges.Consume_Amount",
                Modifier.add(1, 0, 0),
                "How many charges will be consumed when enchantment is triggered?"
            ));

            this.setChargesRechargeAmount(Modifier.read(cfg, "Charges.Recharge_Amount",
                Modifier.add(25, 5, 1),
                "How many charges will be restored when using 'Fuel Item' in anvil?"
            ));

            this.setChargesFuel(ConfigValue.create("Charges.Fuel_Item",
                new ItemStack(Material.LAPIS_LAZULI),
                "An item, that will be used to restore enchantment charges on anvils.",
                WIKI_ITEMS_URL
            ).read(cfg));
        }

        this.placeholders
            .add(ENCHANTMENT_ID, this::getId)
            .add(ENCHANTMENT_NAME, this::getName)
            .add(ENCHANTMENT_NAME_FORMATTED, this::getNameFormatted)
            .add(ENCHANTMENT_DESCRIPTION, () -> String.join("\n", this.getDescription()))
            .add(ENCHANTMENT_DESCRIPTION_FORMATTED, () -> String.join("\n", this.getDescriptionFormatted()))
            .add(ENCHANTMENT_DESCRIPTION_REPLACED, level -> String.join("\n", this.getDescriptionReplaced(level)))
            .add(ENCHANTMENT_LEVEL, NumberUtil::toRoman)
            .add(ENCHANTMENT_LEVEL_MIN, () -> String.valueOf(1))
            .add(ENCHANTMENT_LEVEL_MAX, () -> String.valueOf(this.getMaxLevel()))
            .add(ENCHANTMENT_RARITY, () -> plugin.getLangManager().getEnum(this.getRarity()))
            .add(ENCHANTMENT_FIT_ITEM_TYPES, () -> {
                if (this.getItemCategories().length == 0) return plugin.getLangManager().getEnum(this.getCategory());

                return String.join(", ", Stream.of(this.getItemCategories()).map(type -> plugin.getLangManager().getEnum(type)).toList());
            })
            .add(ENCHANTMENT_CHARGES_MAX_AMOUNT, level -> NumberUtil.format(this.getChargesMax(level)))
            .add(ENCHANTMENT_CHARGES_CONSUME_AMOUNT, level -> NumberUtil.format(this.getChargesConsumeAmount(level)))
            .add(ENCHANTMENT_CHARGES_RECHARGE_AMOUNT, level -> NumberUtil.format(this.getChargesRechargeAmount(level)))
            .add(ENCHANTMENT_CHARGES_FUEL_ITEM, () -> ItemUtil.getItemName(this.getChargesFuel()));
        if (this instanceof ChanceData chanceData) {
            this.placeholders.add(ENCHANTMENT_CHANCE, level -> NumberUtil.format(chanceData.getTriggerChance(level)));
        }
        if (this instanceof PeriodicData periodicData) {
            this.placeholders.add(ENCHANTMENT_INTERVAL, () -> NumberUtil.format(periodicData.getInterval() / 20D));
        }
        if (this instanceof PotionData potionData) {
            this.placeholders.add(ENCHANTMENT_POTION_LEVEL, level -> NumberUtil.toRoman(potionData.getEffectAmplifier(level)));
            this.placeholders.add(ENCHANTMENT_POTION_DURATION, level -> NumberUtil.format(potionData.getEffectDuration(level) / 20D));
            this.placeholders.add(ENCHANTMENT_POTION_TYPE, () -> LangAssets.get(potionData.getEffectType()));
        }
        if (this.getDistributionOptions() instanceof CustomDistribution distribution) {
            this.placeholders.add(ENCHANTMENT_OBTAIN_CHANCE_ENCHANTING, () -> NumberUtil.format(distribution.getWeight(DistributionWay.ENCHANTING)));
            this.placeholders.add(ENCHANTMENT_OBTAIN_CHANCE_VILLAGER, () -> NumberUtil.format(distribution.getWeight(DistributionWay.VILLAGER)));
            this.placeholders.add(ENCHANTMENT_OBTAIN_CHANCE_LOOT_GENERATION, () -> NumberUtil.format(distribution.getWeight(DistributionWay.LOOT_GENERATION)));
            this.placeholders.add(ENCHANTMENT_OBTAIN_CHANCE_FISHING, () -> NumberUtil.format(distribution.getWeight(DistributionWay.FISHING)));
            this.placeholders.add(ENCHANTMENT_OBTAIN_CHANCE_MOB_SPAWNING, () -> NumberUtil.format(distribution.getWeight(DistributionWay.MOB_EQUIPMENT)));
        }

        this.loadAdditional(cfg);
        this.registerListeners();
        return true;
    }

    protected abstract void loadAdditional(@NotNull FileConfig config);

    @Override
    protected void onSave(@NotNull FileConfig cfg) {

    }

    @NotNull
    private String logPrefix() {
        return "[" + this.getId() + "] ";
    }

    protected void info(@NotNull String text) {
        this.plugin.info(this.logPrefix() + text);
    }

    protected void warn(@NotNull String text) {
        this.plugin.warn(this.logPrefix() + text);
    }

    protected void error(@NotNull String text) {
        this.plugin.error(this.logPrefix() + text);
    }

    @NotNull
    public PlaceholderMap getPlaceholders(int level) {
        return this.placeholders.toMap(level);
    }

    public void addPlaceholder(@NotNull String key, @NotNull Function<Integer, String> replacer) {
        this.placeholders.add(key, replacer);
    }

    @Override
    @NotNull
    public ItemCategory[] getItemCategories() {
        return new ItemCategory[0];
    }

    @Override
    public EquipmentSlot[] getSlots() {
        return switch (this.getCategory()) {
            case BOW, CROSSBOW, TRIDENT, FISHING_ROD, WEAPON, TOOL -> new EquipmentSlot[]{EquipmentSlot.HAND};
            case ARMOR_HEAD -> new EquipmentSlot[]{EquipmentSlot.HEAD};
            case ARMOR_TORSO -> new EquipmentSlot[]{EquipmentSlot.CHEST};
            case ARMOR_LEGS -> new EquipmentSlot[]{EquipmentSlot.LEGS};
            case ARMOR_FEET -> new EquipmentSlot[]{EquipmentSlot.FEET};
            case ARMOR, WEARABLE -> new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            case BREAKABLE -> new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            case VANISHABLE -> EnchantUtils.EQUIPMENT_SLOTS;
            default -> throw new IllegalStateException("Unexpected value: " + this.getCategory());
        };
    }

    @Override
    public boolean isAvailableToUse(@NotNull World world) {
        Set<String> disabled = Config.ENCHANTMENTS_DISABLED_IN_WORLDS.get().getOrDefault(world.getName().toLowerCase(), Collections.emptySet());
        return disabled.isEmpty() || (!disabled.contains(this.getId()) && !disabled.contains(WILDCARD));
    }

    @Override
    public boolean isAvailableToUse(@NotNull LivingEntity entity) {
        return this.isAvailableToUse(entity.getWorld());
    }

    @Override
    public boolean checkEnchantLimit(@NotNull ItemStack item) {
        // Allow to re-enchant item with the same enchantment.
        if (EnchantUtils.contains(item, this.getEnchantment())) {
            return true;
        }

        return !EnchantUtils.hasMaximumEnchants(item);
    }

    @Override
    public final boolean checkEnchantCategory(@NotNull ItemStack item) {
        EnchantmentTarget category = this.getCategory();

        if (category == EnchantmentTarget.WEAPON && ItemUtil.isAxe(item)) {
            return Config.CORE_SWORD_ENCHANTS_TO_AXES.get();
        }
        if (category == EnchantmentTarget.BOW && item.getType() == Material.CROSSBOW) {
            return Config.CORE_BOW_ENCHANTS_TO_CROSSBOW.get();
        }
        if ((category == EnchantmentTarget.ARMOR || category == EnchantmentTarget.ARMOR_TORSO) && item.getType() == Material.ELYTRA) {
            return Config.CORE_CHESTPLATE_ENCHANTS_TO_ELYTRA.get();
        }
        return false;
    }

    @Override
    public boolean checkItemCategory(@NotNull ItemStack item) {
        return !this.hasItemCategory() || Stream.of(this.getItemCategories()).anyMatch(itemCategory -> itemCategory.isIncluded(item));
    }

    public int generateLevel() {
        return Rnd.get(1, this.getMaxLevel());
    }

    @Override
    @NotNull
    public String getNameFormatted(int level, int charges) {
        String rarityFormat = this.isCurse() ? Config.ENCHANTMENTS_DISPLAY_NAME_CURSE_FORMAT.get() : Config.ENCHANTMENTS_DISPLAY_NAME_RARITY_FORMAT.get().getOrDefault(this.getRarity(), GENERIC_NAME);
        String chargesFormat = "";
        boolean showLevel = !Config.ENCHANTMENTS_DISPLAY_NAME_HIDE_1ST_LEVEL.get() || level > 1;
        boolean showCharges = this.isChargesEnabled() && charges >= 0;

        if (showCharges) {
            int chargesMax = this.getChargesMax(level);
            int percent = (int) Math.ceil((double) charges / (double) chargesMax * 100D);
            Map.Entry<Integer, String> entry = Config.ENCHANTMENTS_CHARGES_FORMAT.get().floorEntry(percent);
            if (entry != null) {
                chargesFormat = entry.getValue().replace(GENERIC_AMOUNT, String.valueOf(charges));
            }
        }

        String compName = Config.ENCHANTMENTS_DISPLAY_NAME_COMPONENT_NAME.get().replace(GENERIC_VALUE, this.getName());
        String compLevel = showLevel ? Config.ENCHANTMENTS_DISPLAY_NAME_COMPONENT_LEVEL.get().replace(GENERIC_VALUE, NumberUtil.toRoman(level)) : "";
        String compChrages = showCharges ? Config.ENCHANTMENTS_DISPLAY_NAME_COMPONENT_CHARGES.get().replace(GENERIC_VALUE, chargesFormat) : "";

        String nameFormat = Config.ENCHANTMENTS_DISPLAY_NAME_FORMAT.get().
            replace(ENCHANTMENT_NAME, compName)
            .replace(ENCHANTMENT_LEVEL, compLevel)
            .replace(ENCHANTMENT_CHARGES, compChrages);

        return rarityFormat.replace(GENERIC_NAME, nameFormat);
    }

    @Override
    @NotNull
    public List<String> getDescriptionFormatted() {
        return new ArrayList<>(this.getDescription().stream()
            .map(line -> Config.ENCHANTMENTS_DISPLAY_DESCRIPTION_FORMAT.get().replace(GENERIC_DESCRIPTION, line))
            .toList());
    }

    @Override
    @NotNull
    public List<String> getDescriptionReplaced(int level) {
        List<String> description = new ArrayList<>(this.getDescriptionFormatted());
        description.replaceAll(this.getPlaceholders(level).replacer());
        return description;
    }

    @Override
    public int getMinCost(int level) {
        return this.getMinCost().calculate(level);
    }

    @Override
    public int getMaxCost(int level) {
        return this.getMaxCost().calculate(level);
    }

    /*@Override
    public int getAnvilMergeCost(int level) {
        return this.getAnvilMergeCost().getIntValue(level);
    }*/

    @Override
    public int getChargesMax(int level) {
        return this.isChargesEnabled() ? this.getChargesMax().getIntValue(level) : 0;
    }

    @Override
    public int getChargesConsumeAmount(int level) {
        return this.isChargesEnabled() ? this.getChargesConsumeAmount().getIntValue(level) : 0;
    }

    @Override
    public int getChargesRechargeAmount(int level) {
        return this.isChargesEnabled() ? this.getChargesRechargeAmount().getIntValue(level) : 0;
    }

    @Override
    public boolean isChargesFuel(@NotNull ItemStack item) {
        if (Config.ENCHANTMENTS_CHARGES_COMPARE_TYPE_ONLY.get()) {
            return item.getType() == this.getChargesFuel().getType();
        }
        return item.isSimilar(this.getChargesFuel());
    }

    @Override
    public boolean isOutOfCharges(@NotNull ItemStack item) {
        return this.getCharges(item) == 0;
    }

    @Override
    public boolean isFullOfCharges(@NotNull ItemStack item) {
        if (!this.isChargesEnabled()) return true;

        int level = EnchantUtils.getLevel(item, this.getEnchantment());
        int max = this.getChargesMax(level);

        return this.getCharges(item) == max;
    }

    @Override
    public int getCharges(@NotNull ItemMeta meta) {
        return this.isChargesEnabled() ? PDCUtil.getInt(meta, this.chargesKey).orElse(0) : -1;
    }

    @Override
    public void setCharges(@NotNull ItemStack item, int level, int amount) {
        if (!this.isChargesEnabled()) return;

        int max = this.getChargesMax(level);
        int set = Math.min(Math.abs(amount), max);
        PDCUtil.set(item, this.chargesKey, set);
    }

    @Override
    public void restoreCharges(@NotNull ItemStack item, int level) {
        this.setCharges(item, level, Integer.MAX_VALUE);
    }

    @Override
    public void fuelCharges(@NotNull ItemStack item, int level) {
        int recharge = this.getChargesRechargeAmount(level);

        int has = this.getCharges(item);
        int set = has + recharge;

        this.setCharges(item, level, set);
    }

    @Override
    public void consumeChargesNoUpdate(@NotNull ItemStack item, int level) {
        if (!this.isChargesEnabled()) return;

        int charges = this.getCharges(item);
        int consumeAmount = this.getChargesConsumeAmount(level);

        this.setCharges(item, level, charges < consumeAmount ? 0 : Math.max(0, charges - consumeAmount));
    }

    @Override
    public void consumeCharges(@NotNull ItemStack item, int level) {
        if (!this.isChargesEnabled()) return;

        this.consumeChargesNoUpdate(item, level);
        EnchantUtils.updateDisplay(item);
    }

    @Override
    @NotNull
    public Enchantment getEnchantment() {
        return this.enchantment;
    }

    @Override
    public void setEnchantment(@NotNull Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @NotNull
    @Override
    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public void setRarity(@NotNull Rarity rarity) {
        this.rarity = rarity;
    }

    @NotNull
    @Override
    public DistributionOptions getDistributionOptions() {
        return distributionOptions;
    }

    @NotNull
    public String getName() {
        return displayName;
    }

    @Override
    public void setDisplayName(@NotNull String displayName) {
        this.displayName = displayName;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return description;
    }

    @Override
    public void setDescription(@NotNull List<String> description) {
        this.description = description;
    }

    @Override
    public boolean isHiddenFromList() {
        return hiddenFromList;
    }

    @Override
    public void setHiddenFromList(boolean hiddenFromList) {
        this.hiddenFromList = hiddenFromList;
    }

    @Override
    public boolean isTreasure() {
        return this.treasure;
    }

    @Override
    public void setTreasure(boolean treasure) {
        this.treasure = treasure;
    }

    @Override
    public void setMaxLevel(int levelMax) {
        this.maxLevel = Math.max(1, levelMax);
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    @NotNull
    public Cost getMinCost() {
        return this.minCost;
    }

    @Override
    public void setMinCost(@NotNull Cost minCost) {
        this.minCost = minCost;
    }

    @Override
    @NotNull
    public Cost getMaxCost() {
        return this.maxCost;
    }

    @Override
    public void setMaxCost(@NotNull Cost maxCost) {
        this.maxCost = maxCost;
    }

    @Override
    public int getAnvilCost() {
        return anvilCost;
    }

    @Override
    public void setAnvilCost(int anvilCost) {
        this.anvilCost = anvilCost;
    }

    /*@Override
    @NotNull
    public Modifier getAnvilMergeCost() {
        return this.anvilMergeCost;
    }

    @Override
    public void setAnvilMergeCost(@NotNull Modifier anvilMergeCost) {
        this.anvilMergeCost = anvilMergeCost;
    }

    @Override
    public int getMaxMergeLevel() {
        return this.maxMergeLevel;
    }

    @Override
    public void setMaxMergeLevel(int maxMergeLevel) {
        this.maxMergeLevel = Math.min(this.getMaxLevel(), maxMergeLevel);
    }*/

    @Override
    @NotNull
    public Set<String> getConflicts() {
        return this.conflicts;
    }

    @Override
    public void setConflicts(@NotNull Set<String> conflicts) {
        this.conflicts.clear();
        this.conflicts.addAll(conflicts.stream().map(String::toLowerCase).collect(Collectors.toSet()));
    }

    @Override
    public boolean hasVisualEffects() {
        return visualEffects;
    }

    @Override
    public void setVisualEffects(boolean visualEffects) {
        this.visualEffects = visualEffects;
    }

    @Override
    public boolean isChargesEnabled() {
        return chargesEnabled;
    }

    @Override
    public void setChargesEnabled(boolean chargesEnabled) {
        this.chargesEnabled = chargesEnabled;
    }

    @Override
    public boolean isChargesCustomFuel() {
        return chargesCustomFuel;
    }

    @Override
    public void setChargesCustomFuel(boolean chargesCustomFuel) {
        this.chargesCustomFuel = chargesCustomFuel;
    }

    @Override
    @NotNull
    public Modifier getChargesMax() {
        return chargesMax;
    }

    @Override
    public void setChargesMax(@NotNull Modifier chargesMax) {
        this.chargesMax = chargesMax;
    }

    @NotNull
    public ItemStack getChargesFuel() {
        ItemStack fuelHas = this.chargesFuel;
        if (!this.isChargesCustomFuel() || fuelHas == null || fuelHas.getType().isAir()) {
            return new ItemStack(Config.ENCHANTMENTS_CHARGES_FUEL_ITEM.get());
        }
        return new ItemStack(fuelHas);
    }

    @Override
    public void setChargesFuel(@Nullable ItemStack chargesFuel) {
        this.chargesFuel = chargesFuel;
    }

    @Override
    @NotNull
    public Modifier getChargesConsumeAmount() {
        return chargesConsumeAmount;
    }

    @Override
    public void setChargesConsumeAmount(@NotNull Modifier chargesConsumeAmount) {
        this.chargesConsumeAmount = chargesConsumeAmount;
    }

    @Override
    @NotNull
    public Modifier getChargesRechargeAmount() {
        return chargesRechargeAmount;
    }

    @Override
    public void setChargesRechargeAmount(@NotNull Modifier chargesRechargeAmount) {
        this.chargesRechargeAmount = chargesRechargeAmount;
    }
}
