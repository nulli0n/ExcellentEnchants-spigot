package su.nightexpress.excellentenchants.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;

import static su.nexmedia.engine.utils.Colors.*;
import static su.nightexpress.excellentenchants.Placeholders.*;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC = LangKey.of("Command.List.Desc", "List of all custom enchantments.");

    public static final LangKey COMMAND_ENCHANT_USAGE         = LangKey.of("Command.Enchant.Usage", "<enchant> <level>");
    public static final LangKey COMMAND_ENCHANT_DESC          = LangKey.of("Command.Enchant.Desc", "Enchants the item in your hand.");
    public static final LangKey COMMAND_ENCHANT_DONE          = LangKey.of("Command.Enchant.Done", LIGHT_YELLOW + "Successfully enchanted!");
    public static final LangKey COMMAND_ENCHANT_ERROR_NO_ITEM = LangKey.of("Command.Enchant.Error.NoItem", RED + "You must hold an item to enchant it!");

    public static final LangKey COMMAND_BOOK_USAGE = LangKey.of("Command.Book.Usage", "<player> <enchant> <level>");
    public static final LangKey COMMAND_BOOK_DESC  = LangKey.of("Command.Book.Desc", "Gives custom enchanted book.");
    public static final LangKey COMMAND_BOOK_DONE  = LangKey.of("Command.Book.Done", LIGHT_YELLOW + "Given " + ORANGE + GENERIC_ENCHANT + LIGHT_YELLOW + " enchanted book to " + ORANGE + PLAYER_DISPLAY_NAME + LIGHT_YELLOW + ".");

    public static final LangKey COMMAND_TIER_BOOK_USAGE = LangKey.of("Command.TierBook.Usage", "<player> <tier> <level>");
    public static final LangKey COMMAND_TIER_BOOK_DESC  = LangKey.of("Command.TierBook.Desc", "Gives an enchanted book.");
    public static final LangKey COMMAND_TIER_BOOK_ERROR = LangKey.of("Command.TierBook.Error", RED + "Invalid tier!");
    public static final LangKey COMMAND_TIER_BOOK_DONE  = LangKey.of("Command.TierBook.Done", LIGHT_YELLOW + "Given " + ORANGE + TIER_NAME + LIGHT_YELLOW + " enchanted book to " + ORANGE + PLAYER_DISPLAY_NAME + LIGHT_YELLOW + ".");

    public static final LangKey ERROR_NO_ENCHANT = LangKey.of("Error.NoEnchant", RED + "Invalid enchantment.");

}
