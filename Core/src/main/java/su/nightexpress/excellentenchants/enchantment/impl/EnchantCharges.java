package su.nightexpress.excellentenchants.enchantment.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.Charges;
import su.nightexpress.excellentenchants.config.Config;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;

import static su.nightexpress.nightcore.util.Placeholders.WIKI_ITEMS_URL;

public class EnchantCharges implements Charges {

    private boolean   enabled;
    private boolean   customFuel;
    private Modifier  maxAmount;
    private Modifier  consumeAmount;
    private Modifier  rechargeAmount;
    private ItemStack fuel;

    public EnchantCharges() {

    }

    public void load(@NotNull FileConfig config) {
        this.enabled = ConfigValue.create("Charges.Enabled",
            false,
            "When 'true' enables the Charges system for this enchantment.",
            "[*] Enchantments in enchanting table are generated with maximum charges."
        ).read(config);

        if (!this.enabled) return;

        this.customFuel = ConfigValue.create("Charges.Custom_Fuel",
            false,
            "When 'true' uses different (non-default) fuel item (from the 'Fuel_Item' setting) to recharge."
        ).read(config);

        this.maxAmount = Modifier.read(config, "Charges.Maximum",
            Modifier.add(100, 25, 1),
            "Maximum amount of charges for the enchantment."
        );

        this.consumeAmount = Modifier.read(config, "Charges.Consume_Amount",
            Modifier.add(1, 0, 0),
            "How many charges will be consumed when enchantment is triggered?"
        );

        this.rechargeAmount = Modifier.read(config, "Charges.Recharge_Amount",
            Modifier.add(25, 5, 1),
            "How many charges will be restored when using 'Fuel Item' in anvil?"
        );

        this.fuel = ConfigValue.create("Charges.Fuel_Item",
            new ItemStack(Material.LAPIS_LAZULI),
            "An item, that will be used to restore enchantment charges on anvils.",
            WIKI_ITEMS_URL
        ).read(config);
    }

    @Override
    public int getMaxAmount(int level) {
        return this.isEnabled() ? this.getMaxAmount().getIntValue(level) : 0;
    }

    @Override
    public int getConsumeAmount(int level) {
        return this.isEnabled() ? this.getConsumeAmount().getIntValue(level) : 0;
    }

    @Override
    public int getRechargeAmount(int level) {
        return this.isEnabled() ? this.getRechargeAmount().getIntValue(level) : 0;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isCustomFuel() {
        return customFuel;
    }

    @Override
    @NotNull
    public Modifier getMaxAmount() {
        return maxAmount;
    }

    @NotNull
    public ItemStack getFuel() {
        ItemStack fuelHas = this.fuel;
        if (!this.isCustomFuel() || fuelHas == null || fuelHas.getType().isAir()) {
            return new ItemStack(Config.ENCHANTMENTS_CHARGES_FUEL_ITEM.get());
        }
        return new ItemStack(fuelHas);
    }

    @Override
    @NotNull
    public Modifier getConsumeAmount() {
        return consumeAmount;
    }

    @Override
    @NotNull
    public Modifier getRechargeAmount() {
        return rechargeAmount;
    }
}
