package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.Arrays;
import java.util.List;

public class BookCommand extends AbstractCommand<ExcellentEnchants> {

    public BookCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"book"}, Perms.COMMAND_BOOK);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Command_Book_Desc.getMsg();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.lang().Command_Book_Usage.getMsg();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int i, @NotNull String[] args) {
        if (i == 1) {
            return PlayerUtil.getPlayerNames();
        }
        if (i == 2) {
            return Arrays.stream(Enchantment.values()).map(e -> e.getKey().getKey()).toList();
        }
        if (i == 3) {
            return Arrays.asList("-1", "1", "5", "10");
        }
        return super.getTab(player, i, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
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
            plugin.lang().Error_NoEnchant.send(sender);
            return;
        }

        int level = StringUtil.getInteger(args[3], -1, true);
        if (level < 1) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
        if (meta == null) return;

        meta.addStoredEnchant(enchantment, level, true);
        item.setItemMeta(meta);

        EnchantManager.updateItemLoreEnchants(item);
        PlayerUtil.addItem(player, item);

        plugin.lang().Command_Book_Done
            .replace("%enchant%", plugin.lang().getEnchantment(enchantment))
            .replace("%player%", player.getName()).send(sender);
    }
}
