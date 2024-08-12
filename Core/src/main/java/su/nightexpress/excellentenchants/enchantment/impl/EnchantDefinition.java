package su.nightexpress.excellentenchants.enchantment.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.excellentenchants.EnchantsAPI;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.Cost;
import su.nightexpress.excellentenchants.api.enchantment.Definition;
import su.nightexpress.excellentenchants.api.enchantment.ItemsCategory;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.List;
import java.util.Set;

public class EnchantDefinition implements Definition {

    private final String        rarityId;
    private final ItemsCategory supportedItems;
    private final ItemsCategory primaryItems;

    private List<String>  description;
    private String        displayName;
    private Rarity        rarity;
    private Set<String>   conflicts;
    private int           maxLevel;
    private Cost          minCost;
    private Cost          maxCost;
    private int           anvilCost;

    public EnchantDefinition(@NotNull List<String> description,
                             @NotNull String rarityId,
                             int maxLevel,
                             @NotNull ItemsCategory supportedItems,
                             @Nullable ItemsCategory primaryItems,
                             @Nullable Set<String> conflicts) {
        this.rarityId = rarityId;

        this.description = description;
        this.maxLevel = maxLevel;
        this.supportedItems = supportedItems;
        this.primaryItems = primaryItems;
        this.conflicts = conflicts;
    }

    @NotNull
    public static EnchantDefinition create(@NotNull List<String> description,
                                           @NotNull String rarityId,
                                           int maxLevel,
                                           @NotNull ItemsCategory supportedItems,
                                           @Nullable ItemsCategory primaryItems,
                                           @Nullable Set<String> conflicts) {
        return new EnchantDefinition(description, rarityId, maxLevel, supportedItems, primaryItems, conflicts);
    }

    @NotNull
    public static EnchantDefinition create(@NotNull String description,
                                           @NotNull String rarityId,
                                           int maxLevel,
                                           @NotNull ItemsCategory supportedItems,
                                           @Nullable ItemsCategory primaryItems) {
        return create(Lists.newList(description), rarityId, maxLevel, supportedItems, primaryItems, null);
    }

    @NotNull
    public static EnchantDefinition create(@NotNull String description,
                                           @NotNull String rarityId,
                                           int maxLevel,
                                           @NotNull ItemsCategory supportedItems) {
        return create(description, rarityId, maxLevel, supportedItems, (ItemsCategory) null);
    }

    @NotNull
    public static EnchantDefinition create(@NotNull String description,
                                           @NotNull String rarityId,
                                           int maxLevel,
                                           @NotNull ItemsCategory supportedItems,
                                           @NotNull Set<String> conflicts) {
        return create(Lists.newList(description), rarityId, maxLevel, supportedItems, null, conflicts);
    }

    public void load(@NotNull GameEnchantment enchantment, @NotNull FileConfig config) {
        String rarityId = ConfigValue.create("Settings.Rarity",
            this.rarityId,
            "Rarity affects the chance of getting an enchantment from enchanting or loots depending on rarity weight value.",
            "You can create and edit rarities in the config.yml"
        ).read(config);

        Rarity rarity = EnchantsAPI.getRarityManager().getRarity(rarityId);
        if (rarity == null) {
            enchantment.error("Invalid rarity '" + rarityId + "' for the '" + enchantment.getId() + "' enchantment! Replaced with dummy one.");
            rarity = EnchantRarity.DUMMY;
        }
        this.rarity = rarity;

        this.displayName = ConfigValue.create("Settings.Name",
            StringUtil.capitalizeUnderscored(enchantment.getId()),
            "Enchantment display name."
        ).read(config);

        this.description = ConfigValue.create("Settings.Description",
            this.description,
            "Enchantment description.",
            "You can use 'Enchantment' placeholders: " + Placeholders.WIKI_PLACEHOLDERS
        ).read(config);

        this.setMaxLevel(ConfigValue.create("Settings.Level.Max",
            this.maxLevel,
            "The maximum level of this enchantment.",
            "Value between 1 and 255 (inclusive).",
            "[*] Reboot required when changed."
        ).read(config));

        this.conflicts = ConfigValue.create("Settings.Conflicts",
            this.conflicts,
            "Enchantments that are incompatible with this enchantment.",
            "[*] Reboot required when changed."
        ).read(config);

        int costCap = Rnd.get(45, 65) + 1;
        int costPerLevel = (int) ((double) costCap / (double) this.maxLevel);

        int minCost = Rnd.nextInt(5) + 1;
        int maxCost = this.maxLevel == 1 ? costPerLevel : Rnd.get(3, 7) + minCost;

//        int costBase = 45;
//        int costStart = Rnd.get(5) + 1;
//        int costMod = Rnd.get(10) + 1;
//        int costPerLevel = (int) ((double) costBase / (double) this.getMaxLevel());

        this.minCost = Cost.read(config, "Settings.Cost.Min",
            new Cost(minCost, costPerLevel),
            "The minimum possible cost of this enchantment in levels.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            "[*] Reboot required when changed."
        );

        this.maxCost = Cost.read(config, "Settings.Cost.Max",
            new Cost(maxCost, costPerLevel),
            "The maximum possible cost of this enchantment in levels.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            "[*] Reboot required when changed."
        );

        this.anvilCost = ConfigValue.create("Settings.Cost.Anvil",
            Rnd.get(8) + 1,
            "The base cost when applying this enchantment to another item using an anvil. Halved when adding using a book, multiplied by the level of the enchantment.",
            "[*] Reboot required when changed."
        ).read(config);
    }

    private void setMaxLevel(int levelMax) {
        this.maxLevel = Math.clamp(levelMax, 1, EnchantUtils.LEVEL_CAP);
    }

    @Override
    public boolean hasConflicts() {
        return this.conflicts != null && !this.conflicts.isEmpty();
    }

    @NotNull
    @Override
    public ItemsCategory getSupportedItems() {
        return this.supportedItems;
    }

    @NotNull
    @Override
    public ItemsCategory getPrimaryItems() {
        return this.primaryItems == null ? this.supportedItems : this.primaryItems;
    }

    @NotNull
    @Override
    public Set<String> getConflicts() {
        return conflicts;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    @Override
    public List<String> getDescription() {
        return description;
    }

    @NotNull
    @Override
    public Rarity getRarity() {
        return rarity;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @NotNull
    @Override
    public Cost getMinCost() {
        return minCost;
    }

    @NotNull
    @Override
    public Cost getMaxCost() {
        return maxCost;
    }

    @Override
    public int getAnvilCost() {
        return anvilCost;
    }
}
