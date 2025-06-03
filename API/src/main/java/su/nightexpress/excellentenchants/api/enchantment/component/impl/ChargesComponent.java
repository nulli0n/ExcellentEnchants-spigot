package su.nightexpress.excellentenchants.api.enchantment.component.impl;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.EnchantsPlaceholders;
import su.nightexpress.excellentenchants.api.Modifier;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Charges;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.bukkit.NightItem;

public class ChargesComponent implements EnchantComponent<Charges> {

    @Override
    @NotNull
    public String getName() {
        return "charges";
    }

    @Override
    @NotNull
    public Charges read(@NotNull FileConfig config, @NotNull Charges defaultValue) {
        Modifier maxAmount = Modifier.load(config, "Charges.Max_Amount",
            defaultValue.getMaxAmount(),
            "Maximum amount of charges for the enchantment."
        );

        int consumeAmount = ConfigValue.create("Charges.Consume_Amount",
            defaultValue.getConsumeAmount(),
            "Controls how many charges consumed when enchantment is triggered."
        ).read(config);

        int rechargeAmount = ConfigValue.create("Charges.Recharge_Amount",
            defaultValue.getRechargeAmount(),
            "Controls how many charges added per fuel item."
        ).read(config);

        boolean customFuelEnabled = ConfigValue.create("Charges.CustomFuel.Enabled",
            defaultValue.isCustomFuelEnabled(),
            "Controls if custom fuel item should be used for this enchantment."
        ).read(config);

        NightItem customFuelItem = ConfigValue.create("Charges.CustomFuel.Item",
            defaultValue.getCustomFuelItem(),
            "Custom fuel item.",
            EnchantsPlaceholders.URL_WIKI_ITEMS
        ).read(config);

        return new Charges(maxAmount, consumeAmount, rechargeAmount, customFuelEnabled, customFuelItem);
    }
}
