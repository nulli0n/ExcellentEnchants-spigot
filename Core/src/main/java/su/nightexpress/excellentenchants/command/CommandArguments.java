package su.nightexpress.excellentenchants.command;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.enchantment.EnchantRegistry;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.builder.ArgumentNodeBuilder;
import su.nightexpress.nightcore.commands.exceptions.CommandSyntaxException;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bridge.RegistryType;

import java.util.Arrays;
import java.util.Optional;

public class CommandArguments {

    public static final String PLAYER  = "player";
    public static final String AMOUNT  = "amount";
    public static final String LEVEL   = "level";
    public static final String ENCHANT = "enchant";
    public static final String SLOT    = "slot";
    
    public static final String FLAG_CUSTOM  = "custom";
    public static final String FLAG_CHARGED = "charged";

    @NotNull
    public static ArgumentNodeBuilder<Enchantment> enchantArgument(@NotNull String name) {
        return Arguments.enchantment(name).suggestions((reader, context) -> BukkitThing.getAsStrings(RegistryType.ENCHANTMENT));
    }

    @NotNull
    public static ArgumentNodeBuilder<CustomEnchantment> customEnchantArgument(@NotNull String name) {
        return Commands.argument(name, (context, string) ->
                Optional.ofNullable(EnchantRegistry.getById(string)).orElseThrow(() -> CommandSyntaxException.custom(CoreLang.COMMAND_SYNTAX_INVALID_ENCHANTMENT))
            )
            .localized(CoreLang.COMMAND_ARGUMENT_NAME_ENCHANTMENT)
            .suggestions((reader, context) -> EnchantRegistry.getRegisteredNames());
    }

    @NotNull
    public static ArgumentNodeBuilder<Integer> levelArgument(@NotNull String name) {
        return Arguments.integer(CommandArguments.LEVEL)
            .localized(Lang.COMMAND_ARGUMENT_NAME_LEVEL)
            .suggestions((reader, context) -> Lists.newList("-1", "1", "3", "5", "10"));
    }

    @NotNull
    public static ArgumentNodeBuilder<EquipmentSlot> slotArgument(@NotNull String name) {
        return Commands.argument(name, (context, string) ->
                Enums.parse(string, EquipmentSlot.class)
                    .filter(slot -> slot != EquipmentSlot.BODY)
                    .orElseThrow(() -> CommandSyntaxException.custom(Lang.COMMAND_SYNTAX_INVALID_SLOT))
            )
            .localized(Lang.COMMAND_ARGUMENT_NAME_SLOT)
            .suggestions((reader, context) -> Arrays.stream(EntityUtil.EQUIPMENT_SLOTS).map(Enum::name).map(String::toLowerCase).toList());
    }
}
