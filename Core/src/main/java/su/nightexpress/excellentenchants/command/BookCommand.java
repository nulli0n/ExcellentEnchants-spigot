package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Arrays;
import java.util.List;

public class BookCommand extends AbstractCommand<EnchantsPlugin> {

    public BookCommand(@NotNull EnchantsPlugin plugin) {
        super(plugin, new String[]{"book"}, Perms.COMMAND_BOOK);
        this.setDescription(Lang.COMMAND_BOOK_DESC);
        this.setUsage(Lang.COMMAND_BOOK_USAGE);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Players.playerNames(player);
        }
        if (arg == 2) {
            return BukkitThing.getEnchantments().stream().map(enchantment -> enchantment.getKey().getKey()).toList();
        }
        if (arg == 3) {
            return Arrays.asList("-1", "1", "5", "10");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 4) {
            this.errorUsage(sender);
            return;
        }

        Player player = Players.getPlayer(result.getArg(1));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        Enchantment enchantment = BukkitThing.getEnchantment(result.getArg(2));
        if (enchantment == null) {
            Lang.ERROR_INVALID_ENCHANT.getMessage().send(sender);
            return;
        }

        int level = result.getInt(3, -1);
        if (level < 1) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchantment, level, true);
        EnchantUtils.updateDisplay(item);
        Players.addItem(player, item);

        Lang.COMMAND_BOOK_DONE.getMessage()
            .replace(Placeholders.GENERIC_ENCHANT, EnchantUtils.getLocalized(enchantment))
            .replace(Placeholders.forPlayer(player))
            .send(sender);
    }
}
