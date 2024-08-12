package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.TradeType;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionMeta;
import su.nightexpress.excellentenchants.api.enchantment.meta.Period;
import su.nightexpress.excellentenchants.api.enchantment.meta.PotionEffects;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDefinition;
import su.nightexpress.excellentenchants.enchantment.impl.EnchantDistribution;
import su.nightexpress.excellentenchants.enchantment.impl.GameEnchantment;
import su.nightexpress.excellentenchants.util.ItemCategories;
import su.nightexpress.excellentenchants.rarity.EnchantRarity;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_POTION_LEVEL;
import static su.nightexpress.excellentenchants.Placeholders.ENCHANTMENT_POTION_TYPE;

public class WaterBreathingEnchant extends GameEnchantment implements PotionMeta, PassiveEnchant {

    public static final String ID = "aquaman";

    public WaterBreathingEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file, definition(), EnchantDistribution.treasure(TradeType.JUNGLE_SPECIAL));
    }

    @NotNull
    private static EnchantDefinition definition() {
        return EnchantDefinition.create(
            "Grants permanent " + ENCHANTMENT_POTION_TYPE + " " + ENCHANTMENT_POTION_LEVEL + " effect.",
            EnchantRarity.MYTHIC,
            1,
            ItemCategories.HELMET
        );
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.meta.setPotionEffects(PotionEffects.create(this, config, PotionEffectType.WATER_BREATHING, true));
        this.meta.setPeriod(Period.create(config));
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return this.addEffect(entity, level);
    }
}
