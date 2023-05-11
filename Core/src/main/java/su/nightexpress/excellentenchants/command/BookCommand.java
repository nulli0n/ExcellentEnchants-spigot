package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Arrays;
import java.util.List;

public class BookCommand extends AbstractCommand<ExcellentEnchants> {

    public BookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"book"}, Perms.COMMAND_BOOK);
        this.setDescription(plugin.getMessage(Lang.COMMAND_BOOK_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_BOOK_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 4) {
            this.printUsage(sender);
            return;
        }

        Player player = plugin.getServer().getPlayer(result.getArg(1));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(result.getArg(2).toLowerCase()));
        if (enchantment == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = result.getInt(3, -1);
        if (level < 1) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchantment, level, true);
        EnchantUtils.updateDisplay(item);
        PlayerUtil.addItem(player, item);

        plugin.getMessage(Lang.COMMAND_BOOK_DONE)
            .replace(Placeholders.GENERIC_ENCHANT, LangManager.getEnchantment(enchantment))
            .replace(Placeholders.Player.replacer(player))
            .send(sender);
    }
}
