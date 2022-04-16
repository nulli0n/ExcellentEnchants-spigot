package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;

public class ListCommand extends AbstractCommand<ExcellentEnchants> {

    public ListCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"list"}, Perms.COMMAND_LIST);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_List_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.getEnchantManager().getEnchantsListGUI().open((Player) sender, 1);
    }
}
