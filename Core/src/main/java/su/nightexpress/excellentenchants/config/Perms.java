package su.nightexpress.excellentenchants.config;

import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class Perms {

    private static final String PREFIX         = "excellentenchants.";
    private static final String PREFIX_COMMAND = PREFIX + "command.";

    public static final UniPermission PLUGIN  = new UniPermission(PREFIX + EnchantsPlaceholders.WILDCARD);
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + EnchantsPlaceholders.WILDCARD);

    public static final UniPermission COMMAND_BOOK        = new UniPermission(PREFIX_COMMAND + "book");
    public static final UniPermission COMMAND_RANDOM_BOOK        = new UniPermission(PREFIX_COMMAND + "randombook");
    public static final UniPermission COMMAND_ENCHANT     = new UniPermission(PREFIX_COMMAND + "enchant");
    public static final UniPermission COMMAND_DISENCHANT = new UniPermission(PREFIX_COMMAND + "disenchant");
    public static final UniPermission COMMAND_GIVE_FUEL  = new UniPermission(PREFIX_COMMAND + "givefuel");
    public static final UniPermission COMMAND_LIST       = new UniPermission(PREFIX_COMMAND + "list");
    public static final UniPermission COMMAND_LIST_OTHERS = new UniPermission(PREFIX_COMMAND + "list.others");
    public static final UniPermission COMMAND_RARITY_BOOK = new UniPermission(PREFIX_COMMAND + "raritybook");
    public static final UniPermission COMMAND_RELOAD      = new UniPermission(PREFIX_COMMAND + "reload");

    static {
        PLUGIN.addChildren(COMMAND);

        COMMAND.addChildren(
            COMMAND_BOOK,
            COMMAND_RANDOM_BOOK,
            COMMAND_ENCHANT,
            COMMAND_DISENCHANT,
            COMMAND_GIVE_FUEL,
            COMMAND_LIST, COMMAND_LIST_OTHERS,
            COMMAND_RELOAD,
            COMMAND_RARITY_BOOK
        );
    }
}
