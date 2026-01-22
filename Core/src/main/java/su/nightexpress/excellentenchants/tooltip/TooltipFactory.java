package su.nightexpress.excellentenchants.tooltip;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;

@FunctionalInterface
public interface TooltipFactory {

    @NotNull TooltipHandler create(@NotNull TooltipController provider);
}
