package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.registry.EnchantRegistry;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.impl.ReloadCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.random.Rnd;

import java.util.Set;

public class BaseCommands {

    public static void load(@NotNull EnchantsPlugin plugin) {
        ChainedNode rootNode = plugin.getRootNode();

        ReloadCommand.inject(plugin, rootNode, Perms.COMMAND_RELOAD);

        rootNode.addChildren(DirectNode.builder(plugin, "book")
            .description(Lang.COMMAND_BOOK_DESC)
            .permission(Perms.COMMAND_BOOK)
            .withArgument(CommandArguments.enchantArgument(CommandArguments.ENCHANT).required())
            .withArgument(CommandArguments.levelArgument(CommandArguments.LEVEL))
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .executes((context, arguments) -> giveBook(plugin, context, arguments))
        );

        rootNode.addChildren(DirectNode.builder(plugin, "enchant")
            .description(Lang.COMMAND_ENCHANT_DESC)
            .permission(Perms.COMMAND_ENCHANT)
            .withArgument(CommandArguments.enchantArgument(CommandArguments.ENCHANT).required())
            .withArgument(CommandArguments.levelArgument(CommandArguments.LEVEL).required())
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .withArgument(CommandArguments.slotArgument(CommandArguments.SLOT))
            .executes((context, arguments) -> enchantItem(plugin, context, arguments))
        );

        rootNode.addChildren(DirectNode.builder(plugin, "list")
            .playerOnly()
            .description(Lang.COMMAND_LIST_DESC)
            .permission(Perms.COMMAND_LIST)
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(Perms.COMMAND_LIST_OTHERS))
            .executes((context, arguments) -> openList(plugin, context, arguments))
        );

        rootNode.addChildren(DirectNode.builder(plugin, "raritybook")
            .description(Lang.COMMAND_RARITY_BOOK_DESC)
            .permission(Perms.COMMAND_RARITY_BOOK)
            .withArgument(CommandArguments.rarityArgument(plugin, CommandArguments.RARITY).required())
            .withArgument(CommandArguments.levelArgument(CommandArguments.LEVEL))
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .executes((context, arguments) -> giveRarityBook(plugin, context, arguments))
        );

        if (Config.isChargesEnabled()) {
            rootNode.addChildren(DirectNode.builder(plugin, "getfuel")
                .playerOnly()
                .description(Lang.COMMAND_GET_FUEL_DESC)
                .permission(Perms.COMMAND_GET_FUEL)
                .withArgument(CommandArguments.customEnchantArgument(CommandArguments.ENCHANT).required()
                    .withSamples(context -> EnchantRegistry.getRegistered().stream().filter(CustomEnchantment::hasCharges).map(CustomEnchantment::getId).toList())
                )
                .withArgument(ArgumentTypes.integerAbs(CommandArguments.AMOUNT).withSamples(context -> Lists.newList("1", "8", "16", "32", "64")))
                .executes((context, arguments) -> giveFuel(plugin, context, arguments))
            );
        }
    }

    private static int getLevel(@NotNull Enchantment enchantment, @NotNull ParsedArguments arguments) {
        int level = arguments.getIntArgument(CommandArguments.LEVEL, -1);
        if (level < 1) {
            level = EnchantUtils.randomLevel(enchantment);
        }
        return level;
    }

    public static boolean giveBook(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        Enchantment enchantment = arguments.getEnchantmentArgument(CommandArguments.ENCHANT);
        int level = getLevel(enchantment, arguments);

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchantment, level, true);
        Players.addItem(player, item);

        Lang.COMMAND_BOOK_DONE.getMessage()
            .replace(Placeholders.GENERIC_ENCHANT, EnchantUtils.getLocalized(enchantment))
            .replace(Placeholders.forPlayer(player))
            .send(context.getSender());

        return true;
    }

    public static boolean enchantItem(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        EquipmentSlot slot = arguments.getArgument(CommandArguments.SLOT, EquipmentSlot.class, EquipmentSlot.HAND);

        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType().isAir()) {
            Lang.COMMAND_ENCHANT_ERROR_NO_ITEM.getMessage().send(context.getSender());
            return false;
        }

        Enchantment enchantment = arguments.getEnchantmentArgument(CommandArguments.ENCHANT);
        int level = getLevel(enchantment, arguments);

        if (level > 0) {
            EnchantUtils.add(item, enchantment, level, true);
        }
        else EnchantUtils.remove(item, enchantment);

        //player.getInventory().setItem(slot, item);

        (context.getSender() == player ? Lang.COMMAND_ENCHANT_DONE_SELF : Lang.COMMAND_ENCHANT_DONE_OTHERS).getMessage()
            .replace(Placeholders.forPlayer(player))
            .replace(Placeholders.GENERIC_ITEM, ItemUtil.getItemName(item))
            .replace(Placeholders.GENERIC_ENCHANT, EnchantUtils.getLocalized(enchantment))
            .replace(Placeholders.GENERIC_LEVEL, NumberUtil.toRoman(level))
            .send(context.getSender());

        return true;
    }

    public static boolean giveFuel(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = context.getPlayerOrThrow();
        CustomEnchantment enchantment = arguments.getArgument(CommandArguments.ENCHANT, CustomEnchantment.class);
        int amount = arguments.getIntArgument(CommandArguments.AMOUNT, 1);

        if (!enchantment.hasCharges()) {
            Lang.COMMAND_GET_FUEL_ERROR_NO_CHARGES.getMessage()
                .replace(Placeholders.GENERIC_NAME, enchantment.getDisplayName())
                .send(context.getSender());
            return false;
        }

        ItemStack fuel = enchantment.getCharges().getFuel();
        fuel.setAmount(amount);

        Players.addItem(player, fuel);

        Lang.COMMAND_GET_FUEL_DONE.getMessage()
            .replace(Placeholders.GENERIC_AMOUNT, NumberUtil.format(amount))
            .replace(Placeholders.GENERIC_NAME, ItemUtil.getItemName(fuel))
            .send(context.getSender());

        return true;
    }

    public static boolean openList(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        plugin.getEnchantManager().openEnchantsMenu(player);

        if (player != context.getSender()) {
            Lang.COMMAND_LIST_DONE_OTHERS.getMessage().replace(Placeholders.forPlayer(player)).send(context.getSender());
        }
        return true;
    }

    public static boolean giveRarityBook(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        Rarity rarity = arguments.getArgument(CommandArguments.RARITY, Rarity.class);

        Set<CustomEnchantment> enchants = EnchantRegistry.getByRarity(rarity);
        CustomEnchantment customEnchantment = enchants.isEmpty() ? null : Rnd.get(enchants);
        if (customEnchantment == null) {
            Lang.COMMAND_RARITY_BOOK_ERROR_EMPTY.getMessage().send(context.getSender());
            return false;
        }

        Enchantment enchantment = customEnchantment.getBukkitEnchantment();
        int level = getLevel(enchantment, arguments);

        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantUtils.add(item, enchantment, level, true);
        Players.addItem(player, item);

        Lang.COMMAND_RARITY_BOOK_DONE.getMessage()
            .replace(Placeholders.GENERIC_NAME, rarity.getName())
            .replace(Placeholders.forPlayer(player))
            .send(context.getSender());

        return true;
    }
}
