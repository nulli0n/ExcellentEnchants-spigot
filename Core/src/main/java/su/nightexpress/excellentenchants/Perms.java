package su.nightexpress.excellentenchants;

import org.bukkit.permissions.PermissionDefault;
import su.nexmedia.engine.api.server.JPermission;

public class Perms {

    private static final String PREFIX = "excellentenchants.";
    private static final String PREFIX_COMMAND = PREFIX + "command.";

    public static final JPermission PLUGIN  = new JPermission(PREFIX + Placeholders.WILDCARD, "Access to all the plugin functions.");
    public static final JPermission COMMAND = new JPermission(PREFIX_COMMAND + Placeholders.WILDCARD, "Access to all the plugin commands.");

    public static final JPermission COMMAND_BOOK     = new JPermission(PREFIX_COMMAND + "book", "Allows to use '/eenchants book' command.");
    public static final JPermission COMMAND_ENCHANT  = new JPermission(PREFIX_COMMAND + "enchant", "Allows to use '/eenchants enchant' command.");
    public static final JPermission COMMAND_LIST     = new JPermission(PREFIX_COMMAND + "list", "Allows to use '/eenchants list' command.", PermissionDefault.TRUE);
    public static final JPermission COMMAND_TIERBOOK = new JPermission(PREFIX_COMMAND + "tierbook", "Allows to use '/eenchants tierbook' command.");
    public static final JPermission COMMAND_RELOAD   = new JPermission(PREFIX_COMMAND + "reload", "Allows to use '/eenchants reload' command.");

    static {
        PLUGIN.addChildren(COMMAND);

        COMMAND.addChildren(COMMAND_BOOK, COMMAND_ENCHANT, COMMAND_LIST, COMMAND_RELOAD, COMMAND_TIERBOOK);
    }
}
