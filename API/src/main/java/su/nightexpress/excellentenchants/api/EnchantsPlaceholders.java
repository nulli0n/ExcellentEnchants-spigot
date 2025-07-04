package su.nightexpress.excellentenchants.api;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.CustomEnchantment;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.LangUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;

public class EnchantsPlaceholders extends Placeholders {

    public static final String WIKI_URL          = "https://nightexpressdev.com/excellentenchants/";
    public static final String WIKI_PLACEHOLDERS = WIKI_URL + "placeholders";
    public static final String WIKI_MODIFIERS    = WIKI_URL + "modifiers";
    public static final String WIKI_CHRAGES      = WIKI_URL + "features/charges";
    public static final String WIKI_ITEM_SETS    = WIKI_URL + "features/item-sets";

    public static final String GENERIC_TYPE        = "%type%";
    public static final String GENERIC_NAME        = "%name%";
    public static final String GENERIC_ITEM        = "%item%";
    public static final String GENERIC_LEVEL       = "%level%";
    public static final String GENERIC_AMOUNT      = "%amount%";
    public static final String GENERIC_CHARGES     = "%charges%";
    public static final String GENERIC_MODIFIER    = "%modifier%";
    public static final String GENERIC_DESCRIPTION = "%description%";
    public static final String GENERIC_ENCHANT     = "%enchant%";
    public static final String GENERIC_RADIUS      = "%radius%";
    public static final String GENERIC_DURATION    = "%duration%";
    public static final String GENERIC_DAMAGE      = "%damage%";
    public static final String GENERIC_MIN         = "%min%";
    public static final String GENERIC_MAX         = "%max%";
    public static final String GENERIC_TIME        = "%time%";

    public static final String TRIGGER_CHANCE   = "%enchantment_trigger_chance%";
    public static final String TIRGGER_INTERVAL = "%enchantment_trigger_interval%";
    public static final String EFFECT_AMPLIFIER = "%enchantment_potion_level%";
    public static final String EFFECT_DURATION  = "%enchantment_potion_duration%";
    public static final String EFFECT_TYPE      = "%enchantment_potion_type%";

    public static final String ENCHANTMENT_ID                            = "%enchantment_id%";
    public static final String ENCHANTMENT_NAME                          = "%enchantment_name%";
    public static final String ENCHANTMENT_DESCRIPTION                   = "%enchantment_description%";
    public static final String ENCHANTMENT_DESCRIPTION_REPLACED          = "%enchantment_description_replaced%";
    public static final String ENCHANTMENT_LEVEL                         = "%enchantment_level%";
    public static final String ENCHANTMENT_LEVEL_MIN                     = "%enchantment_level_min%";
    public static final String ENCHANTMENT_LEVEL_MAX                     = "%enchantment_level_max%";
    public static final String ENCHANTMENT_FIT_ITEM_TYPES                = "%enchantment_fit_item_types%";
    public static final String ENCHANTMENT_CHARGES_MAX_AMOUNT            = "%enchantment_charges_max_amount%";
    public static final String ENCHANTMENT_CHARGES_CONSUME_AMOUNT        = "%enchantment_charges_consume_amount%";
    public static final String ENCHANTMENT_CHARGES_RECHARGE_AMOUNT       = "%enchantment_charges_recharge_amount%";
    public static final String ENCHANTMENT_CHARGES_FUEL_ITEM             = "%enchantment_charges_fuel_item%";

    @NotNull
    public static PlaceholderList<Integer> forEnchant(@NotNull CustomEnchantment enchantment) {
        return PlaceholderList.create(list -> list
            .add(ENCHANTMENT_ID, level -> enchantment.getId())
            .add(ENCHANTMENT_NAME, level -> enchantment.getDisplayName())
            .add(ENCHANTMENT_DESCRIPTION, level -> String.join("\n", enchantment.getDescription()))
            .add(ENCHANTMENT_DESCRIPTION_REPLACED, level -> String.join("\n", enchantment.getDescription(level)))
            .add(ENCHANTMENT_LEVEL, NumberUtil::toRoman)
            .add(ENCHANTMENT_LEVEL_MIN, level -> String.valueOf(1))
            .add(ENCHANTMENT_LEVEL_MAX, level -> String.valueOf(enchantment.getDefinition().getMaxLevel()))
            .add(ENCHANTMENT_FIT_ITEM_TYPES, level -> enchantment.getSupportedItems().getDisplayName())
            .add(ENCHANTMENT_CHARGES_MAX_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getMaxAmount(level)))
            .add(ENCHANTMENT_CHARGES_CONSUME_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getConsumeAmount()))
            .add(ENCHANTMENT_CHARGES_RECHARGE_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getRechargeAmount()))
            .add(ENCHANTMENT_CHARGES_FUEL_ITEM, level -> ItemUtil.getItemNameSerialized(enchantment.getFuel()))
            .add(TRIGGER_CHANCE, level -> NumberUtil.format(enchantment.getComponent(EnchantComponent.PROBABILITY).getTriggerChance(level)))
            .add(TIRGGER_INTERVAL, () -> NumberUtil.format(enchantment.getComponent(EnchantComponent.PERIODIC).getInterval()))
            .add(EFFECT_AMPLIFIER, level -> NumberUtil.toRoman(enchantment.getComponent(EnchantComponent.POTION_EFFECT).getAmplifier(level)))
            .add(EFFECT_DURATION, level -> NumberUtil.format(enchantment.getComponent(EnchantComponent.POTION_EFFECT).getDuration(level) / 20D))
            .add(EFFECT_TYPE, () -> LangUtil.getSerializedName(enchantment.getComponent(EnchantComponent.POTION_EFFECT).getType()))
        );
    }
}
