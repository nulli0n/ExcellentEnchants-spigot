package su.nightexpress.excellentenchants.api.wrapper;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantId;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.config.ConfigBridge;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.Writeable;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnchantDefinition implements Writeable {

    private final String       displayName;
    private final List<String> description;
    private final int          weight;
    private final int          maxLevel;
    private final EnchantCost  minCost;
    private final EnchantCost  maxCost;
    private final int          anvilCost;
    private final String       primaryItemsId;
    private final String       supportedItemsId;
    private final Set<String>  exclusiveSet;

    public EnchantDefinition(@NotNull String displayName,
                             @NotNull List<String> description,
                             int weight,
                             int maxLevel,
                             EnchantCost minCost,
                             EnchantCost maxCost,
                             int anvilCost,
                             @NotNull String supportedItems,
                             @NotNull String primaryItems,
                             @NotNull Set<String> exclusiveSet) {
        this.displayName = displayName;
        this.description = description;
        this.weight = weight;
        this.maxLevel = Math.clamp(maxLevel, 1, ConfigBridge.LEVEL_CAP);
        this.minCost = minCost;
        this.maxCost = maxCost;
        this.anvilCost = anvilCost;
        this.supportedItemsId = supportedItems;
        this.primaryItemsId = primaryItems;
        this.exclusiveSet = exclusiveSet;
    }

    @NotNull
    public static Builder builder(@NotNull String name, int maxLevel) {
        return new Builder(name, maxLevel);
    }

    @NotNull
    public static EnchantDefinition read(@NotNull FileConfig config, @NotNull String path) {
        String displayName = ConfigValue.create(path + ".DisplayName",
            "null",
            "Enchantment display name.",
            "https://docs.advntr.dev/minimessage/format.html#standard-tags",
            "[*] Reboot required when changed."
        ).read(config);

        List<String> description = ConfigValue.create(path + ".Description",
            Lists.newList(),
            "Enchantment description.",
            "[*] Reboot required when changed."
        ).read(config);

        int weight = ConfigValue.create(path + ".Weight",
            5,
            "Weight affects the chance of getting an enchantment from enchanting or loots.",
            "[*] Reboot required when changed."
        ).read(config);

        int maxLevel = ConfigValue.create(path + ".MaxLevel",
            3,
            "The maximum level of this enchantment.",
            "Value between 1 and " + ConfigBridge.LEVEL_CAP + " (inclusive).",
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

        String supportedItems = ConfigValue.create(path + ".SupportedItems",
            "null",
            "Items on which this enchantment can be applied using an anvil or using the /enchant command.",
            EnchantsPlaceholders.WIKI_ITEM_SETS,
            "[*] IF INVALID ITEM SET PROVIDED, THE ENCHANTMENT WILL NOT LOAD!",
            "[*] Reboot required when changed."
        ).read(config);

        String primaryItems = ConfigValue.create(path + ".PrimaryItems",
            "null",
            "Items for which this enchantment appears in an enchanting table.",
            EnchantsPlaceholders.WIKI_ITEM_SETS,
            "[*] IF INVALID ITEM SET PROVIDED, THE ENCHANTMENT WILL NOT LOAD!",
            "[*] Reboot required when changed."
        ).read(config);

        Set<String> conflicts = ConfigValue.create(path + ".Exclusives",
            Lists.newSet(),
            "Enchantments that are incompatible with this enchantment.",
            "[*] Vanilla enchantments must be specified with the 'minecraft:' namespace: 'minecraft:sharpness'.",
            "[*] Excellent enchantments must be specified with the '" + ConfigBridge.NAMESPACE + ":' namespace: '" + ConfigBridge.NAMESPACE + ":" + EnchantId.ICE_ASPECT + "'.",
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
        config.set(path + ".SupportedItems", this.supportedItemsId);
        config.set(path + ".PrimaryItems", this.primaryItemsId);
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
    public String getSupportedItemsId() {
        return this.supportedItemsId;
    }

    @NotNull
    public String getPrimaryItemsId() {
        return this.primaryItemsId;
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
        private int         maxLevel;
        private EnchantCost minCost;
        private EnchantCost maxCost;
        private int         anvilCost;
        private String       primaryItemsId;
        private String       supportedItemsId;
        private Set<String>  exclusives;

        public Builder(@NotNull String displayName, int maxLevel) {
            this.displayName = displayName;
            this.description = new ArrayList<>();
            this.weight = 5;
            this.maxLevel = maxLevel;
            this.anvilCost = Rnd.get(8) + 1;
            this.primaryItemsId = "null";
            this.supportedItemsId = "null";
            this.exclusives = new HashSet<>();

            int costCap = Rnd.get(45, 65) + 1;
            int costPerLevel = (int) ((double) costCap / (double) maxLevel);

            int minCost = Rnd.nextInt(5) + 1;
            int maxCost = maxLevel == 1 ? costPerLevel : Rnd.get(3, 7) + minCost;

            this.minCost = new EnchantCost(minCost, costPerLevel);
            this.maxCost = new EnchantCost(maxCost, costPerLevel);
        }

        @NotNull
        public EnchantDefinition build() {
            return new EnchantDefinition(
                this.displayName,
                this.description,
                this.weight,
                this.maxLevel,
                this.minCost,
                this.maxCost,
                this.anvilCost,
                this.supportedItemsId,
                this.primaryItemsId,
                this.exclusives
            );
        }

        @NotNull
        public Builder displayName(@NotNull String displayName) {
            this.displayName = displayName;
            return this;
        }

        @NotNull
        public Builder description(@NotNull String... description) {
            return this.description(Lists.newList(description));
        }

        @NotNull
        public Builder description(@NotNull List<String> description) {
            this.description = description;
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
        public Builder items(@NotNull String category) {
            this.primaryItemsId = category;
            this.supportedItemsId = category;
            return this;
        }

        @NotNull
        public Builder primaryItems(@NotNull String primaryItemsId) {
            this.primaryItemsId = primaryItemsId;
            return this;
        }

        @NotNull
        public Builder supportedItems(@NotNull String supportedItemsId) {
            this.supportedItemsId = supportedItemsId;
            return this;
        }

//        @NotNull
//        @Deprecated
//        public Builder exclusives(@NotNull String... exclusives) {
//            return this.exclusives(Lists.newSet(exclusives));
//        }

//        @NotNull
//        @Deprecated
//        public Builder exclusives(@NotNull Set<String> exclusives) {
//            this.exclusives = exclusives;
//            return this;
//        }

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
