package su.nightexpress.excellentenchants.enchantment.impl.armor;

import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.ExcellentEnchants;
import su.nightexpress.excellentenchants.Placeholders;
import su.nightexpress.excellentenchants.api.enchantment.meta.Potioned;
import su.nightexpress.excellentenchants.api.enchantment.type.PassiveEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.ExcellentEnchant;
import su.nightexpress.excellentenchants.enchantment.impl.meta.PotionImplementation;

public class NightVisionEnchant extends ExcellentEnchant implements Potioned, PassiveEnchant {

    public static final String ID = "night_vision";

    private PotionImplementation potionImplementation;

    public NightVisionEnchant(@NotNull ExcellentEnchants plugin) {
        super(plugin, ID);
        this.getDefaults().setDescription("Grants permanent " + Placeholders.ENCHANTMENT_POTION_TYPE + " " + Placeholders.ENCHANTMENT_POTION_LEVEL + " effect.");
        this.getDefaults().setLevelMax(1);
        this.getDefaults().setTier(0.7);
    }

    @Override
    public void loadSettings() {
        super.loadSettings();
        this.potionImplementation = PotionImplementation.create(this, PotionEffectType.NIGHT_VISION, true);
    }

    @NotNull
    @Override
    public PotionImplementation getPotionImplementation() {
        return potionImplementation;
    }

    @Override
    @NotNull
    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ARMOR_HEAD;
    }

    @Override
    public boolean onTrigger(@NotNull LivingEntity entity, @NotNull ItemStack item, int level) {
        return this.addEffect(entity, level);
    }
}