package su.nightexpress.excellentenchants.api.enchantment;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;

public interface Charges {

    boolean isEnabled();

    boolean isCustomFuel();

    int getMaxAmount(int level);

    int getConsumeAmount(int level);

    int getRechargeAmount(int level);

    @NotNull ItemStack getFuel();

    @NotNull Modifier getMaxAmount();

    @NotNull Modifier getConsumeAmount();

    @NotNull Modifier getRechargeAmount();
}
