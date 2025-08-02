package su.nightexpress.excellentenchants.config;

import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;
import static su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers.*;

public class Lang extends CoreLang {

    public static final LangString COMMAND_ARGUMENT_NAME_LEVEL = LangString.of("Command.Argument.Name.Level", "level");
    public static final LangString COMMAND_ARGUMENT_NAME_SLOT  = LangString.of("Command.Argument.Name.Slot", "slot");

    public static final LangString COMMAND_LIST_DESC        = LangString.of("Command.List.Desc", "List of custom enchantments.");
    public static final LangString COMMAND_ENCHANT_DESC     = LangString.of("Command.Enchant.Desc", "Enchant item in specific slot.");
    public static final LangString COMMAND_DISENCHANT_DESC  = LangString.of("Command.Disenchant.Desc", "Disenchant item in specific slot.");
    public static final LangString COMMAND_BOOK_DESC        = LangString.of("Command.Book.Desc", "Give a book with specific enchantment.");
    public static final LangString COMMAND_RANDOM_BOOK_DESC = LangString.of("Command.RandomBook.Desc", "Give a book with random enchantment.");
    public static final LangString COMMAND_GIVE_FUEL_DESC   = LangString.of("Command.GiveFuel.Desc", "Give enchantment fuel item.");

    public static final LangText COMMAND_LIST_DONE_OTHERS = LangText.of("Command.List.DoneOthers",
        GRAY.wrap("Opened enchantments GUI for " + SOFT_YELLOW.wrap(PLAYER_NAME) + ".")
    );


    public static final LangText COMMAND_ENCHANT_DONE_SELF = LangText.of("Command.Enchant.Done.Self",
        GRAY.wrap(SOFT_YELLOW.wrap(GENERIC_ITEM) + " enchanted with " + SOFT_YELLOW.wrap(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_ENCHANT_DONE_OTHERS = LangText.of("Command.Enchant.Done.Others",
        GRAY.wrap(SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "'s " + SOFT_YELLOW.wrap(GENERIC_ITEM) + " enchanted with " + SOFT_YELLOW.wrap(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_DISENCHANT_DONE_SELF = LangText.of("Command.Disenchant.Done.Self",
        GRAY.wrap(SOFT_YELLOW.wrap(GENERIC_ITEM) + " disenchanted from " + SOFT_YELLOW.wrap(GENERIC_ENCHANT) + "!")
    );

    public static final LangText COMMAND_DISENCHANT_DONE_OTHERS = LangText.of("Command.Disenchant.Done.Others",
        GRAY.wrap(SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "'s " + SOFT_YELLOW.wrap(GENERIC_ITEM) + " disenchanted from " + SOFT_YELLOW.wrap(GENERIC_ENCHANT) + "!")
    );

    public static final LangText COMMAND_ENCHANT_ERROR_NO_ITEM = LangText.of("Command.Enchant.Error.NoItem",
        SOFT_RED.wrap("There is no item to enchant!")
    );



    public static final LangText ENCHANTED_BOOK_GAVE = LangText.of("Command.Book.Done",
        GRAY.wrap("You gave " + SOFT_YELLOW.wrap(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + " book to " + SOFT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + ".")
    );

    public static final LangText CHARGES_FUEL_GAVE = LangText.of("Charges.Fuel.Gave",
        GRAY.wrap("You gave " + SOFT_YELLOW.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_NAME) + " fuel to " + SOFT_YELLOW.wrap(PLAYER_NAME) + ".")
    );

    public static final LangText CHARGES_FUEL_BAD_ENCHANTMENT = LangText.of("Charges.Fuel.BadEnchantment",
        GRAY.wrap("Enchantment " + SOFT_RED.wrap(GENERIC_NAME) + " can't be charged.")
    );



    public static final LangText ERROR_COMMAND_INVALID_SLOT_ARGUMENT = LangText.of("Error.Command.Argument.InvalidSlot",
        GRAY.wrap(SOFT_RED.wrap(GENERIC_VALUE) + " is not a valid slot!")
    );
}
