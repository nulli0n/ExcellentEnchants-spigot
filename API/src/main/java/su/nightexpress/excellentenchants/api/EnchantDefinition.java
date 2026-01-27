package su.nightexpress.excellentenchants.api;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsKeys;
import su.nightexpress.excellentenchants.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.item.ItemSet;
import su.nightexpress.excellentenchants.api.item.ItemSetDefaults;
import su.nightexpress.excellentenchants.api.item.ItemSetRegistry;
import su.nightexpress.excellentenchants.api.wrapper.EnchantCost;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Randomizer;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.*;

public class EnchantDefinition implements Writeable {

    private static final int LEVEL_CAP  = 255;
    private static final int WEIGHT_CAP = 1024;

    private final String       displayName;
    private final List<String> description;
    private final int          weight;
    private final int          maxLevel;
    private final EnchantCost  minCost;
    private final EnchantCost  maxCost;
    private final int          anvilCost;
    private final ItemSet      primaryItemSet;
    private final ItemSet      supportedItemSet;
    private final Set<String>  exclusiveSet;

    public EnchantDefinition(@NotNull String displayName,
                             @NotNull List<String> description,
                             int weight,
                             int maxLevel,
                             EnchantCost minCost,
                             EnchantCost maxCost,
                             int anvilCost,
                             @NotNull ItemSet supportedItems,
                             @NotNull ItemSet primaryItems,
                             @NotNull Set<String> exclusiveSet) {
        this.displayName = displayName;
        this.description = description;
        this.weight = Math.clamp(weight, 1, WEIGHT_CAP);
        this.maxLevel = Math.clamp(maxLevel, 1, LEVEL_CAP);
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.anvilCost = anvilCost;
        this.supportedItemSet = supportedItems;
        this.primaryItemSet = primaryItems;
        this.exclusiveSet = exclusiveSet;
    }

    @NotNull
    public static Builder builder(@NotNull String name, int maxLevel) {
        return new Builder(name, maxLevel);
    }

    @NotNull
    public static EnchantDefinition read(@NotNull FileConfig config, @NotNull String path, @NotNull ItemSetRegistry itemSetRegistry) throws IllegalStateException {
        String displayName = ConfigValue.create(path + ".DisplayName",
            "null",
            "Enchantment display name.",
            "https://docs.advntr.dev/minimessage/format.html#standard-tags",
            "[*] Only one color and decorations are allowed.",
            "[*] Reboot required when changed."
        ).read(config);

        List<String> description = ConfigValue.create(path + ".Description",
            Collections.emptyList(),
            "Enchantment description.",
            "[*] Reboot required when changed."
        ).read(config);

        int weight = ConfigValue.create(path + ".Weight",
            5,
            "Weight affects the chance of getting an enchantment from enchanting or loots.",
            "Value between 1 and " + WEIGHT_CAP + " (inclusive).",
            "[*] Reboot required when changed."
        ).read(config);

        int maxLevel = ConfigValue.create(path + ".MaxLevel",
            3,
            "The maximum level of this enchantment.",
            "Value between 1 and " + LEVEL_CAP + " (inclusive).",
            "[*] Reboot required when changed."
        ).read(config);

        EnchantCost minCost = ConfigValue.create(path + ".MinCost", EnchantCost::read,
            new EnchantCost(0, 0),
            "The minimum possible cost of this enchantment in levels.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            "[*] Reboot required when changed."
        ).read(config);

        EnchantCost maxCost = ConfigValue.create(path + ".MaxCost", EnchantCost::read,
            new EnchantCost(0, 0),
            "The maximum possible cost of this enchantment in levels.",
            "Explanation: https://minecraft.wiki/w/Enchanting_mechanics#How_enchantments_are_chosen",
            "Vanilla costs: https://minecraft.wiki/w/Enchanting/Levels",
            "[*] Reboot required when changed."
        ).read(config);

        int anvilCost = ConfigValue.create(path + ".AnvilCost",
            1,
            "The base cost when applying this enchantment to another item using an anvil. Halved when adding using a book, multiplied by the level of the enchantment.",
            "[*] Reboot required when changed."
        ).read(config);

        String supportedItemsId = ConfigValue.create(path + ".SupportedItems",
            "null",
            "Items on which this enchantment can be applied using an anvil or using the /enchant command.",
            EnchantsPlaceholders.WIKI_ITEM_SETS,
            "[*] Reboot required when changed."
        ).read(config);

        String primaryItemsId = ConfigValue.create(path + ".PrimaryItems",
            "null",
            "Items for which this enchantment appears in an enchanting table.",
            EnchantsPlaceholders.WIKI_ITEM_SETS,
            "[*] Reboot required when changed."
        ).read(config);

        ItemSet supportedItems = itemSetRegistry.getByKey(supportedItemsId);
        if (supportedItems == null) throw new IllegalStateException("Invalid supported set");

        ItemSet primaryItems = itemSetRegistry.getByKey(primaryItemsId);
        if (primaryItems == null) throw new IllegalStateException("Invalid primary items set");

        Set<String> conflicts = ConfigValue.create(path + ".Exclusives",
            Collections.emptySet(),
            "Enchantments that are incompatible with this enchantment.",
            "[*] Vanilla enchantments must be specified as: 'minecraft:enchant_name'.",
            "[*] Excellent enchantments must be specified as: '%s.".formatted(EnchantsKeys.create("enchant_name")),
            "    If custom namespace is disabled, use the vanilla (minecraft) one.",
            "[*] Reboot required when changed."
        ).read(config);

        return new EnchantDefinition(displayName, description, weight, maxLevel, minCost, maxCost, anvilCost, supportedItems, primaryItems, conflicts);
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".DisplayName", this.displayName);
        config.set(path + ".Description", this.description);
        config.set(path + ".Weight", this.weight);
        config.set(path + ".MaxLevel", this.maxLevel);
        config.set(path + ".MinCost", this.minCost);
        config.set(path + ".MaxCost", this.maxCost);
        config.set(path + ".AnvilCost", this.anvilCost);
        config.set(path + ".SupportedItems", this.supportedItemSet.getId());
        config.set(path + ".PrimaryItems", this.primaryItemSet.getId());
        config.set(path + ".Exclusives", this.exclusiveSet);
    }

    @NotNull
    public String getDisplayName() {
        return this.displayName;
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    public int getWeight() {
        return this.weight;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    @NotNull
    public EnchantCost getMinCost() {
        return this.minCost;
    }

    @NotNull
    public EnchantCost getMaxCost() {
        return this.maxCost;
    }

    public int getAnvilCost() {
        return this.anvilCost;
    }

    @NotNull
    public ItemSet getSupportedItemSet() {
        return this.supportedItemSet;
    }

    @NotNull
    public ItemSet getPrimaryItemSet() {
        return this.primaryItemSet;
    }

    public boolean hasConflicts() {
        return this.exclusiveSet != null && !this.exclusiveSet.isEmpty();
    }

    @NotNull
    public Set<String> getExclusiveSet() {
        return this.exclusiveSet;
    }

    public static class Builder {

        private String       displayName;
        private List<String> description;
        private int          weight;
        private int          maxLevel;
        private EnchantCost  minCost;
        private EnchantCost  maxCost;
        private int          anvilCost;
        private ItemSet      primaryItemSet;
        private ItemSet      supportedItemSet;
        private Set<String>  exclusives;

        public Builder(@NotNull String displayName, int maxLevel) {
            this.displayName(displayName);
            this.description = new ArrayList<>();
            this.weight = 5;
            this.maxLevel = maxLevel;
            this.anvilCost = Randomizer.nextInt(8);
            this.exclusives = new HashSet<>();

            int costCap = Randomizer.nextInt(45, 65);
            int costPerLevel = (int) ((double) costCap / (double) maxLevel);

            int minCost = Randomizer.nextInt(5);
            int maxCost = maxLevel == 1 ? costPerLevel : Randomizer.nextInt(3, 7) + minCost;

            this.minCost = new EnchantCost(minCost, costPerLevel);
            this.maxCost = new EnchantCost(maxCost, costPerLevel);
        }

        @NotNull
        public EnchantDefinition build() {
            Objects.requireNonNull(this.supportedItemSet, "Enchantments must have supported items set");

            return new EnchantDefinition(
                this.displayName,
                this.description,
                this.weight,
                this.maxLevel,
                this.minCost,
                this.maxCost,
                this.anvilCost,
                this.supportedItemSet,
                this.primaryItemSet == null ? this.supportedItemSet : this.primaryItemSet,
                this.exclusives
            );
        }

        @NotNull
        public Builder displayName(@NotNull String displayName) {
            this.displayName = TagWrappers.COLOR.with("#279CF5").wrap(displayName);
            return this;
        }

        @NotNull
        public Builder description(@NotNull String... description) {
            return this.description(Lists.newList(description));
        }

        @NotNull
        public Builder description(@NotNull List<String> description) {
            this.description = Lists.modify(description, TagWrappers.GRAY::wrap);
            return this;
        }

        @NotNull
        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        @NotNull
        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        @NotNull
        public Builder cost(@NotNull EnchantCost minCost, @NotNull EnchantCost maxCost) {
            this.minCost = minCost;
            this.maxCost = maxCost;
            return this;
        }

        @NotNull
        public Builder anvilCost(int anvilCost) {
            this.anvilCost = anvilCost;
            return this;
        }

        @NotNull
        public Builder items(@NotNull ItemSetDefaults defaults) {
            this.primaryItemSet = defaults.getItemSet();
            this.supportedItemSet = defaults.getItemSet();
            return this;
        }

        @NotNull
        public Builder primaryItems(@NotNull ItemSetDefaults defaults) {
            this.primaryItemSet = defaults.getItemSet();
            return this;
        }

        @NotNull
        public Builder supportedItems(@NotNull ItemSetDefaults defaults) {
            this.supportedItemSet = defaults.getItemSet();
            return this;
        }

        @NotNull
        public Builder exclusives(@NotNull NamespacedKey... exclusives) {
            return this.exclusives(Lists.newSet(exclusives));
        }

        @NotNull
        public Builder exclusives(@NotNull Set<NamespacedKey> exclusives) {
            this.exclusives = Lists.modify(exclusives, BukkitThing::getAsString);
            return this;
        }
    }
}
