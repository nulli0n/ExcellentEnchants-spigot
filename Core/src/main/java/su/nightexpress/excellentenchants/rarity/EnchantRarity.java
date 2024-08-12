package su.nightexpress.excellentenchants.rarity;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.StringUtil;

import java.util.List;

import static su.nightexpress.nightcore.util.text.tag.Tags.*;
import static su.nightexpress.excellentenchants.Placeholders.*;

public class EnchantRarity implements Rarity {

    public static final String COMMON    = "common";
    public static final String RARE      = "uncommon";
    public static final String LEGENDARY = "rare";
    public static final String MYTHIC    = "very_rare";

    public static final Rarity DUMMY = new EnchantRarity("dummy", "Default", ENCHANTMENT_NAME, 5);

    private final String id;
    private final String name;
    private final String nameFormat;
    private final int    weight;

    public EnchantRarity(@NotNull String id, @NotNull String name, @NotNull String nameFormat, int weight) {
        this.id = id;
        this.name = name;
        this.nameFormat = nameFormat;
        this.weight = weight;
    }

    @NotNull
    public static EnchantRarity read(@NotNull FileConfig config, @NotNull String path, @NotNull String id) {
        String name = ConfigValue.create(path + ".Name", StringUtil.capitalizeUnderscored(id)).read(config);

        String nameFormat = ConfigValue.create(path + ".Name_Format",
            Placeholders.ENCHANTMENT_NAME,
            "Sets general name format for all enchantments of this rarity.",
            "You can use 'Enchantment' placeholders: " + Placeholders.WIKI_PLACEHOLDERS,
            "Use only 'static' placeholders! Level-dependant and charges placeholders will not update their values.",
            "Text formations: " + Placeholders.WIKI_TEXT_URL,
            "[*] Reboot required when changed!"
        ).read(config);

        int weight = ConfigValue.create(path + ".Weight",
            10,
            "Controls the probability of enchantment with this rarity when enchanting",
            "The probability is determined 'weight / total weight * 100%', where 'total_weight' is the sum of the weights of all available enchantments.",
            "[*] Reboot required when changed!"
        ).read(config);

        return new EnchantRarity(id, name, nameFormat, weight);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        config.set(path + ".Name", this.getName());
        config.set(path + ".Name_Format", this.getNameFormat());
        config.set(path + ".Weight", this.getWeight());
    }

    @NotNull
    public static List<Rarity> getDefaults() {
        return Lists.newList(
            new EnchantRarity(COMMON, LIGHT_GRAY.enclose("Common"), LIGHT_GRAY.enclose(ENCHANTMENT_NAME), 10),
            new EnchantRarity(RARE, LIGHT_GREEN.enclose("Rare"), LIGHT_GREEN.enclose(ENCHANTMENT_NAME), 5),
            new EnchantRarity(LEGENDARY, LIGHT_YELLOW.enclose("Legendary"),  LIGHT_YELLOW.enclose(ENCHANTMENT_NAME), 2),
            new EnchantRarity(MYTHIC, LIGHT_PURPLE.enclose("Mythic"), LIGHT_PURPLE.enclose(ENCHANTMENT_NAME), 1)
        );
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public String getNameFormat() {
        return nameFormat;
    }

    @Override
    public int getWeight() {
        return weight;
    }
}
