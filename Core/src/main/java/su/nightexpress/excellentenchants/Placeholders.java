package su.nightexpress.excellentenchants;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.api.enchantment.meta.ChanceMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.PeriodMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionMeta;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.nightcore.language.LangAssets;
import su.nightexpress.nightcore.util.ItemUtil;
import su.nightexpress.nightcore.util.NumberUtil;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;

public class Placeholders extends su.nightexpress.nightcore.util.Placeholders {

    public static final String WIKI_URL          = "https://nightexpress.gitbook.io/excellentenchants/";
    public static final String WIKI_PLACEHOLDERS = WIKI_URL + "utility/placeholders";
    public static final String WIKI_CHRAGES      = WIKI_URL + "features/charges";

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

    public static final String ENCHANTMENT_CHANCE                        = "%enchantment_trigger_chance%";
    public static final String ENCHANTMENT_INTERVAL                      = "%enchantment_trigger_interval%";
    public static final String ENCHANTMENT_POTION_LEVEL                  = "%enchantment_potion_level%";
    public static final String ENCHANTMENT_POTION_DURATION               = "%enchantment_potion_duration%";
    public static final String ENCHANTMENT_POTION_TYPE                   = "%enchantment_potion_type%";
    public static final String ENCHANTMENT_ID                            = "%enchantment_id%";
    public static final String ENCHANTMENT_NAME                          = "%enchantment_name%";
    public static final String ENCHANTMENT_DESCRIPTION                   = "%enchantment_description%";
    //public static final String ENCHANTMENT_DESCRIPTION_FORMATTED         = "%enchantment_description_formatted%";
    public static final String ENCHANTMENT_DESCRIPTION_REPLACED          = "%enchantment_description_replaced%";
    public static final String ENCHANTMENT_LEVEL                         = "%enchantment_level%";
    public static final String ENCHANTMENT_LEVEL_MIN                     = "%enchantment_level_min%";
    public static final String ENCHANTMENT_LEVEL_MAX                     = "%enchantment_level_max%";
    public static final String ENCHANTMENT_RARITY                        = "%enchantment_rarity%";
    public static final String ENCHANTMENT_FIT_ITEM_TYPES                = "%enchantment_fit_item_types%";
    public static final String ENCHANTMENT_CHARGES                       = "%enchantment_charges%";
    public static final String ENCHANTMENT_CHARGES_MAX_AMOUNT            = "%enchantment_charges_max_amount%";
    public static final String ENCHANTMENT_CHARGES_CONSUME_AMOUNT        = "%enchantment_charges_consume_amount%";
    public static final String ENCHANTMENT_CHARGES_RECHARGE_AMOUNT       = "%enchantment_charges_recharge_amount%";
    public static final String ENCHANTMENT_CHARGES_FUEL_ITEM             = "%enchantment_charges_fuel_item%";

    @NotNull
    public static PlaceholderList<Integer> forEnchant(@NotNull GameEnchantment enchantment) {
        PlaceholderList<Integer> placeholders = PlaceholderList.create(list -> list
            .add(ENCHANTMENT_ID, level -> enchantment.getId())
            .add(ENCHANTMENT_NAME, level -> enchantment.getDisplayName())
            .add(ENCHANTMENT_DESCRIPTION, level -> String.join("\n", enchantment.getDescription()))
            //.add(ENCHANTMENT_DESCRIPTION_FORMATTED, () -> String.join("\n", enchantment.getDescriptionFormatted()))
            .add(ENCHANTMENT_DESCRIPTION_REPLACED, level -> String.join("\n", enchantment.getDescription(level)))
            .add(ENCHANTMENT_LEVEL, NumberUtil::toRoman)
            .add(ENCHANTMENT_LEVEL_MIN, level -> String.valueOf(1))
            .add(ENCHANTMENT_LEVEL_MAX, level -> String.valueOf(enchantment.getDefinition().getMaxLevel()))
            .add(ENCHANTMENT_RARITY, level -> enchantment.getDefinition().getRarity().getName())
            .add(ENCHANTMENT_FIT_ITEM_TYPES, level -> enchantment.getDefinition().getSupportedItems().getLocalized())
            .add(ENCHANTMENT_CHARGES_MAX_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getMaxAmount(level)))
            .add(ENCHANTMENT_CHARGES_CONSUME_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getConsumeAmount(level)))
            .add(ENCHANTMENT_CHARGES_RECHARGE_AMOUNT, level -> NumberUtil.format(enchantment.getCharges().getRechargeAmount(level)))
            .add(ENCHANTMENT_CHARGES_FUEL_ITEM, level -> ItemUtil.getItemName(enchantment.getCharges().getFuel()))
        );

        if (enchantment instanceof ChanceMeta chanceMeta) {
            placeholders.add(ENCHANTMENT_CHANCE, level -> NumberUtil.format(chanceMeta.getTriggerChance(level)));
        }

        if (enchantment instanceof PeriodMeta periodMeta) {
            placeholders.add(ENCHANTMENT_INTERVAL, () -> NumberUtil.format(periodMeta.getInterval() / 20D));
        }

        if (enchantment instanceof PotionMeta potionMeta) {
            placeholders.add(ENCHANTMENT_POTION_LEVEL, level -> NumberUtil.toRoman(potionMeta.getEffectAmplifier(level)));
            placeholders.add(ENCHANTMENT_POTION_DURATION, level -> NumberUtil.format(potionMeta.getEffectDuration(level) / 20D));
            placeholders.add(ENCHANTMENT_POTION_TYPE, () -> LangAssets.get(potionMeta.getEffectType()));
        }

        return placeholders;
    }
}

