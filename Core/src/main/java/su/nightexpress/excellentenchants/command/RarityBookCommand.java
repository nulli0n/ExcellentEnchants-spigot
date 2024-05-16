package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RarityBookCommand extends AbstractCommand<EnchantsPlugin> {

    public RarityBookCommand(@NotNull EnchantsPlugin plugin) {
        super(plugin, new String[]{"raritybook"}, Perms.COMMAND_RARITY_BOOK);
        this.setDescription(Lang.COMMAND_RARITY_BOOK_DESC);
        this.setUsage(Lang.COMMAND_RARITY_BOOK_USAGE);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return Players.playerNames(player);
        }
        if (arg == 2) {
            return Lists.getEnums(Rarity.class);
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

        Rarity rarity = StringUtil.getEnum(result.getArg(2), Rarity.class).orElse(null);
        if (rarity == null) {
            Lang.ERROR_INVALID_RARITY.getMessage().send(sender);
            return;
        }

        Set<EnchantmentData> enchants = EnchantRegistry.getByRarity(rarity);
        EnchantmentData enchantmentData = enchants.isEmpty() ? null : Rnd.get(enchants);
        if (enchantmentData == null) {
            Lang.ERROR_INVALID_ENCHANT.getMessage().send(sender);
            return;
        }

        int level = result.getInt(3, -1);
        if (level < 1) {
            level = Rnd.get(1, enchantmentData.getMaxLevel());
        }

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchantmentData.getEnchantment(), level, true);
        EnchantUtils.updateDisplay(item);
        Players.addItem(player, item);

        Lang.COMMAND_RARITY_BOOK_DONE.getMessage()
            .replace(Placeholders.GENERIC_NAME, plugin.getLangManager().getEnum(rarity))
            .replace(Placeholders.forPlayer(player))
            .send(sender);
    }
}
