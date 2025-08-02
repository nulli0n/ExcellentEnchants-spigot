package su.nightexpress.excellentenchants.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.command.experimental.builder.SimpleFlagBuilder;
import su.nightexpress.nightcore.command.experimental.flag.FlagTypes;

public class CommandFlags {

    public static final String CUSTOM = "custom";
    public static final String CHARGED = "charged";

    @NotNull
    public static SimpleFlagBuilder custom() {
        return FlagTypes.simple(CUSTOM);
    }

    @NotNull
    public static SimpleFlagBuilder charged() {
        return FlagTypes.simple(CHARGED);
    }
}
