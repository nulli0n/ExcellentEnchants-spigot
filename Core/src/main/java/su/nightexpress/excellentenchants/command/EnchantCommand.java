package su.nightexpress.excellentenchants.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.manager.EnchantManager;

import java.util.Arrays;
import java.util.List;

public class EnchantCommand extends AbstractCommand<ExcellentEnchants> {

    public EnchantCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"enchant"}, Perms.COMMAND_ENCHANT);
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_ENCHANT_DESC).getLocalized();
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_ENCHANT_USAGE).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Arrays.stream(Enchantment.values()).map(e -> e.getKey().getKey()).toList();
        }
        if (arg == 2) {
            return Arrays.asList("-1", "1", "5", "10");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length != 3) {
            this.printUsage(sender);
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            this.errorItem(sender);
            return;
        }

        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[1].toLowerCase()));
        if (enchantment == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        int level = StringUtil.getInteger(args[2], -1, true);
        if (level < 0) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            if (level == 0) {
                storageMeta.removeStoredEnchant(enchantment);
            }
            else {
                storageMeta.addStoredEnchant(enchantment, level, true);
            }
        }
        else {
            if (level == 0) {
                meta.removeEnchant(enchantment);
            }
            else {
                meta.addEnchant(enchantment, level, true);
            }
        }
        item.setItemMeta(meta);
        EnchantManager.updateItemLoreEnchants(item);

        plugin.getMessage(Lang.COMMAND_ENCHANT_DONE).send(sender);
    }
}
