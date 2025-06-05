package su.nightexpress.excellentenchants.config;

import su.nightexpress.nightcore.core.CoreLang;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;

import static su.nightexpress.excellentenchants.api.EnchantsPlaceholders.*;
import static su.nightexpress.nightcore.util.text.tag.Tags.*;

public class Lang extends CoreLang {

    public static final LangString COMMAND_ARGUMENT_NAME_LEVEL  = LangString.of("Command.Argument.Name.Level", "level");
    public static final LangString COMMAND_ARGUMENT_NAME_SLOT   = LangString.of("Command.Argument.Name.Slot", "slot");

    public static final LangString COMMAND_LIST_DESC        = LangString.of("Command.List.Desc", "List of custom enchantments.");
    public static final LangString COMMAND_ENCHANT_DESC     = LangString.of("Command.Enchant.Desc", "Enchant item in specific slot.");
    public static final LangString COMMAND_DISENCHANT_DESC  = LangString.of("Command.Disenchant.Desc", "Disenchant item in specific slot.");
    public static final LangString COMMAND_BOOK_DESC        = LangString.of("Command.Book.Desc", "Give a book with specific enchantment.");
    public static final LangString COMMAND_GET_FUEL_DESC    = LangString.of("Command.GetFuel.Desc", "Get enchantment fuel item.");

    public static final LangText COMMAND_LIST_DONE_OTHERS = LangText.of("Command.List.DoneOthers",
        LIGHT_GRAY.wrap("Opened enchantments GUI for " + LIGHT_YELLOW.wrap(PLAYER_NAME) + ".")
    );


    public static final LangText COMMAND_GET_FUEL_DONE = LangText.of("Command.GetFuel.Done",
        LIGHT_GRAY.wrap("You got " + LIGHT_YELLOW.wrap("x" + GENERIC_AMOUNT + " " + GENERIC_NAME) + ".")
    );

    public static final LangText COMMAND_GET_FUEL_ERROR_NO_CHARGES = LangText.of("Command.GetFuel.Error.NoCharges",
        LIGHT_GRAY.wrap("Enchantment " + LIGHT_RED.wrap(GENERIC_NAME) + " don't have charges.")
    );


    public static final LangText COMMAND_ENCHANT_DONE_SELF = LangText.of("Command.Enchant.Done.Self",
        LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.wrap(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_ENCHANT_DONE_OTHERS = LangText.of("Command.Enchant.Done.Others",
        LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "'s " + LIGHT_YELLOW.wrap(GENERIC_ITEM) + " enchanted with " + LIGHT_YELLOW.wrap(GENERIC_ENCHANT + " " + GENERIC_LEVEL) + "!")
    );

    public static final LangText COMMAND_DISENCHANT_DONE_SELF = LangText.of("Command.Disenchant.Done.Self",
        LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap(GENERIC_ITEM) + " disenchanted from " + LIGHT_YELLOW.wrap(GENERIC_ENCHANT) + "!")
    );

    public static final LangText COMMAND_DISENCHANT_DONE_OTHERS = LangText.of("Command.Disenchant.Done.Others",
        LIGHT_GRAY.wrap(LIGHT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + "'s " + LIGHT_YELLOW.wrap(GENERIC_ITEM) + " disenchanted from " + LIGHT_YELLOW.wrap(GENERIC_ENCHANT) + "!")
    );

    public static final LangText COMMAND_ENCHANT_ERROR_NO_ITEM = LangText.of("Command.Enchant.Error.NoItem",
        LIGHT_RED.wrap("There is no item to enchant!")
    );

    public static final LangText COMMAND_BOOK_DONE = LangText.of("Command.Book.Done",
        LIGHT_GRAY.wrap("Given " + LIGHT_YELLOW.wrap(GENERIC_ENCHANT) + " enchanted book to " + LIGHT_YELLOW.wrap(PLAYER_DISPLAY_NAME) + ".")
    );


    public static final LangText ERROR_COMMAND_INVALID_SLOT_ARGUMENT = LangText.of("Error.Command.Argument.InvalidSlot",
        LIGHT_GRAY.wrap(LIGHT_RED.wrap(GENERIC_VALUE) + " is not a valid slot!")
    );
}
