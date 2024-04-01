package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.util.Players;

import java.util.List;

public class ListCommand extends AbstractCommand<EnchantsPlugin> {

    public ListCommand(@NotNull EnchantsPlugin plugin) {
        super(plugin, new String[]{"list"}, Perms.COMMAND_LIST);
        this.setDescription(Lang.COMMAND_LIST_DESC);
        this.setDescription(Lang.COMMAND_LIST_USAGE);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1 && player.hasPermission(Perms.COMMAND_LIST_OTHERS)) {
            return Players.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() >= 2 && !sender.hasPermission(Perms.COMMAND_LIST_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        Player player = Players.getPlayer(result.getArg(1, sender.getName()));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        plugin.getEnchantManager().openEnchantsMenu(player);

        if (player != sender) {
            Lang.COMMAND_LIST_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)).send(sender);
        }
    }
}
