package su.nightexpress.excellentenchants.config;

import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.excellentenchants.Placeholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Lang extends CoreLang {

    public static final LangString COMMAND_ARGUMENT_NAME_LEVEL = LangString.of("Command.Argument.Name.Level", "level");
    public static final LangString COMMAND_ARGUMENT_NAME_SLOT = LangString.of("Command.Argument.Name.Slot", "slot");
    public static final LangString COMMAND_ARGUMENT_NAME_RARITY = LangString.of("Command.Argument.Name.Rarity", "rarity");

    public static final LangString COMMAND_LIST_DESC        = LangString.of("Command.List.Desc", "List of custom enchantments.");
    public static final LangString COMMAND_ENCHANT_DESC     = LangString.of("Command.Enchant.Desc", "Enchant item in specific slot.");
    public static final LangString COMMAND_BOOK_DESC        = LangString.of("Command.Book.Desc", "Give a book with specific enchantment.");
    public static final LangString COMMAND_RARITY_BOOK_DESC = LangString.of("Command.RarityBook.Desc", "Give a book with enchantment of specific rarity.");
    public static final LangString COMMAND_GET_FUEL_DESC    = LangString.of("Command.GetFuel.Desc", "Get enchantment fuel item.");

    public static final LangText COMMAND_LIST_DONE_OTHERS = LangText.of("Command.List.DoneOthers",
        LIGHT_GRAY.enclose("Opened enchantments GUI for " + LIGHT_YELLOW.enclose(PLAYER_NAME) + ".")
    );


    public static final LangText COMMAND_GET_FUEL_DONE = LangText.of("Command.GetFuel.Done",
        LIGHT_GRAY.enclose("You got " + LIGHT_YELLOW.enclose("x" + GENERIC_AMOUNT + " " + GENERIC_NAME) + ".")
    );

    public static final LangText COMMAND_GET_FUEL_ERROR_NO_CHARGES = LangText.of("Command.GetFuel.Error.NoCharges",
        LIGHT_GRAY.enclose("Enchantment " + LIGHT_RED.enclose(GENERIC_NAME) + " don't have charges.")
    );


    public static final LangText COMMAND_ENCHANT_DONE_SELF = LangText.of("Command.Enchant.Done.Self",
        LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_ENCHANT_DONE_OTHERS = LangText.of("Command.Enchant.Done.Others",
        LIGHT_GRAY.enclose(LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + "'s " + LIGHT_YELLOW.enclose(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_ENCHANT_ERROR_NO_ITEM = LangText.of("Command.Enchant.Error.NoItem",
        LIGHT_RED.enclose("There is no item to enchant!")
    );


    public static final LangText COMMAND_BOOK_DONE = LangText.of("Command.Book.Done",
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_ENCHANT) + " enchanted book to " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + ".")
    );


    public static final LangText COMMAND_RARITY_BOOK_DONE = LangText.of("Command.RarityBook.Done",
        LIGHT_GRAY.enclose("Given " + LIGHT_YELLOW.enclose(GENERIC_NAME) + " enchanted book to " + LIGHT_YELLOW.enclose(PLAYER_DISPLAY_NAME) + ".")
    );

    public static final LangText COMMAND_RARITY_BOOK_ERROR_EMPTY = LangText.of("Command.RarityBook.Error.Empty",
        LIGHT_GRAY.enclose("There is no enchantments with the " + LIGHT_RED.enclose(GENERIC_NAME) + " rarity!")
    );



    public static final LangText ERROR_COMMAND_INVALID_RARITY_ARGUMENT = LangText.of("Error.Command.Argument.InvalidRarity",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose(GENERIC_VALUE) + " is not a valid rarity!")
    );

    public static final LangText ERROR_COMMAND_INVALID_SLOT_ARGUMENT = LangText.of("Error.Command.Argument.InvalidSlot",
        LIGHT_GRAY.enclose(LIGHT_RED.enclose(GENERIC_VALUE) + " is not a valid slot!")
    );

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
