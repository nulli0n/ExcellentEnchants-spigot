package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.config.Lang;

public class ListCommand extends AbstractCommand<ExcellentEnchants> {

    public ListCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"list"}, Perms.COMMAND_LIST);
        this.setDescription(plugin.getMessage(Lang.COMMAND_LIST_DESC));
        this.setPlayerOnly(true);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        plugin.getEnchantManager().getEnchantsListGUI().open((Player) sender, 1);
    }
}
