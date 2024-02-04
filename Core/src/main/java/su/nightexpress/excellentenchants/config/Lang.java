package su.nightexpress.excellentenchants.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;

import static su.nexmedia.engine.utils.Colors2.*;
import static su.nightexpress.excellentenchants.Placeholders.*;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC = LangKey.of("Command.List.Desc", "List of all custom enchantments.");

    public static final LangKey COMMAND_ENCHANT_USAGE         = LangKey.of("Command.Enchant.Usage", "<enchant> <level> [player] [slot]");
    public static final LangKey COMMAND_ENCHANT_DESC          = LangKey.of("Command.Enchant.Desc", "Enchants the item in your hand.");
    public static final LangKey COMMAND_ENCHANT_DONE_SELF     = LangKey.of("Command.Enchant.Done.Self", LIGHT_ORANGE + GENERIC_ITEM + LIGHT_YELLOW + " enchanted with " + GENERIC_ENCHANT + " " + GENERIC_LEVEL + LIGHT_YELLOW + "!");
    public static final LangKey COMMAND_ENCHANT_DONE_OTHERS   = LangKey.of("Command.Enchant.Done.Others", LIGHT_ORANGE + PLAYER_DISPLAY_NAME + LIGHT_YELLOW + "'s " + LIGHT_ORANGE + GENERIC_ITEM + LIGHT_YELLOW + " enchanted with " + GENERIC_ENCHANT + " " + GENERIC_LEVEL + LIGHT_YELLOW + "!");
    public static final LangKey COMMAND_ENCHANT_ERROR_NO_ITEM = LangKey.of("Command.Enchant.Error.NoItem", RED + "There is no item to enchant!");

    public static final LangKey COMMAND_BOOK_USAGE = LangKey.of("Command.Book.Usage", "<player> <enchant> <level>");
    public static final LangKey COMMAND_BOOK_DESC  = LangKey.of("Command.Book.Desc", "Gives custom enchanted book.");
    public static final LangKey COMMAND_BOOK_DONE  = LangKey.of("Command.Book.Done", LIGHT_YELLOW + "Given " + LIGHT_ORANGE + GENERIC_ENCHANT + LIGHT_YELLOW + " enchanted book to " + LIGHT_ORANGE + PLAYER_DISPLAY_NAME + LIGHT_YELLOW + ".");

    public static final LangKey COMMAND_TIER_BOOK_USAGE = LangKey.of("Command.TierBook.Usage", "<player> <tier> <level>");
    public static final LangKey COMMAND_TIER_BOOK_DESC  = LangKey.of("Command.TierBook.Desc", "Gives an enchanted book.");
    public static final LangKey COMMAND_TIER_BOOK_ERROR = LangKey.of("Command.TierBook.Error", RED + "Invalid tier!");
    public static final LangKey COMMAND_TIER_BOOK_DONE  = LangKey.of("Command.TierBook.Done", LIGHT_YELLOW + "Given " + LIGHT_ORANGE + TIER_NAME + LIGHT_YELLOW + " enchanted book to " + LIGHT_ORANGE + PLAYER_DISPLAY_NAME + LIGHT_YELLOW + ".");

    public static final LangKey ERROR_NO_ENCHANT = LangKey.of("Error.NoEnchant", RED + "Invalid enchantment.");

}
