package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchantsPlugin;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;

public class ListCommand extends AbstractCommand<ExcellentEnchantsPlugin> {

    public ListCommand(@NotNull ExcellentEnchantsPlugin plugin) {
        super(plugin, new String[]{"list"}, Perms.COMMAND_LIST);
        this.setDescription(Lang.COMMAND_LIST_DESC);
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        plugin.getEnchantManager().openEnchantsMenu((Player) sender);
    }
}
