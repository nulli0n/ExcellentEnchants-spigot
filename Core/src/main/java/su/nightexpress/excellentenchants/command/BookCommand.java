package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.EnchantManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BookCommand extends AbstractCommand<ExcellentEnchants> {

    public BookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"book"}, Perms.COMMAND_BOOK);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_BOOK_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_BOOK_USAGE).getLocalized();
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
            return Arrays.stream(Enchantment.values()).map(e -> e.getKey().getKey()).toList();
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

        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
        if (enchantment == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = StringUtil.getInteger(args[3], -1, true);
        if (level < 1) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantManager.addEnchantment(item, enchantment, level, true);
        PlayerUtil.addItem(player, item);

        plugin.getMessage(Lang.COMMAND_BOOK_DONE)
            .replace(Placeholders.GENERIC_ENCHANT, LangManager.getEnchantment(enchantment))
            .replace(Placeholders.Player.replacer(player))
            .send(sender);
    }
}
