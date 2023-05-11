package su.nightexpress.excellentenchants.enchantment.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.ExcellentEnchantsAPI;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.type.ObtainType;
import su.nightexpress.excellentenchants.tier.Tier;
import su.nightexpress.excellentenchants.tier.TierManager;

import java.util.*;

public class EnchantDefaults {

    private final Map<ObtainType, Double> obtainChance;
    private final Map<ObtainType, int[]>  obtainLevelCap;

    private String        displayName;
    private Tier          tier;
    private List<String>  description;
    private boolean       isTreasure;
    private int           levelMin;
    private int           levelMax;
    private EnchantScaler levelByEnchantCost;
    private EnchantScaler anvilMergeCost;
    private Set<String>   conflicts;
    private boolean       visualEffects;

    private boolean       chargesEnabled;
    private boolean       chargesCustomFuel;
    private EnchantScaler chargesMax;
    private EnchantScaler chargesConsumeAmount;
    private EnchantScaler chargesRechargeAmount;
    private ItemStack     chargesFuel;

    public EnchantDefaults(@NotNull ExcellentEnchant enchant) {
        this.setDisplayName(StringUtil.capitalizeUnderscored(enchant.getId()));
        this.setTier(0.1);
        this.setDescription(new ArrayList<>());
        this.setTreasure(false);
        this.setLevelMin(1);
        this.setLevelMax(3);
        this.setConflicts(new HashSet<>());
        this.setVisualEffects(true);
        this.obtainChance = new HashMap<>();
        this.obtainLevelCap = new HashMap<>();
    }

    public void load(@NotNull ExcellentEnchant enchant) {
        ExcellentEnchants plugin = ExcellentEnchantsAPI.PLUGIN;
        JYML cfg = enchant.getConfig();

        this.setDisplayName(JOption.create("Name", this.getDisplayName(),
            "Enchantment display name. It will be shown in item lore.").read(cfg));

        Tier tier = plugin.getTierManager().getTierById(JOption.create("Tier", this.getTier().getId(),
            "Enchantment tier. Must be a valid tier identifier from the '" + TierManager.FILE_NAME + "'.").read(cfg));
        this.setTier(tier == null ? plugin.getTierManager().getMostCommon() : tier);
        //this.getTier().getEnchants().add(enchant);

        this.setDescription(JOption.create("Description", this.getDescription(),
            "Enchantment description. It will be shown in item lore under enchantment name.",
            "You can use 'Enchantment' placeholders: " + Placeholders.URL_PLACEHOLDERS)
            .read(cfg));

        this.setTreasure(JOption.create("Is_Treasure", this.isTreasure(),
            "Sets whether this enchantment is a treasure enchantment.",
            "Treasure enchantments can only be received via looting, trading, or fishing.").read(cfg));

        this.setLevelMin(JOption.create("Level.Min", this.getLevelMin(),
            "Sets the minimal (start) enchantment level. Can not be less than 1.").read(cfg));

        this.setLevelMax(JOption.create("Level.Max", this.getLevelMax(),
            "Sets the maximal enchantment level. Can not be less than min. level.",
            "Note: While you can 'bypass' this value by enchant commands, all level-dependant enchantment",
            "settings will have a limit up to this setting.").read(cfg));

        this.setLevelByEnchantCost(EnchantScaler.read(enchant, ObtainType.ENCHANTING.getPathName() + ".Level_By_Exp_Cost",
            (int)(30D / this.levelMax) + " * " + Placeholders.ENCHANTMENT_LEVEL,
            "Sets how much XP levels must be used in enchanting table to obtain this enchantment.",
            "With a default formula '9 * %enchantment_level%' it will be [9, 18, 27] XP levels for [1, 2, 3] enchantment levels."));

        this.setAnvilMergeCost(EnchantScaler.read(enchant, "Anvil.Merge_Cost", Placeholders.ENCHANTMENT_LEVEL,
            "Sets how much XP levels will be added to the anvil cost when combining custom enchantments."));

        for (ObtainType obtainType : ObtainType.values()) {
            double obtainChance = JOption.create(obtainType.getPathName() + ".Chance", 50D,
                "Chance for this enchantment to be obtained via " + obtainType.getPathName()).read(cfg);
            this.getObtainChance().put(obtainType, obtainChance);

            int levelMin = JOption.create(obtainType.getPathName() + ".Level.Min", -1,
                "Minimal level when obtained via " + obtainType.getPathName(),
                "Can not be less than enchantment min. level. Set -1 to use enchantment min. level.").read(cfg);
            int levelMax = JOption.create(obtainType.getPathName() + ".Level.Max", -1,
                "Maximal level when obtained via " + obtainType.getPathName(),
                "Can not be greater than enchantment max. level. Set -1 to use enchantment max. level.").read(cfg);
            this.getObtainLevelCap().put(obtainType, new int[]{levelMin, levelMax});
        }


        this.setConflicts(JOption.create("Conflicts", this.getConflicts(),
            "A list of conflicting enchantment names.",
            "Conflicting enchantments can not be combined on anvils and obtained together on the same item.").read(cfg));

        this.setVisualEffects(JOption.create("Settings.Visual_Effects", this.isVisualEffects(),
            "Enables/Disables enchantment visual effects, such as particles.").read(cfg));


        if (Config.ENCHANTMENTS_CHARGES_ENABLED.get()) {
            this.setChargesEnabled(JOption.create("Settings.Charges.Enabled", this.isChargesEnabled(),
                "When 'true' enables the Charges system for this enchantment.",
                "When enchanted the first time on enchanting table, it will have maximum charges amount.").read(cfg));
            this.setChargesCustomFuel(JOption.create("Settings.Charges.Custom_Fuel", this.isChargesCustomFuel(),
                "When 'true' uses different (non-default) fuel item (from the 'Fuel_Item' setting) to recharge.").read(cfg));
            this.setChargesMax(EnchantScaler.read(enchant, "Settings.Charges.Maximum", "100",
                "Maximum amount of charges for the enchantment."));
            this.setChargesConsumeAmount(EnchantScaler.read(enchant, "Settings.Charges.Consume_Amount", "1",
                "How many charges will be consumed when enchantment is triggered?"));
            this.setChargesRechargeAmount(EnchantScaler.read(enchant, "Settings.Charges.Recharge_Amount", "25",
                "How many charges will be restored when using 'Fuel Item' in anvil?"));
            this.setChargesFuel(JOption.create("Settings.Charges.Fuel_Item", new ItemStack(Material.LAPIS_LAZULI),
                "An item, that will be used to restore enchantment charges on anvils.",
                "Item Options:" + Placeholders.URL_ENGINE_ITEMS)
                .setWriter(JYML::setItem).read(cfg));
        }
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NotNull String displayName) {
        this.displayName = Colorizer.apply(displayName);
    }

    @NotNull
    public Tier getTier() {
        return tier;
    }

    public void setTier(double rarity) {
        this.setTier(ExcellentEnchantsAPI.getTierManager().getByRarityModifier(rarity));
    }

    public void setTier(@NotNull Tier tier) {
        this.tier = tier;
    }

    public void setDescription(@NotNull String... description) {
        this.setDescription(Arrays.asList(description));
    }

    public void setDescription(@NotNull List<String> description) {
        this.description = Colorizer.apply(description);
    }

    @NotNull
    public List<String> getDescription() {
        return description;
    }

    public boolean isTreasure() {
        return isTreasure;
    }

    public void setTreasure(boolean treasure) {
        isTreasure = treasure;
    }

    public void setLevelMin(int levelMin) {
        this.levelMin = Math.max(1, levelMin);
    }

    public int getLevelMin() {
        return levelMin;
    }

    public void setLevelMax(int levelMax) {
        this.levelMax = Math.max(1, levelMax);
    }

    public int getLevelMax() {
        return levelMax;
    }

    @NotNull
    public EnchantScaler getLevelByEnchantCost() {
        return levelByEnchantCost;
    }

    public void setLevelByEnchantCost(@NotNull EnchantScaler levelByEnchantCost) {
        this.levelByEnchantCost = levelByEnchantCost;
    }

    @NotNull
    public EnchantScaler getAnvilMergeCost() {
        return anvilMergeCost;
    }

    public void setAnvilMergeCost(@NotNull EnchantScaler anvilMergeCost) {
        this.anvilMergeCost = anvilMergeCost;
    }

    @NotNull
    public Map<ObtainType, Double> getObtainChance() {
        return obtainChance;
    }

    @NotNull
    public Map<ObtainType, int[]> getObtainLevelCap() {
        return obtainLevelCap;
    }

    public void setConflicts(@NotNull String... conflicts) {
        this.setConflicts(new HashSet<>(Arrays.asList(conflicts)));
    }

    public void setConflicts(@NotNull Set<String> conflicts) {
        this.conflicts = conflicts;
    }

    @NotNull
    public Set<String> getConflicts() {
        return conflicts;
    }

    public boolean isVisualEffects() {
        return visualEffects;
    }

    public void setVisualEffects(boolean visualEffects) {
        this.visualEffects = visualEffects;
    }

    public boolean isChargesEnabled() {
        return chargesEnabled;
    }

    public void setChargesEnabled(boolean chargesEnabled) {
        this.chargesEnabled = chargesEnabled;
    }

    public boolean isChargesCustomFuel() {
        return chargesCustomFuel;
    }

    public void setChargesCustomFuel(boolean chargesCustomFuel) {
        this.chargesCustomFuel = chargesCustomFuel;
    }

    @NotNull
    public EnchantScaler getChargesMax() {
        return chargesMax;
    }

    public void setChargesMax(@NotNull EnchantScaler chargesMax) {
        this.chargesMax = chargesMax;
    }

    @Nullable
    public ItemStack getChargesFuel() {
        return chargesFuel;
    }

    public void setChargesFuel(@Nullable ItemStack chargesFuel) {
        this.chargesFuel = chargesFuel;
    }

    @NotNull
    public EnchantScaler getChargesConsumeAmount() {
        return chargesConsumeAmount;
    }

    public void setChargesConsumeAmount(@NotNull EnchantScaler chargesConsumeAmount) {
        this.chargesConsumeAmount = chargesConsumeAmount;
    }

    @NotNull
    public EnchantScaler getChargesRechargeAmount() {
        return chargesRechargeAmount;
    }

    public void setChargesRechargeAmount(@NotNull EnchantScaler chargesRechargeAmount) {
        this.chargesRechargeAmount = chargesRechargeAmount;
    }
}
