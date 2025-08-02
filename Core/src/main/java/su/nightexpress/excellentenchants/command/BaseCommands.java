package su.nightexpress.excellentenchants.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.excellentenchants.config.Perms;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.command.experimental.CommandContext;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.ParsedArguments;
import su.nightexpress.nightcore.command.experimental.impl.ReloadCommand;
import su.nightexpress.nightcore.command.experimental.node.ChainedNode;
import su.nightexpress.nightcore.command.experimental.node.DirectNode;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bridge.RegistryType;
import su.nightexpress.nightcore.util.random.Rnd;

public class BaseCommands {

    public static void load(@NotNull EnchantsPlugin plugin) {
        ChainedNode rootNode = plugin.getRootNode();

        rootNode.addChildren(ReloadCommand.builder(plugin, Perms.COMMAND_RELOAD));

        rootNode.addChildren(DirectNode.builder(plugin, "book")
            .description(Lang.COMMAND_BOOK_DESC)
            .permission(Perms.COMMAND_BOOK)
            .withArgument(CommandArguments.enchantArgument(CommandArguments.ENCHANT).required())
            .withArgument(CommandArguments.levelArgument(CommandArguments.LEVEL))
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .withFlag(CommandFlags.charged())
            .executes(BaseCommands::giveEnchantBook)
        );

        rootNode.addChildren(DirectNode.builder(plugin, "randombook")
            .description(Lang.COMMAND_RANDOM_BOOK_DESC)
            .permission(Perms.COMMAND_RANDOM_BOOK)
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .withFlag(CommandFlags.custom())
            .withFlag(CommandFlags.charged())
            .executes(BaseCommands::giveRandomBook)
        );

        rootNode.addChildren(DirectNode.builder(plugin, "enchant")
            .description(Lang.COMMAND_ENCHANT_DESC)
            .permission(Perms.COMMAND_ENCHANT)
            .withArgument(CommandArguments.enchantArgument(CommandArguments.ENCHANT).required())
            .withArgument(CommandArguments.levelArgument(CommandArguments.LEVEL).required())
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .withArgument(CommandArguments.slotArgument(CommandArguments.SLOT))
            .withFlag(CommandFlags.charged())
            .executes((context, arguments) -> enchantItem(plugin, context, arguments))
        );

        rootNode.addChildren(DirectNode.builder(plugin, "disenchant")
            .description(Lang.COMMAND_DISENCHANT_DESC)
            .permission(Perms.COMMAND_DISENCHANT)
            .withArgument(CommandArguments.enchantArgument(CommandArguments.ENCHANT).required())
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
            .withArgument(CommandArguments.slotArgument(CommandArguments.SLOT))
            .executes((context, arguments) -> disenchantItem(plugin, context, arguments))
        );

        rootNode.addChildren(DirectNode.builder(plugin, "list")
            .playerOnly()
            .description(Lang.COMMAND_LIST_DESC)
            .permission(Perms.COMMAND_LIST)
            .withArgument(ArgumentTypes.player(CommandArguments.PLAYER).permission(Perms.COMMAND_LIST_OTHERS))
            .executes((context, arguments) -> openList(plugin, context, arguments))
        );

        if (Config.isChargesEnabled()) {
            rootNode.addChildren(DirectNode.builder(plugin, "givefuel")
                .playerOnly()
                .description(Lang.COMMAND_GIVE_FUEL_DESC)
                .permission(Perms.COMMAND_GIVE_FUEL)
                .withArgument(CommandArguments.customEnchantArgument(CommandArguments.ENCHANT).required()
                    .withSamples(context -> EnchantRegistry.getRegistered().stream().filter(CustomEnchantment::isChargeable).map(CustomEnchantment::getId).toList())
                )
                .withArgument(ArgumentTypes.integerAbs(CommandArguments.AMOUNT).withSamples(context -> Lists.newList("1", "8", "16", "32", "64")))
                .withArgument(ArgumentTypes.player(CommandArguments.PLAYER))
                .executes((context, arguments) -> giveFuel(plugin, context, arguments))
            );
        }
    }

    private static int getLevel(@NotNull Enchantment enchantment, @NotNull ParsedArguments arguments) {
        int level = arguments.getIntArgument(CommandArguments.LEVEL, -1);
        if (level <= 0) {
            level = EnchantUtils.randomLevel(enchantment);
        }
        return level;
    }

    public static boolean giveEnchantBook(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        boolean charged = arguments.hasFlag(CommandFlags.CHARGED);
        Enchantment enchantment = arguments.getEnchantmentArgument(CommandArguments.ENCHANT);
        int level = getLevel(enchantment, arguments);

        giveBook(context.getSender(), player, enchantment, level, charged);
        return true;
    }

    public static boolean giveRandomBook(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        boolean custom = arguments.hasFlag(CommandFlags.CUSTOM);
        boolean charged = arguments.hasFlag(CommandFlags.CHARGED);
        Enchantment enchantment = Rnd.get(custom ? EnchantRegistry.getRegisteredBukkit() : BukkitThing.getAll(RegistryType.ENCHANTMENT));
        int level = EnchantUtils.randomLevel(enchantment);

        giveBook(context.getSender(), player, enchantment, level, charged);
        return true;
    }

    private static boolean giveBook(@NotNull CommandSender sender, @NotNull Player player, @NotNull Enchantment enchantment, int level, boolean charged) {
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        if (charged) {
            EnchantUtils.restoreCharges(itemStack, enchantment, level);
        }

        EnchantUtils.add(itemStack, enchantment, level, true);
        Players.addItem(player, itemStack);

        Lang.ENCHANTED_BOOK_GAVE.getMessage().send(sender, replacer -> replacer
            .replace(EnchantsPlaceholders.GENERIC_ENCHANT, LangUtil.getSerializedName(enchantment))
            .replace(EnchantsPlaceholders.GENERIC_LEVEL, NumberUtil.toRoman(level))
            .replace(EnchantsPlaceholders.forPlayer(player))
        );

        return true;
    }

    public static boolean enchantItem(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        EquipmentSlot slot = arguments.getArgument(CommandArguments.SLOT, EquipmentSlot.class, EquipmentSlot.HAND);

        ItemStack itemStack = EntityUtil.getItemInSlot(player, slot);
        if (itemStack == null || itemStack.getType().isAir()) {
            Lang.COMMAND_ENCHANT_ERROR_NO_ITEM.getMessage().send(context.getSender());
            return false;
        }

        boolean charged = arguments.hasFlag(CommandFlags.CHARGED);
        Enchantment enchantment = arguments.getEnchantmentArgument(CommandArguments.ENCHANT);
        int level = getLevel(enchantment, arguments);

        EnchantUtils.add(itemStack, enchantment, level, true);

        if (charged) {
            EnchantUtils.restoreCharges(itemStack, enchantment, level);
        }

        (context.getSender() == player ? Lang.COMMAND_ENCHANT_DONE_SELF : Lang.COMMAND_ENCHANT_DONE_OTHERS).getMessage().send(context.getSender(), replacer -> replacer
            .replace(EnchantsPlaceholders.forPlayer(player))
            .replace(EnchantsPlaceholders.GENERIC_ITEM, ItemUtil.getNameSerialized(itemStack))
            .replace(EnchantsPlaceholders.GENERIC_ENCHANT, LangUtil.getSerializedName(enchantment))
            .replace(EnchantsPlaceholders.GENERIC_LEVEL, NumberUtil.toRoman(level))
        );

        return true;
    }

    public static boolean disenchantItem(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        EquipmentSlot slot = arguments.getArgument(CommandArguments.SLOT, EquipmentSlot.class, EquipmentSlot.HAND);

        ItemStack itemStack = EntityUtil.getItemInSlot(player, slot);
        if (itemStack == null || itemStack.getType().isAir()) {
            Lang.COMMAND_ENCHANT_ERROR_NO_ITEM.getMessage().send(context.getSender());
            return false;
        }

        Enchantment enchantment = arguments.getEnchantmentArgument(CommandArguments.ENCHANT);
        EnchantUtils.remove(itemStack, enchantment);

        (context.getSender() == player ? Lang.COMMAND_DISENCHANT_DONE_SELF : Lang.COMMAND_DISENCHANT_DONE_OTHERS).getMessage().send(context.getSender(), replacer -> replacer
            .replace(EnchantsPlaceholders.forPlayer(player))
            .replace(EnchantsPlaceholders.GENERIC_ITEM, ItemUtil.getNameSerialized(itemStack))
            .replace(EnchantsPlaceholders.GENERIC_ENCHANT, LangUtil.getSerializedName(enchantment))
        );

        return true;
    }

    public static boolean giveFuel(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        CustomEnchantment enchantment = arguments.getArgument(CommandArguments.ENCHANT, CustomEnchantment.class);
        int amount = arguments.getIntArgument(CommandArguments.AMOUNT, 1);

        if (!enchantment.isChargeable()) {
            Lang.CHARGES_FUEL_BAD_ENCHANTMENT.getMessage().send(context.getSender(), replacer -> replacer
                .replace(EnchantsPlaceholders.GENERIC_NAME, enchantment.getDisplayName())
            );
            return false;
        }

        ItemStack fuel = enchantment.getFuel();
        fuel.setAmount(amount);

        Players.addItem(player, fuel);

        Lang.CHARGES_FUEL_GAVE.getMessage().send(context.getSender(), replacer -> replacer
            .replace(EnchantsPlaceholders.GENERIC_AMOUNT, NumberUtil.format(amount))
            .replace(EnchantsPlaceholders.GENERIC_NAME, ItemUtil.getNameSerialized(fuel))
            .replace(EnchantsPlaceholders.forPlayer(player))
        );

        return true;
    }

    public static boolean openList(@NotNull EnchantsPlugin plugin, @NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        Player player = CommandUtil.getPlayerOrSender(context, arguments, CommandArguments.PLAYER);
        if (player == null) return false;

        plugin.getEnchantManager().openEnchantsMenu(player);

        if (player != context.getSender()) {
            Lang.COMMAND_LIST_DONE_OTHERS.getMessage().send(context.getSender(), replacer -> replacer.replace(EnchantsPlaceholders.forPlayer(player)));
        }
        return true;
    }
}
