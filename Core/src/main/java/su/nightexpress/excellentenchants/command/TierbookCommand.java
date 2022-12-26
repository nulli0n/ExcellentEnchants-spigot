package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.api.enchantment.ExcellentEnchant;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.manager.EnchantManager;
import su.nightexpress.excellentenchants.manager.object.EnchantTier;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TierbookCommand extends AbstractCommand<ExcellentEnchants> {

    public TierbookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"tierbook"}, Perms.COMMAND_TIERBOOK);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_TIER_BOOK_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_TIER_BOOK_USAGE).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return PlayerUtil.getPlayerNames();
        }
        if (arg == 2) {
            return EnchantManager.getTierIds();
        }
        if (arg == 3) {
            return Arrays.asList("-1", "1", "5", "10");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length != 4) {
            this.printUsage(sender);
            return;
        }

        Player player = plugin.getServer().getPlayer(args[1]);
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        EnchantTier tier = EnchantManager.getTierById(args[2].toLowerCase());
        if (tier == null) {
            plugin.getMessage(Lang.COMMAND_TIER_BOOK_ERROR).send(sender);
            return;
        }

        ExcellentEnchant enchant = Rnd.get(tier.getEnchants());
        if (enchant == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = StringUtil.getInteger(args[3], -1, true);
        if (level < 1) {
            level = Rnd.get(enchant.getStartLevel(), enchant.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantManager.addEnchant(item, enchant, level, true);
        PlayerUtil.addItem(player, item);

        plugin.getMessage(Lang.COMMAND_TIER_BOOK_DONE)
            .replace("%tier%", tier.getName())
            .replace("%player%", player.getName()).send(sender);
    }
}
