package su.nightexpress.excellentenchants.config;

import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Lang extends CoreLang {

    public static final LangString COMMAND_LIST_DESC = LangString.of("Command.List.Desc",
        "List of all custom enchantments.");

    public static final LangString COMMAND_LIST_USAGE = LangString.of("Command.List.Usage",
        "[player]");

    public static final LangText COMMAND_LIST_DONE_OTHERS = LangText.of("Command.List.DoneOthers",
        LIGHT_GRAY.enclose("Opened enchantments GUI for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + "."));


    public static final LangString COMMAND_GET_FUEL_DESC = LangString.of("Command.GetFuel.Desc",
        "Get enchantment fuel item.");

    public static final LangString COMMAND_GET_FUEL_USAGE = LangString.of("Command.GetFuel.Usage",
        "<enchant> [amount]");

    public static final LangText COMMAND_GET_FUEL_DONE = LangText.of("Command.GetFuel.Done",
        LIGHT_GRAY.enclose("You got " + LIGHT_YELLOW.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_NAME) + "."));


    public static final LangString COMMAND_ENCHANT_USAGE = LangString.of("Command.Enchant.Usage",
        "<enchant> <level> [player] [slot]");

    public static final LangString COMMAND_ENCHANT_DESC = LangString.of("Command.Enchant.Desc",
        "Enchants the item in your hand.");

    public static final LangText COMMAND_ENCHANT_DONE_SELF = LangText.of("Command.Enchant.Done.Self",
        LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!"));

    public static final LangText COMMAND_ENCHANT_DONE_OTHERS = LangText.of("Command.Enchant.Done.Others",
        LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + "'s " + LIGHT_YELLOW.enclose(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!"));

    public static final LangText COMMAND_ENCHANT_ERROR_NO_ITEM = LangText.of("Command.Enchant.Error.NoItem",
        RED.enclose("There is no item to enchant!"));


    public static final LangString COMMAND_BOOK_USAGE = LangString.of("Command.Book.Usage",
        "<player> <enchant> <level>");

    public static final LangString COMMAND_BOOK_DESC = LangString.of("Command.Book.Desc",
        "Gives custom enchanted book.");

    public static final LangText COMMAND_BOOK_DONE = LangText.of("Command.Book.Done",
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT) + " enchanted book to " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + "."));


    public static final LangString COMMAND_RARITY_BOOK_USAGE = LangString.of("Command.RarityBook.Usage",
        "<player> <tier> <level>");

    public static final LangString COMMAND_RARITY_BOOK_DESC = LangString.of("Command.RarityBook.Desc",
        "Give an enchanted book with enchantment of specified rarity.");

    public static final LangText COMMAND_RARITY_BOOK_DONE = LangText.of("Command.RarityBook.Done",
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_NAME) + " enchanted book to " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + "."));


    public static final LangText ERROR_INVALID_ENCHANT = LangText.of("Error.InvalidEnchantment",
        RED.enclose("Invalid enchantment."));

    public static final LangText ERROR_INVALID_RARITY = LangText.of("Error.InvalidRarity",
        RED.enclose("Invalid rarity!"));

    public static final LangString ITEM_CATEGORY_HELMET     = LangString.of("ItemCategory.Helmet", "Helmet");
    public static final LangString ITEM_CATEGORY_CHESTPLATE = LangString.of("ItemCategory.Chestplate", "Chestplate");
    public static final LangString ITEM_CATEGORY_LEGGINGS   = LangString.of("ItemCategory.Leggings", "Leggings");
    public static final LangString ITEM_CATEGORY_BOOTS      = LangString.of("ItemCategory.Boots", "Boots");
    public static final LangString ITEM_CATEGORY_ELYTRA     = LangString.of("ItemCategory.Elytra", "Elytra");

    public static final LangString ITEM_CATEGORY_SWORD       = LangString.of("ItemCategory.Sword", "Sword");
    public static final LangString ITEM_CATEGORY_AXE         = LangString.of("ItemCategory.Axe", "Axe");
    public static final LangString ITEM_CATEGORY_HOE         = LangString.of("ItemCategory.Hoe", "Hoe");
    public static final LangString ITEM_CATEGORY_PICKAXE     = LangString.of("ItemCategory.Pickaxe", "Pickaxe");
    public static final LangString ITEM_CATEGORY_SHOVEL      = LangString.of("ItemCategory.Shovel", "Shovel");
    public static final LangString ITEM_CATEGORY_TRIDENT     = LangString.of("ItemCategory.Trident", "Trident");
    public static final LangString ITEM_CATEGORY_BOW         = LangString.of("ItemCategory.Bow", "Bow");
    public static final LangString ITEM_CATEGORY_CROSSBOW    = LangString.of("ItemCategory.Crossbow", "Crossbow");
    public static final LangString ITEM_CATEGORY_FISHING_ROD = LangString.of("ItemCategory.FishingRod", "FishingRod");
    public static final LangString ITEM_CATEGORY_SHIELD      = LangString.of("ItemCategory.Shield", "Shield");

    public static final LangString ITEM_CATEGORY_BREAKABLE    = LangString.of("ItemCategory.Breakable", "Breakable");
    public static final LangString ITEM_CATEGORY_ARMOR        = LangString.of("ItemCategory.Armor", "Armor");
    public static final LangString ITEM_CATEGORY_TOOL         = LangString.of("ItemCategory.Tool", "Tool");
    public static final LangString ITEM_CATEGORY_WEAPON       = LangString.of("ItemCategory.Weapon", "Sword/Axe");
    public static final LangString ITEM_CATEGORY_BOWS         = LangString.of("ItemCategory.Bows", "Bow/Crossbow");
    public static final LangString ITEM_CATEGORY_TORSO        = LangString.of("ItemCategory.Torso", "Chestplate/Elytra");
    public static final LangString ITEM_CATEGORY_ALL_WEAPON   = LangString.of("ItemCategory.AllWeapon", "All Weapon");
    public static final LangString ITEM_CATEGORY_MINING_TOOLS = LangString.of("ItemCategory.MiningTools", "Mining Tools");


}
