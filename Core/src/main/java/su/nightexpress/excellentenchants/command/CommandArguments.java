package su.nightexpress.excellentenchants.command;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.EnchantRegistry;
import su.nightexpress.excellentenchants.config.Lang;
import su.nightexpress.nightcore.command.experimental.argument.ArgumentTypes;
import su.nightexpress.nightcore.command.experimental.argument.CommandArgument;
import su.nightexpress.nightcore.command.experimental.builder.ArgumentBuilder;
import su.nightexpress.nightcore.util.*;
import su.nightexpress.nightcore.util.bridge.RegistryType;

import java.util.Arrays;

public class CommandArguments {

    public static final String PLAYER  = "player";
    public static final String AMOUNT  = "amount";
    public static final String LEVEL   = "level";
    public static final String ENCHANT = "enchant";
    public static final String SLOT    = "slot";

    @NotNull
    public static ArgumentBuilder<Enchantment> enchantArgument(@NotNull String name) {
        return ArgumentTypes.enchantment(name).withSamples(context -> BukkitThing.getAsStrings(RegistryType.ENCHANTMENT));
    }

    @NotNull
    public static ArgumentBuilder<CustomEnchantment> customEnchantArgument(@NotNull String name) {
        return CommandArgument.builder(name, (string, context) -> EnchantRegistry.getById(string))
            .localized(Lang.COMMAND_ARGUMENT_NAME_ENCHANTMENT)
            .customFailure(Lang.ERROR_COMMAND_INVALID_ENCHANTMENT_ARGUMENT)
            .withSamples(context -> EnchantRegistry.getRegisteredNames());
    }

    @NotNull
    public static ArgumentBuilder<Integer> levelArgument(@NotNull String name) {
        return ArgumentTypes.integer(CommandArguments.LEVEL)
            .localized(Lang.COMMAND_ARGUMENT_NAME_LEVEL)
            .withSamples(context -> Lists.newList("-1", "1", "3", "5", "10"));
    }

    @NotNull
    public static ArgumentBuilder<EquipmentSlot> slotArgument(@NotNull String name) {
        return CommandArgument.builder(name, (str, context) -> {
                EquipmentSlot slot = Enums.get(str, EquipmentSlot.class);
                return slot == EquipmentSlot.BODY ? null : slot;
            })
            .localized(Lang.COMMAND_ARGUMENT_NAME_SLOT)
            .customFailure(Lang.ERROR_COMMAND_INVALID_SLOT_ARGUMENT)
            .withSamples(context -> Arrays.stream(EntityUtil.EQUIPMENT_SLOTS).map(Enum::name).map(String::toLowerCase).toList());
    }
}
