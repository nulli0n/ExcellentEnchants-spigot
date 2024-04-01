package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.enchantment.Rarity;
import su.nightexpress.excellentenchants.api.enchantment.data.PeriodicSettings;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionData;
import su.nightexpress.excellentenchants.api.enchantment.data.PotionSettings;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.data.AbstractEnchantmentData;
import su.nightexpress.excellentenchants.enchantment.data.PeriodSettingsImpl;
import su.nightexpress.excellentenchants.enchantment.data.PotionSettingsImpl;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;

import static su.nightexpress.excellentenchants.Placeholders.*;

public class NightVisionEnchant extends AbstractEnchantmentData implements PotionData, PassiveEnchant {

    public static final String ID = "night_vision";

    private PotionSettingsImpl potionSettings;
    private PeriodSettingsImpl periodSettings;

    public NightVisionEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file) {
        super(plugin, file);
        this.setDescription("Grants permanent " + ENCHANTMENT_POTION_TYPE + " " + ENCHANTMENT_POTION_LEVEL + " effect.");
        this.setMaxLevel(1);
        this.setRarity(Rarity.VERY_RARE);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        this.potionSettings = PotionSettingsImpl.create(this, config, PotionEffectType.NIGHT_VISION, true);
        this.periodSettings = PeriodSettingsImpl.create(config);
    }

    @NotNull
    @Override
    public PotionSettings getPotionSettings() {
        return potionSettings;
    }

    @NotNull
    @Override
    public PeriodicSettings getPeriodSettings() {
        return periodSettings;
    }

    @Override
    @NotNull
    public EnchantmentTarget getCategory() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return this.addEffect(entity, level);
    }
}