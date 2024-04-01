package su.nightexpress.excellentenchants.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.EnchantmentData;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.enchantment.registry.EnchantRegistry;
import su.nightexpress.nightcore.command.CommandResult;
import su.nightexpress.nightcore.command.impl.AbstractCommand;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Players;

import java.util.List;

public class GetFuelCommand extends AbstractCommand<EnchantsPlugin> {

    public GetFuelCommand(@NotNull EnchantsPlugin plugin) {
        super(plugin, new String[]{"getfuel"}, Perms.COMMAND_GET_FUEL);
        this.setDescription(Lang.COMMAND_GET_FUEL_DESC);
        this.setUsage(Lang.COMMAND_GET_FUEL_USAGE);
        this.setPlayerOnly(true);
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return EnchantRegistry.getRegistered().stream().filter(EnchantmentData::isChargesEnabled).map(EnchantmentData::getId).toList();
        }
        if (arg == 2) {
            return Lists.newList("1", "8", "16", "32", "64");
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.errorUsage(sender);
            return;
        }

        EnchantmentData data = EnchantRegistry.getById(result.getArg(1));
        if (data == null) {
            Lang.ERROR_INVALID_ENCHANT.getMessage().send(sender);
            return;
        }

        int amount = result.getInt(2, 1);

        ItemStack fuel = data.getChargesFuel();
        fuel.setAmount(amount);

        Player player = (Player) sender;
        Players.addItem(player, fuel);

        Lang.COMMAND_GET_FUEL_DONE.getMessage()
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount))
            .replace(Placeholders.GENERIC_NAME, ItemUtil.getItemName(fuel))
            .send(sender);
    }
}
