package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Arrays;
import java.util.List;

public class EnchantCommand extends AbstractCommand<EnchantsPlugin> {

    public EnchantCommand(@NotNull EnchantsPlugin plugin) {
        super(plugin, new String[]{"enchant"}, Perms.COMMAND_ENCHANT);
        this.setDescription(Lang.COMMAND_ENCHANT_DESC);
        this.setUsage(Lang.COMMAND_ENCHANT_USAGE);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return BukkitThing.getEnchantments().stream().map(enchantment -> enchantment.getKey().getKey()).toList();
        }
        if (arg == 2) {
            return Arrays.asList("-1", "1", "5", "10");
        }
        if (arg == 3) {
            return Players.playerNames(player);
        }
        if (arg == 4) {
            return Lists.getEnums(EquipmentSlot.class);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 3) {
            this.errorUsage(sender);
            return;
        }

        Enchantment enchantment = BukkitThing.getEnchantment(result.getArg(1));
        if (enchantment == null) {
            Lang.ERROR_INVALID_ENCHANT.getMessage().send(sender);
            return;
        }

        Player player = Players.getPlayer(result.getArg(3, sender.getName()));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        EquipmentSlot slot = StringUtil.getEnum(result.getArg(4, ""), EquipmentSlot.class).orElse(EquipmentSlot.HAND);

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) {
            Lang.COMMAND_ENCHANT_ERROR_NO_ITEM.getMessage().send(sender);
            return;
        }

        int level = result.getInt(2, -1);
        if (level < 0) {
            level = Rnd.get(enchantment.getStartLevel(), enchantment.getMaxLevel());
        }

        if (level > 0) {
            EnchantUtils.add(item, enchantment, level, true);
        }
        else EnchantUtils.remove(item, enchantment);

        EnchantUtils.updateDisplay(item);
        player.getInventory().setItem(slot, item);

        (sender == player ? Lang.COMMAND_ENCHANT_DONE_SELF : Lang.COMMAND_ENCHANT_DONE_OTHERS).getMessage()
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
            .replace(Placeholders.GENERIC_ENCHANT, EnchantUtils.getLocalized(enchantment))
            .replace(Placeholders.GENERIC_LEVEL, NumberUtil.toRoman(level))
            .send(sender);
    }
}
