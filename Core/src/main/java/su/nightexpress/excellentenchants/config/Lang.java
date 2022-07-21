package su.nightexpress.excellentenchants.config;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.lang.EngineLang;

public class Lang extends EngineLang {

    public static final LangKey COMMAND_LIST_DESC = new LangKey("Command.List.Desc", "List of all custom enchantments.");

    public static final LangKey COMMAND_ENCHANT_USAGE = new LangKey("Command.Enchant.Usage", "<enchant> <level>");
    public static final LangKey COMMAND_ENCHANT_DESC = new LangKey("Command.Enchant.Desc", "Enchants the item in your hand.");
    public static final LangKey COMMAND_ENCHANT_DONE = new LangKey("Command.Enchant.Done", "&aSuccessfully enchanted!");

    public static final LangKey COMMAND_BOOK_USAGE = new LangKey("Command.Book.Usage", "<player> <enchant> <level>");
    public static final LangKey COMMAND_BOOK_DESC = new LangKey("Command.Book.Desc", "Gives custom enchanted book.");
    public static final LangKey COMMAND_BOOK_DONE = new LangKey("Command.Book.Done", "Given &6%enchant%&7 enchanted book to &6%player%&7.");

    public static final LangKey COMMAND_TIER_BOOK_USAGE = new LangKey("Command.TierBook.Usage", "<player> <tier> <level>");
    public static final LangKey COMMAND_TIER_BOOK_DESC  = new LangKey("Command.TierBook.Desc", "Gives an enchanted book.");
    public static final LangKey COMMAND_TIER_BOOK_ERROR = new LangKey("Command.TierBook.Error", "&cInvalid tier!");
    public static final LangKey COMMAND_TIER_BOOK_DONE  = new LangKey("Command.TierBook.Done", "Given &6%tier%&7 enchanted book to &6%player%&7.");

    public static final LangKey ERROR_NO_ENCHANT = new LangKey("Error.NoEnchant", "&cNo such enchant.");

}
