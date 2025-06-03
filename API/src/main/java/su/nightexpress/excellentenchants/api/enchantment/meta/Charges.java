package su.nightexpress.excellentenchants.api.enchantment.meta;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class Charges {

    private final Modifier  maxAmount;
    private final int       consumeAmount;
    private final int       rechargeAmount;
    private final boolean   customFuelEnabled;
    private final NightItem customFuelItem;

    public Charges(@NotNull Modifier maxAmount, int consumeAmount, int rechargeAmount, boolean customFuelEnabled, @NotNull NightItem customFuelItem) {
        this.maxAmount = maxAmount;
        this.consumeAmount = consumeAmount;
        this.rechargeAmount = rechargeAmount;
        this.customFuelEnabled = customFuelEnabled;
        this.customFuelItem = customFuelItem;
    }

    @NotNull
    public static Charges normal() {
        return new Charges(Modifier.addictive(100).perLevel(25).build(), 1, 25, false, NightItem.fromType(Material.LAPIS_LAZULI));
    }

    @NotNull
    public static Charges custom(@NotNull Modifier.Builder maxAmount, int consumeAmount, int rechargeAmount, @NotNull NightItem fuel) {
        return custom(maxAmount.build(), consumeAmount, rechargeAmount, fuel);
    }

    @NotNull
    public static Charges custom(@NotNull Modifier maxAmount, int consumeAmount, int rechargeAmount, @NotNull NightItem fuel) {
        return new Charges(maxAmount, consumeAmount, rechargeAmount, true, fuel);
    }

    public int getMaxAmount(int level) {
        return this.maxAmount.getIntValue(level);
    }

    public boolean isCustomFuelEnabled() {
        return this.customFuelEnabled;
    }

    @NotNull
    public Modifier getMaxAmount() {
        return this.maxAmount;
    }

    @NotNull
    public NightItem getCustomFuelItem() {
        return this.customFuelItem;
    }

    public int getConsumeAmount() {
        return this.consumeAmount;
    }

    public int getRechargeAmount() {
        return this.rechargeAmount;
    }
}
