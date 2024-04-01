package su.nightexpress.excellentenchants.config;

import org.bukkit.permissions.PermissionDefault;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.nightcore.util.wrapper.UniPermission;

public class Perms {

    private static final String PREFIX         = "excellentenchants.";
    private static final String PREFIX_COMMAND = PREFIX + "command.";

    public static final UniPermission PLUGIN  = new UniPermission(PREFIX + Placeholders.WILDCARD, "Access to all the plugin functions.");
    public static final UniPermission COMMAND = new UniPermission(PREFIX_COMMAND + Placeholders.WILDCARD, "Access to all the plugin commands.");

    public static final UniPermission COMMAND_BOOK        = new UniPermission(PREFIX_COMMAND + "book");
    public static final UniPermission COMMAND_ENCHANT     = new UniPermission(PREFIX_COMMAND + "enchant");
    public static final UniPermission COMMAND_GET_FUEL    = new UniPermission(PREFIX_COMMAND + "getfuel");
    public static final UniPermission COMMAND_LIST        = new UniPermission(PREFIX_COMMAND + "list", PermissionDefault.TRUE);
    public static final UniPermission COMMAND_LIST_OTHERS        = new UniPermission(PREFIX_COMMAND + "list.others");
    public static final UniPermission COMMAND_RARITY_BOOK = new UniPermission(PREFIX_COMMAND + "raritybook");
    public static final UniPermission COMMAND_RELOAD      = new UniPermission(PREFIX_COMMAND + "reload");

    static {
        PLUGIN.addChildren(COMMAND);

        COMMAND.addChildren(
            COMMAND_BOOK,
            COMMAND_ENCHANT,
            COMMAND_GET_FUEL,
            COMMAND_LIST, COMMAND_LIST_OTHERS,
            COMMAND_RELOAD,
            COMMAND_RARITY_BOOK
        );
    }
}
