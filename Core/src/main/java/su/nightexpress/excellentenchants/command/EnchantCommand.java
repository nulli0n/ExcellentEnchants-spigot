package su.nightexpress.excellentenchants.command;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.utils.*;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Perms;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.enchantment.util.EnchantUtils;

import java.util.Arrays;
import java.util.List;

public class EnchantCommand extends AbstractCommand<ExcellentEnchants> {

    public EnchantCommand(@NotNull ExcellentEnchants plugin) {
        super(plugin, new String[]{"enchant"}, Perms.COMMAND_ENCHANT);
        this.setDescription(plugin.getMessage(Lang.COMMAND_ENCHANT_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_ENCHANT_USAGE));
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
        if (arg == 3) {
            return CollectionsUtil.playerNames(player);
        }
        if (arg == 4) {
            return CollectionsUtil.getEnumsList(EquipmentSlot.class);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 3) {
            this.printUsage(sender);
            return;
        }

        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(result.getArg(1).toLowerCase()));
        if (enchantment == null) {
            plugin.getMessage(Lang.ERROR_NO_ENCHANT).send(sender);
            return;
        }

        Player player = PlayerUtil.getPlayer(result.getArg(3, sender.getName()));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        EquipmentSlot slot = StringUtil.getEnum(result.getArg(4, ""), EquipmentSlot.class).orElse(EquipmentSlot.HAND);

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) {
            this.plugin.getMessage(Lang.COMMAND_ENCHANT_ERROR_NO_ITEM).send(sender);
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

        plugin.getMessage(sender == player ? Lang.COMMAND_ENCHANT_DONE_SELF : Lang.COMMAND_ENCHANT_DONE_OTHERS)
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
            .replace(Placeholders.GENERIC_ENCHANT, EnchantUtils.getLocalized(enchantment))
            .replace(Placeholders.GENERIC_LEVEL, NumberUtil.toRoman(level))
            .send(sender);
    }
}
